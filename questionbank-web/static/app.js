const state = {
  questions: [],
  subjects: [],
  selectedId: null,
  query: "",
  subject: "",
  draft: null,
  draftSelectedIndex: 0,
};

const elements = {
  statusText: document.getElementById("statusText"),
  previewBox: document.getElementById("previewBox"),
  draftStatsText: document.getElementById("draftStatsText"),
  draftListCountText: document.getElementById("draftListCountText"),
  draftQuestionList: document.getElementById("draftQuestionList"),
  draftDetailPanel: document.getElementById("draftDetailPanel"),
  saveDraftButton: document.getElementById("saveDraftButton"),
  draftSaveMode: document.getElementById("draftSaveMode"),
  statsText: document.getElementById("statsText"),
  questionList: document.getElementById("questionList"),
  editorForm: document.getElementById("editorForm"),
  choiceFields: document.getElementById("choiceFields"),
  searchInput: document.getElementById("searchInput"),
  subjectFilter: document.getElementById("subjectFilter"),
  deleteButton: document.getElementById("deleteButton"),
  exportButton: document.getElementById("exportButton"),
};

function setStatus(message) {
  elements.statusText.textContent = message;
}

function buildChoiceFields() {
  elements.choiceFields.innerHTML = "";
  for (let index = 0; index < 5; index += 1) {
    const wrapper = document.createElement("div");
    wrapper.className = "choice-pair";
    wrapper.innerHTML = `
      <label class="field">
        <span>보기 ${index + 1} 라벨</span>
        <input type="text" name="choiceLabel${index + 1}" value="${index + 1}">
      </label>
      <label class="field">
        <span>보기 ${index + 1} 내용</span>
        <textarea name="choiceContent${index + 1}" rows="5"></textarea>
      </label>
    `;
    elements.choiceFields.appendChild(wrapper);
  }
}

function fillEditor(question) {
  elements.editorForm.questionId.value = question?.id ?? "";
  elements.editorForm.prompt.value = question?.prompt ?? "";
  elements.editorForm.subject.value = question?.subject ?? "";
  elements.editorForm.answerLabel.value = question?.answerLabel ?? "";
  elements.editorForm.answerExplanation.value = question?.answerExplanation ?? "";
  for (let index = 0; index < 5; index += 1) {
    const choice = question?.choices[index];
    elements.editorForm[`choiceLabel${index + 1}`].value = choice?.label ?? `${index + 1}`;
    elements.editorForm[`choiceContent${index + 1}`].value = choice?.content ?? "";
  }
}

function renderDraft() {
  if (!state.draft) {
    elements.draftStatsText.textContent = "아직 분석된 배치가 없습니다.";
    elements.draftListCountText.textContent = "0개 문항";
    elements.previewBox.textContent = "분석 전입니다.";
    elements.draftQuestionList.innerHTML = `<p class="empty">파일 또는 텍스트를 분석하면 여러 문제가 한꺼번에 표시됩니다.</p>`;
    elements.draftDetailPanel.innerHTML = `<p class="empty">배치 분석 후 왼쪽 목록에서 문항 하나를 선택하면 상세 내용이 표시됩니다.</p>`;
    return;
  }

  elements.draftStatsText.textContent =
    `${state.draft.questionCount}문항 추출 · 원문 ${state.draft.rawLength}자 · 과목 ${state.draft.subject}`;
  elements.draftListCountText.textContent = `${state.draft.questions.length}개 문항`;
  elements.previewBox.textContent = state.draft.previewText || "추출된 텍스트가 없습니다.";
  elements.draftQuestionList.innerHTML = "";

  state.draft.questions.forEach((question, index) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `draft-list-item ${state.draftSelectedIndex === index ? "active" : ""}`;
    const title = document.createElement("strong");
    title.textContent = `문항 ${index + 1}`;
    const prompt = document.createElement("span");
    prompt.className = "draft-list-prompt";
    prompt.title = question.prompt;
    prompt.textContent = question.prompt;
    const meta = document.createElement("small");
    meta.textContent = `${question.subject} · 보기 ${question.choices.length}개 · 정답 ${question.answerLabel ?? "미설정"}`;
    button.append(title, prompt, meta);
    button.addEventListener("click", () => {
      state.draftSelectedIndex = index;
      renderDraft();
    });
    elements.draftQuestionList.appendChild(button);
  });

  if (state.draftSelectedIndex >= state.draft.questions.length) {
    state.draftSelectedIndex = 0;
  }

  renderDraftDetail();
}

