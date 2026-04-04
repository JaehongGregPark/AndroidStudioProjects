from __future__ import annotations

from html import unescape
from io import BytesIO
from pathlib import Path
from zipfile import ZipFile
import re
import sys

sys.path.insert(0, str(Path(__file__).resolve().parent / "vendor"))

from pypdf import PdfReader


def extract_text(filename: str, payload: bytes) -> str:
    extension = Path(filename).suffix.lower()
    if extension == ".txt":
        return _normalize_text(_decode_text(payload))
    if extension == ".docx":
        return _normalize_text(_read_docx(payload))
    if extension == ".pdf":
        return _normalize_text(_read_pdf(payload))
    if extension == ".doc":
        raise ValueError("doc 형식은 지원하지 않습니다. docx로 변환해 주세요.")
    raise ValueError(f"지원하지 않는 파일 형식입니다: {extension}")


def _decode_text(payload: bytes) -> str:
    if payload.startswith(b"\xef\xbb\xbf"):
        return payload[3:].decode("utf-8", errors="ignore")

    for encoding in ("utf-8", "cp949", "iso-8859-1"):
        try:
            text = payload.decode(encoding)
            if "\ufffd" not in text:
                return text
        except UnicodeDecodeError:
            continue
    return payload.decode("utf-8", errors="ignore")


def _read_docx(payload: bytes) -> str:
    with ZipFile(BytesIO(payload)) as archive:
        xml_text = archive.read("word/document.xml").decode("utf-8", errors="ignore")
    xml_text = xml_text.replace("</w:p>", "\n").replace("<w:tab/>", "\t")
    xml_text = re.sub(r"<[^>]+>", " ", xml_text)
    return unescape(xml_text)


def _read_pdf(payload: bytes) -> str:
    reader = PdfReader(BytesIO(payload))
    pages = [page.extract_text() or "" for page in reader.pages]
    return "\n".join(pages)


def _normalize_text(text: str) -> str:
    lines = [line.replace("\xa0", " ").rstrip() for line in text.splitlines()]
    normalized = "\n".join(lines)
    normalized = re.sub(r"\n{3,}", "\n\n", normalized)
    return normalized.strip()
