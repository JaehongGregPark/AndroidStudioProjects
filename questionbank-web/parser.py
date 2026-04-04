from __future__ import annotations

import re


QUESTION_PATTERN = re.compile(
    r"^\s*(?:(?:Question|\uBB38\uC81C)\s*)?(\d+|Q\d+)[\).:]?\s+(.+)$",
    re.IGNORECASE,
)
CHOICE_PATTERN = re.compile(
    r"^\s*([1-5]|[A-E]|[\u3131-\u314E]|[\u2460-\u2464])[\).]?(?:\s+|\s*[:-]\s*)(.+)$",
    re.IGNORECASE,
)
ANSWER_PATTERN = re.compile(
    r"^\s*(?:Answer|Ans|Correct\s*Answer|\uC815\uB2F5|\uB2F5|\uD574\uB2F5)\s*[:\uFF1A.]?\s*([1-5A-E\u3131-\u314E\u2460-\u2464])\s*$",
    re.IGNORECASE,
)
SUBJECT_PATTERN = re.compile(
    r"^\s*(?:Subject|Category|\uACFC\uBAA9|\uBD84\uB958)\s*[:\uFF1A]\s*(.+)$",
    re.IGNORECASE,
)
COURSE_HEADER_PATTERN = re.compile(
    r"^\s*(?:\uC81C\s*)?(\d+)\s*\uACFC\uBAA9\s*[:\uFF1A]?\s*(.+)?$",
    re.IGNORECASE,
)
SECTION_HEADER_PATTERN = re.compile(
    r"^\s*(?:\uB300\uB2E8\uC6D0|\uC911\uB2E8\uC6D0|\uC18C\uB2E8\uC6D0|\uCC55\uD130|Chapter)\s*[:\uFF1A]?\s*(.+)$",
    re.IGNORECASE,
)


def parse_questions(raw_text: str, default_subject: str) -> list[dict]:
    questions: list[dict] = []
    lines = [line.strip() for line in raw_text.splitlines() if line.strip()]

    prompt_lines: list[str] = []
    choices: list[list[str]] = []
    labels: list[str] = []
    current_subject = default_subject
    current_answer: str | None = None
    seen_choice = False

    def flush() -> None:
        nonlocal prompt_lines, choices, labels, current_answer, seen_choice
        if not prompt_lines or len(choices) not in (4, 5):
            prompt_lines = []
            choices = []
            labels = []
            current_answer = None
            seen_choice = False
            return

        questions.append(
            {
                "prompt": _normalize_spaces(" ".join(prompt_lines)),
                "subject": current_subject,
                "answerLabel": current_answer,
                "choices": [
                    {
                        "label": labels[index] if index < len(labels) else str(index + 1),
                        "content": _normalize_spaces(" ".join(parts)),
                    }
                    for index, parts in enumerate(choices)
                ],
            }
        )
        prompt_lines = []
        choices = []
        labels = []
        current_answer = None
        seen_choice = False

    for line in lines:
        subject_match = SUBJECT_PATTERN.match(line)
        if subject_match:
            current_subject = subject_match.group(1).strip() or default_subject
            continue

        course_header_match = COURSE_HEADER_PATTERN.match(line)
        if course_header_match:
            header_subject = (course_header_match.group(2) or "").strip()
            current_subject = header_subject or default_subject
            continue

        section_header_match = SECTION_HEADER_PATTERN.match(line)
        if section_header_match and not prompt_lines and not seen_choice:
            current_subject = section_header_match.group(1).strip() or current_subject
            continue

        answer_match = ANSWER_PATTERN.match(line)
        if answer_match:
            current_answer = _normalize_label(answer_match.group(1))
            continue

        question_match = QUESTION_PATTERN.match(line)
        choice_match = CHOICE_PATTERN.match(line)

        # If a full set of choices is already present, the next numbered line
        # should start a new question rather than being consumed as another choice.
        if question_match and (current_answer is not None or (seen_choice and len(choices) in (4, 5))):
            flush()
            prompt_lines.append(question_match.group(2))
            continue

        if prompt_lines and choice_match:
            seen_choice = True
            labels.append(_normalize_label(choice_match.group(1)))
            choices.append([choice_match.group(2)])
            continue

        if question_match:
            flush()
            prompt_lines.append(question_match.group(2))
            continue

        if seen_choice and choices:
            choices[-1].append(line)
        elif prompt_lines:
            prompt_lines.append(line)

    flush()
    return questions


def _normalize_label(raw: str) -> str:
    mapping = {"\u2460": "1", "\u2461": "2", "\u2462": "3", "\u2463": "4", "\u2464": "5"}
    return mapping.get(raw.strip().upper(), raw.strip().upper())


def _normalize_spaces(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip()