function renderDraftDetail() {
  if (!state.draft?.questions?.length) {
    elements.draftDetailPanel.innerHTML = `<p class="empty">표시할 문항이 없습니다.</p>`;
    return;
  }

  const question = state.draft.questions[state.draftSelectedIndex];
  elements.draftDetailPanel.innerHTML = "";

  const article = document.createElement("article");
  article.className = "draft-item draft-item-detail";

  const header = document.createElement("header");
  const title = document.createElement("strong");
  title.textContent = `문항 ${state.draftSelectedIndex + 1}`;
  const meta = document.createElement("span");
  meta.textContent = `${question.subject} · 보기 ${question.choices.length}개 · 정답 ${question.answerLabel ?? "미설정"}`;
  header.append(title, meta);

  const prompt = document.createElement("p");
  prompt.textContent = question.prompt;

  if (question.answerExplanation) {
    const explanationTitle = document.createElement("strong");
    explanationTitle.textContent = "정답 상세해설";
    const explanation = document.createElement("p");
    explanation.className = "draft-explanation";
    explanation.textContent = question.answerExplanation;
    article.append(header, prompt, explanationTitle, explanation);
  } else {
    article.append(header, prompt);
  }

  const list = document.createElement("ol");
  question.choices.forEach((choice) => {
    const item = document.createElement("li");
    item.textContent = `${choice.label}. ${choice.content}`;
    list.appendChild(item);
  });

  article.append(list);
  elements.draftDetailPanel.appendChild(article);
}

function renderQuestions() {
  elements.questionList.innerHTML = "";
  if (!state.questions.length) {
    elements.questionList.innerHTML = `<p class="empty">저장된 문제가 없습니다.</p>`;
    fillEditor(null);
    return;
  }

  state.questions.forEach((question, index) => {
    const row = document.createElement("div");
    row.className = `question-row ${state.selectedId === question.id ? "active" : ""}`;

    const number = document.createElement("span");
    number.className = "question-row-number";
    number.textContent = String(index + 1);

    const item = document.createElement("button");
    item.type = "button";
    item.className = "question-item";
    const title = document.createElement("strong");
    title.textContent = question.prompt;
    const meta = document.createElement("small");
    meta.textContent = `정답 ${question.answerLabel ?? "미설정"} · 보기 ${question.choices.length}개 · ${question.sourceName}`;
    item.append(title, meta);
    item.addEventListener("click", () => {
      state.selectedId = question.id;
      fillEditor(question);
      renderQuestions();
    });

    const deleteButton = document.createElement("button");
    deleteButton.type = "button";
    deleteButton.className = "question-row-delete";
    deleteButton.textContent = "삭제";
    deleteButton.addEventListener("click", async () => {
      if (!window.confirm("선택한 문제를 삭제할까요?")) {
        return;
      }
      const response = await fetch(`/api/questions/${question.id}`, { method: "DELETE" });
      const data = await response.json();
      if (state.selectedId === question.id) {
        state.selectedId = null;
      }
      setStatus(data.message || "문제를 삭제했습니다.");
      await loadQuestions();
    });

    row.append(number, item, deleteButton);
    elements.questionList.appendChild(row);
  });

  const selected = state.questions.find((item) => item.id === state.selectedId) ?? state.questions[0];
  state.selectedId = selected.id;
  fillEditor(selected);
}

function renderSubjects() {
  const current = state.subject;
  elements.subjectFilter.innerHTML = "";
  const options = ["", ...state.subjects];
  options.forEach((subject) => {
    const option = document.createElement("option");
    option.value = subject;
    option.textContent = subject || "전체 과목";
    if (subject === current) {
      option.selected = true;
    }
    elements.subjectFilter.appendChild(option);
  });
}

