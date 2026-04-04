package com.questionbank.android.parser

import com.questionbank.android.data.ParsedChoiceDraft
import com.questionbank.android.data.ParsedQuestionDraft

class QuestionParser {

    private val questionPattern = Regex(
        pattern = "^\\s*(?:(?:Question|\\uBB38\\uC81C)\\s*)?(\\d+|Q\\d+)[\\).:]?\\s+(.+)$",
        option = RegexOption.IGNORE_CASE
    )

    private val choicePattern = Regex(
        pattern = "^\\s*([1-5]|[A-E]|[\\u3131-\\u314E]|[\\u2460-\\u2464])[\\).]?(?:\\s+|\\s*[:-]\\s*)(.+)$",
        option = RegexOption.IGNORE_CASE
    )

    private val answerPattern = Regex(
        pattern = "^\\s*(?:Answer|Ans|Correct\\s*Answer|\\uC815\\uB2F5|\\uB2F5|\\uD574\\uB2F5)\\s*[:：.]?\\s*([1-5A-E\\u3131-\\u314E\\u2460-\\u2464])\\s*$",
        option = RegexOption.IGNORE_CASE
    )

    private val subjectPattern = Regex(
        pattern = "^\\s*(?:Subject|Category|\\uACFC\\uBAA9|\\uBD84\\uB958)\\s*[:：]\\s*(.+)$",
        option = RegexOption.IGNORE_CASE
    )

    fun parse(rawText: String, defaultSubject: String): List<ParsedQuestionDraft> {
        val questions = mutableListOf<ParsedQuestionDraft>()
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val promptLines = mutableListOf<String>()
        val choices = mutableListOf<MutableList<String>>()
        val labels = mutableListOf<String>()
        var currentSubject = defaultSubject
        var currentAnswer: String? = null
        var seenChoice = false

        fun flushCurrentQuestion() {
            if (promptLines.isEmpty() || choices.size !in 4..5) {
                promptLines.clear()
                choices.clear()
                labels.clear()
                currentAnswer = null
                seenChoice = false
                return
            }

            questions += ParsedQuestionDraft(
                prompt = promptLines.joinToString(" ").normalizeSpaces(),
                subject = currentSubject,
                answerLabel = currentAnswer,
                choices = choices.mapIndexed { index, parts ->
                    ParsedChoiceDraft(
                        label = labels.getOrNull(index) ?: (index + 1).toString(),
                        content = parts.joinToString(" ").normalizeSpaces()
                    )
                }
            )

            promptLines.clear()
            choices.clear()
            labels.clear()
            currentAnswer = null
            seenChoice = false
        }

        lines.forEach { line ->
            subjectPattern.matchEntire(line)?.let { match ->
                currentSubject = match.groupValues[1].trim().ifBlank { defaultSubject }
                return@forEach
            }

            answerPattern.matchEntire(line)?.let { match ->
                currentAnswer = normalizeAnswerLabel(match.groupValues[1])
                return@forEach
            }

            val questionMatch = questionPattern.matchEntire(line)
            if (questionMatch != null) {
                flushCurrentQuestion()
                promptLines += questionMatch.groupValues[2]
                return@forEach
            }

            val choiceMatch = choicePattern.matchEntire(line)
            if (choiceMatch != null) {
                seenChoice = true
                labels += normalizeAnswerLabel(choiceMatch.groupValues[1])
                choices += mutableListOf(choiceMatch.groupValues[2])
                return@forEach
            }

            when {
                seenChoice && choices.isNotEmpty() -> choices.last() += line
                promptLines.isNotEmpty() -> promptLines += line
            }
        }

        flushCurrentQuestion()
        return questions
    }

    private fun normalizeAnswerLabel(raw: String): String {
        return when (raw.trim().uppercase()) {
            "\u2460" -> "1"
            "\u2461" -> "2"
            "\u2462" -> "3"
            "\u2463" -> "4"
            "\u2464" -> "5"
            else -> raw.trim().uppercase()
        }
    }

    private fun String.normalizeSpaces(): String = replace(Regex("\\s+"), " ").trim()
}
