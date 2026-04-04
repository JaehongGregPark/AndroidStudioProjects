from __future__ import annotations

from datetime import datetime, timezone
import json
import sqlite3
from pathlib import Path
from typing import Iterable


class QuestionStore:
    def __init__(self, db_path: Path) -> None:
        self.db_path = db_path
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self._init_db()

    def _connect(self) -> sqlite3.Connection:
        connection = sqlite3.connect(self.db_path)
        connection.row_factory = sqlite3.Row
        connection.execute("PRAGMA foreign_keys = ON")
        return connection

    def _init_db(self) -> None:
        with self._connect() as conn:
            conn.executescript(
                """
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    prompt TEXT NOT NULL,
                    subject TEXT NOT NULL,
                    answer_label TEXT,
                    answer_explanation TEXT,
                    source_name TEXT NOT NULL,
                    created_at INTEGER NOT NULL DEFAULT (strftime('%s','now')),
                    updated_at INTEGER NOT NULL DEFAULT (strftime('%s','now'))
                );

                CREATE TABLE IF NOT EXISTS choices (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    question_id INTEGER NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
                    ordering INTEGER NOT NULL,
                    label TEXT NOT NULL,
                    content TEXT NOT NULL
                );
                """
            )
            self._ensure_column(conn, "questions", "answer_explanation", "TEXT")

    def _ensure_column(self, conn: sqlite3.Connection, table_name: str, column_name: str, column_sql: str) -> None:
        columns = conn.execute(f"PRAGMA table_info({table_name})").fetchall()
        if column_name not in {column["name"] for column in columns}:
            conn.execute(f"ALTER TABLE {table_name} ADD COLUMN {column_name} {column_sql}")

    def import_questions(self, questions: list[dict], source_name: str, mode: str = "replace") -> int:
        with self._connect() as conn:
            if mode == "replace":
                conn.execute("DELETE FROM questions")

            for item in questions:
                cursor = conn.execute(
                    """
                    INSERT INTO questions (prompt, subject, answer_label, answer_explanation, source_name)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    (
                        item["prompt"],
                        item["subject"],
                        item.get("answerLabel"),
                        item.get("answerExplanation"),
                        source_name,
                    ),
                )
                question_id = cursor.lastrowid
                conn.executemany(
                    """
                    INSERT INTO choices (question_id, ordering, label, content)
                    VALUES (?, ?, ?, ?)
                    """,
                    [
                        (question_id, index + 1, choice["label"], choice["content"])
                        for index, choice in enumerate(item["choices"])
                    ],
                )
        return len(questions)

    def list_questions(self, query: str = "", subject: str = "") -> list[dict]:
        query = query.strip().lower()
        subject = subject.strip()
        with self._connect() as conn:
            rows = conn.execute(
                "SELECT * FROM questions ORDER BY updated_at DESC, id DESC"
            ).fetchall()

            questions = []
            for row in rows:
                choices = conn.execute(
                    "SELECT * FROM choices WHERE question_id = ? ORDER BY ordering ASC",
                    (row["id"],),
                ).fetchall()
                item = {
                    "id": row["id"],
                    "prompt": row["prompt"],
                    "subject": row["subject"],
                    "answerLabel": row["answer_label"],
                    "answerExplanation": row["answer_explanation"],
                    "sourceName": row["source_name"],
                    "choices": [
                        {
                            "id": choice["id"],
                            "ordering": choice["ordering"],
                            "label": choice["label"],
                            "content": choice["content"],
                        }
                        for choice in choices
                    ],
                }
                if subject and item["subject"] != subject:
                    continue
                if query and query not in self._search_blob(item):
                    continue
                questions.append(item)
            return questions

    def list_subjects(self) -> list[str]:
        with self._connect() as conn:
            rows = conn.execute(
                "SELECT DISTINCT subject FROM questions ORDER BY subject ASC"
            ).fetchall()
        return [row["subject"] for row in rows]

    def update_question(self, question_id: int, payload: dict) -> None:
        choices = [choice for choice in payload["choices"] if choice["content"].strip()]
        if len(choices) not in (4, 5):
            raise ValueError("보기는 4개 또는 5개여야 합니다.")

        with self._connect() as conn:
            conn.execute(
                """
                UPDATE questions
                SET prompt = ?, subject = ?, answer_label = ?, answer_explanation = ?, updated_at = strftime('%s','now')
                WHERE id = ?
                """,
                (
                    payload["prompt"].strip(),
                    payload["subject"].strip(),
                    payload.get("answerLabel") or None,
                    payload.get("answerExplanation") or None,
                    question_id,
                ),
            )
            conn.execute("DELETE FROM choices WHERE question_id = ?", (question_id,))
            conn.executemany(
                """
                INSERT INTO choices (question_id, ordering, label, content)
                VALUES (?, ?, ?, ?)
                """,
                [
                    (
                        question_id,
                        index + 1,
                        choice["label"].strip() or str(index + 1),
                        choice["content"].strip(),
                    )
                    for index, choice in enumerate(choices)
                ],
            )

    def delete_question(self, question_id: int) -> None:
        with self._connect() as conn:
            conn.execute("DELETE FROM questions WHERE id = ?", (question_id,))

    def export_bundle(self) -> dict:
        questions = self.list_questions()
        return {
            "schemaVersion": 1,
            "source": "questionbank-web",
            "syncMode": "manual-json-export-import",
            "exportedAt": datetime.now(timezone.utc).isoformat(),
            "questionCount": len(questions),
            "subjects": self.list_subjects(),
            "questions": [
                {
                    "prompt": question["prompt"],
                    "subject": question["subject"],
                    "answerLabel": question["answerLabel"],
                    "answerExplanation": question.get("answerExplanation"),
                    "sourceName": question["sourceName"],
                    "choices": [
                        {
                            "label": choice["label"],
                            "content": choice["content"],
                        }
                        for choice in question["choices"]
                    ],
                }
                for question in questions
            ],
        }

    def stats(self) -> dict:
        questions = self.list_questions()
        return {
            "questionCount": len(questions),
            "choiceCount": sum(len(item["choices"]) for item in questions),
        }

    def _search_blob(self, item: dict) -> str:
        parts = [
            item["prompt"],
            item["subject"],
            item.get("answerLabel") or "",
            item.get("answerExplanation") or "",
        ]
        for choice in item["choices"]:
            parts.extend([choice["label"], choice["content"]])
        return " ".join(parts).lower()