async function loadQuestions() {
  const params = new URLSearchParams();
  if (state.query) params.set("query", state.query);
  if (state.subject) params.set("subject", state.subject);
  const response = await fetch(`/api/questions?${params.toString()}`);
  const data = await response.json();
  state.questions = data.questions;
  state.subjects = data.subjects;
  elements.statsText.textContent = `${data.stats.questionCount}문항 · ${data.stats.choiceCount}개 보기`;
  renderSubjects();
  renderQuestions();
}

async function handleParseResponse(response) {
  const data = await response.json();
  if (!response.ok) {
    setStatus(data.error || "배치 분석에 실패했습니다.");
    return;
  }
  state.draft = data;
  state.draftSelectedIndex = 0;
  renderDraft();
  setStatus(data.message);
}

async function handleFileParse(event) {
  event.preventDefault();
  const formData = new FormData(event.target);
  setStatus("파일 전체 문제를 배치 분석하고 있습니다...");
  const response = await fetch("/api/parse-file", { method: "POST", body: formData });
  await handleParseResponse(response);
}

async function handleTextParse(event) {
  event.preventDefault();
  const payload = Object.fromEntries(new FormData(event.target).entries());
  setStatus("텍스트 전체 문제를 배치 분석하고 있습니다...");
  const response = await fetch("/api/parse-text", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  await handleParseResponse(response);
}

async function handleSaveDraft() {
  if (!state.draft?.questions?.length) {
    setStatus("먼저 파일 또는 텍스트를 배치 분석해 주세요.");
    return;
  }

  setStatus("추출된 문제 전체를 저장하고 있습니다...");
  const response = await fetch("/api/save-draft", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      sourceName: state.draft.sourceName,
      mode: elements.draftSaveMode.value,
      questions: state.draft.questions,
    }),
  });
  const data = await response.json();
  if (!response.ok) {
    setStatus(data.error || "전체 저장에 실패했습니다.");
    return;
  }
  setStatus(data.message);
  await loadQuestions();
}

async function handleEditorSave(event) {
  event.preventDefault();
  const questionId = elements.editorForm.questionId.value;
  if (!questionId) {
    setStatus("수정할 문제를 먼저 선택해 주세요.");
    return;
  }
  const choices = [];
  for (let index = 0; index < 5; index += 1) {
    choices.push({
      label: elements.editorForm[`choiceLabel${index + 1}`].value,
      content: elements.editorForm[`choiceContent${index + 1}`].value,
    });
  }
  const payload = {
    prompt: elements.editorForm.prompt.value,
    subject: elements.editorForm.subject.value,
    answerLabel: elements.editorForm.answerLabel.value,
    answerExplanation: elements.editorForm.answerExplanation.value,
    choices,
  };
  const response = await fetch(`/api/questions/${questionId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const data = await response.json();
  setStatus(data.message || data.error || "저장 결과를 확인해 주세요.");
  await loadQuestions();
}

async function handleDelete() {
  const questionId = elements.editorForm.questionId.value;
  if (!questionId) {
    setStatus("삭제할 문제를 먼저 선택해 주세요.");
    return;
  }
  if (!window.confirm("선택한 문제를 삭제할까요?")) {
    return;
  }
  const response = await fetch(`/api/questions/${questionId}`, { method: "DELETE" });
  const data = await response.json();
  state.selectedId = null;
  setStatus(data.message || "문제를 삭제했습니다.");
  await loadQuestions();
}

function handleExport() {
  window.location.href = "/api/export";
}

document.getElementById("fileParseForm").addEventListener("submit", handleFileParse);
document.getElementById("textParseForm").addEventListener("submit", handleTextParse);
elements.saveDraftButton.addEventListener("click", handleSaveDraft);
elements.editorForm.addEventListener("submit", handleEditorSave);
elements.deleteButton.addEventListener("click", handleDelete);
elements.exportButton.addEventListener("click", handleExport);
elements.searchInput.addEventListener("input", async (event) => {
  state.query = event.target.value.trim();
  await loadQuestions();
});
elements.subjectFilter.addEventListener("change", async (event) => {
  state.subject = event.target.value;
  await loadQuestions();
});

buildChoiceFields();
fillEditor(null);
renderDraft();
loadQuestions();
