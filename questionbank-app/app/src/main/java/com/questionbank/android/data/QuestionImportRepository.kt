package com.questionbank.android.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import org.json.JSONObject

class QuestionImportRepository(
    private val context: Context,
    private val dao: QuestionDao
) {

    suspend fun importBundleFromUri(uri: Uri): ImportResult {
        val sourceName = resolveFileName(uri)
        val rawJson = context.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(Charsets.UTF_8).readText()
        } ?: throw IllegalArgumentException("문제은행 파일을 열 수 없습니다.")

        val root = JSONObject(rawJson)
        val questionsJson = root.optJSONArray("questions")
            ?: throw IllegalArgumentException("문제은행 형식이 올바르지 않습니다.")

        val drafts = buildList {
            for (index in 0 until questionsJson.length()) {
                val item = questionsJson.getJSONObject(index)
                val choicesJson = item.optJSONArray("choices")
                    ?: throw IllegalArgumentException("보기 정보가 누락된 문제가 있습니다.")
                val choices = buildList {
                    for (choiceIndex in 0 until choicesJson.length()) {
                        val choice = choicesJson.getJSONObject(choiceIndex)
                        add(
                            ParsedChoiceDraft(
                                label = choice.optString("label").ifBlank { (choiceIndex + 1).toString() },
                                content = choice.optString("content").trim()
                            )
                        )
                    }
                }
                add(
                    BundleQuestionDraft(
                        prompt = item.optString("prompt").trim(),
                        subject = item.optString("subject").ifBlank { "미분류" },
                        answerLabel = item.optString("answerLabel").ifBlank { null },
                        sourceName = item.optString("sourceName").ifBlank { sourceName },
                        choices = choices
                    )
                )
            }
        }.filter { it.prompt.isNotBlank() && it.choices.size in 4..5 }

        require(drafts.isNotEmpty()) { "저장할 문제가 없습니다. JSON 번들 형식을 확인해 주세요." }

        dao.replaceBundle(drafts)
        return ImportResult(
            sourceName = sourceName,
            defaultSubject = root.optJSONArray("subjects")?.optString(0).orEmpty(),
            rawLength = rawJson.length,
            questionCount = drafts.size
        )
    }

    suspend fun loadAll(): List<QuestionWithChoices> = dao.getAllQuestions()

    suspend fun loadSubjects(): List<String> = dao.getSubjects()

    private fun resolveFileName(uri: Uri): String {
        val resolver = context.contentResolver
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index) ?: "question-bank-bundle.json"
            }
        }
        return uri.lastPathSegment ?: "question-bank-bundle.json"
    }
}

data class ImportResult(
    val sourceName: String,
    val defaultSubject: String,
    val rawLength: Int,
    val questionCount: Int
)
