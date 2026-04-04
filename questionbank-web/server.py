from __future__ import annotations

import json
import mimetypes
from email.parser import BytesParser
from email.policy import default
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import parse_qs, urlparse

from extractor import extract_text
from parser import parse_questions
from storage import QuestionStore


ROOT = Path(__file__).resolve().parent
STATIC_DIR = ROOT / "static"
STORE = QuestionStore(ROOT / "data" / "question_bank.sqlite3")


class AppHandler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        parsed = urlparse(self.path)
        if parsed.path == "/":
            self._serve_file(STATIC_DIR / "index.html", "text/html; charset=utf-8")
            return
        if parsed.path == "/app.js":
            self._serve_file(STATIC_DIR / "app.js", "application/javascript; charset=utf-8")
            return
        if parsed.path == "/styles.css":
            self._serve_file(STATIC_DIR / "styles.css", "text/css; charset=utf-8")
            return
        if parsed.path == "/api/questions":
            params = parse_qs(parsed.query)
            query = params.get("query", [""])[0]
            subject = params.get("subject", [""])[0]
            self._send_json(
                {
                    "questions": STORE.list_questions(query=query, subject=subject),
                    "subjects": STORE.list_subjects(),
                    "stats": STORE.stats(),
                }
            )
            return
        if parsed.path == "/api/export":
            bundle = STORE.export_bundle()
            payload = json.dumps(bundle, ensure_ascii=False, indent=2).encode("utf-8")
            self.send_response(HTTPStatus.OK)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Disposition", 'attachment; filename="question-bank-bundle.json"')
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)
            return
        self.send_error(HTTPStatus.NOT_FOUND)

    def do_POST(self) -> None:
        if self.path == "/api/parse-text":
            payload = self._read_json_body()
            source_name = (payload.get("sourceName") or "pasted-text").strip() or "pasted-text"
            default_subject = (payload.get("subject") or source_name).strip() or "\uBBF8\uBD84\uB958"
            questions = parse_questions(payload.get("text", ""), default_subject)
            if payload.get("subject"):
                for item in questions:
                    item["subject"] = default_subject
            self._send_parse_response(
                source_name=source_name,
                default_subject=default_subject,
                raw_text=payload.get("text", ""),
                questions=questions,
            )
            return

        if self.path == "/api/parse-file":
            form = self._read_multipart()
            file_part = form.get("file")
            if not file_part or not file_part["filename"]:
                self._send_json(
                    {"error": "\uC5C5\uB85C\uB4DC\uD560 \uD30C\uC77C\uC744 \uC120\uD0DD\uD574 \uC8FC\uC138\uC694."},
                    status=HTTPStatus.BAD_REQUEST,
                )
                return

            filename = file_part["filename"]
            subject_override = (form.get("subject", {}).get("value") or "").strip()
            raw_text = extract_text(filename, file_part["content"])
            default_subject = subject_override or Path(filename).stem or "\uBBF8\uBD84\uB958"
            questions = parse_questions(raw_text, default_subject)
            if subject_override:
                for item in questions:
                    item["subject"] = subject_override
            self._send_parse_response(
                source_name=filename,
                default_subject=default_subject,
                raw_text=raw_text,
                questions=questions,
            )
            return

        if self.path == "/api/save-draft":
            payload = self._read_json_body()
            source_name = (payload.get("sourceName") or "draft-import").strip() or "draft-import"
            mode = (payload.get("mode") or "replace").strip() or "replace"
            questions = payload.get("questions") or []
            cleaned_questions = self._clean_questions(questions)
            if not cleaned_questions:
                self._send_json(
                    {"error": "\uC800\uC7A5\uD560 \uBB38\uC81C\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4. \uBA3C\uC800 \uBC30\uCE58 \uCD94\uCD9C\uC744 \uC2E4\uD589\uD574 \uC8FC\uC138\uC694."},
                    status=HTTPStatus.BAD_REQUEST,
                )
                return
            count = STORE.import_questions(cleaned_questions, source_name=source_name, mode=mode)
            self._send_json(
                {
                    "message": f"{count}\uAC1C\uC758 \uBB38\uC81C\uB97C \uBB38\uC81C\uC740\uD589\uC5D0 \uC800\uC7A5\uD588\uC2B5\uB2C8\uB2E4.",
                    "count": count,
                }
            )
            return

        self.send_error(HTTPStatus.NOT_FOUND)

    def do_PUT(self) -> None:
        if self.path.startswith("/api/questions/"):
            question_id = int(self.path.rsplit("/", 1)[-1])
            payload = self._read_json_body()
            STORE.update_question(question_id, payload)
            self._send_json({"message": "\uBB38\uC81C\uB97C \uC218\uC815\uD588\uC2B5\uB2C8\uB2E4."})
            return
        self.send_error(HTTPStatus.NOT_FOUND)

    def do_DELETE(self) -> None:
        if self.path.startswith("/api/questions/"):
            question_id = int(self.path.rsplit("/", 1)[-1])
            STORE.delete_question(question_id)
            self._send_json({"message": "\uBB38\uC81C\uB97C \uC0AD\uC81C\uD588\uC2B5\uB2C8\uB2E4."})
            return
        self.send_error(HTTPStatus.NOT_FOUND)

    def log_message(self, format: str, *args) -> None:
        return

    def _send_parse_response(
        self,
        *,
        source_name: str,
        default_subject: str,
        raw_text: str,
        questions: list[dict],
    ) -> None:
        if not questions:
            self._send_json(
                {
                    "error": "\uBB38\uC81C \uBE14\uB85D\uC744 \uCC3E\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4. \uD55C \uD30C\uC77C\uC5D0 \uC5EC\uB7EC \uBB38\uD56D\uC774 \uC788\uC5B4\uB3C4 \uBB38\uD56D \uBC88\uD638/\uBCF4\uAE30 \uAD6C\uC870\uAC00 \uC77C\uAD00\uB418\uBA74 \uC77C\uAD04 \uCD94\uCD9C\uB429\uB2C8\uB2E4."
                },
                status=HTTPStatus.BAD_REQUEST,
            )
            return

        preview = []
        for index, item in enumerate(questions[:20], start=1):
            preview.append(
                {
                    "number": index,
                    "prompt": item["prompt"],
                    "subject": item["subject"],
                    "answerLabel": item.get("answerLabel"),
                    "choiceCount": len(item["choices"]),
                    "choices": item["choices"],
                }
            )

        self._send_json(
            {
                "message": f"{len(questions)}\uAC1C\uC758 \uBB38\uC81C\uAC00 \uBC30\uCE58\uB85C \uCD94\uCD9C\uB418\uC5C8\uC2B5\uB2C8\uB2E4.",
                "sourceName": source_name,
                "subject": default_subject,
                "rawLength": len(raw_text),
                "questionCount": len(questions),
                "previewText": raw_text[:1600],
                "questions": questions,
                "previewQuestions": preview,
            }
        )

    def _clean_questions(self, questions: list[dict]) -> list[dict]:
        cleaned = []
        for item in questions:
            prompt = (item.get("prompt") or "").strip()
            subject = (item.get("subject") or "").strip() or "\uBBF8\uBD84\uB958"
            answer = (item.get("answerLabel") or "").strip() or None
            raw_choices = item.get("choices") or []
            choices = []
            for index, choice in enumerate(raw_choices):
                content = (choice.get("content") or "").strip()
                if not content:
                    continue
                label = (choice.get("label") or "").strip() or str(index + 1)
                choices.append({"label": label, "content": content})
            if prompt and len(choices) in (4, 5):
                cleaned.append(
                    {
                        "prompt": prompt,
                        "subject": subject,
                        "answerLabel": answer,
                        "choices": choices,
                    }
                )
        return cleaned

    def _serve_file(self, path: Path, content_type: str | None = None) -> None:
        if not path.exists():
            self.send_error(HTTPStatus.NOT_FOUND)
            return
        payload = path.read_bytes()
        self.send_response(HTTPStatus.OK)
        self.send_header(
            "Content-Type",
            content_type or mimetypes.guess_type(path.name)[0] or "application/octet-stream",
        )
        self.send_header("Content-Length", str(len(payload)))
        self.end_headers()
        self.wfile.write(payload)

    def _read_json_body(self) -> dict:
        length = int(self.headers.get("Content-Length", "0"))
        raw = self.rfile.read(length) if length else b"{}"
        return json.loads(raw.decode("utf-8"))

    def _read_multipart(self) -> dict:
        content_type = self.headers.get("Content-Type", "")
        length = int(self.headers.get("Content-Length", "0"))
        raw = self.rfile.read(length)
        message = BytesParser(policy=default).parsebytes(
            f"Content-Type: {content_type}\r\nMIME-Version: 1.0\r\n\r\n".encode("utf-8") + raw
        )
        fields: dict = {}
        for part in message.iter_parts():
            name = part.get_param("name", header="content-disposition")
            if not name:
                continue
            filename = part.get_filename()
            content = part.get_payload(decode=True) or b""
            if filename:
                fields[name] = {"filename": filename, "content": content}
            else:
                fields[name] = {"value": content.decode("utf-8", errors="ignore")}
        return fields

    def _send_json(self, payload: dict, status: HTTPStatus = HTTPStatus.OK) -> None:
        encoded = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(encoded)))
        self.end_headers()
        self.wfile.write(encoded)


if __name__ == "__main__":
    server = ThreadingHTTPServer(("127.0.0.1", 8765), AppHandler)
    print("Question Bank Web running at http://127.0.0.1:8765")
    server.serve_forever()
