package com.questionbank.android.data

import androidx.room.Embedded
import androidx.room.Relation

data class QuestionWithChoices(
    @Embedded val question: QuestionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "questionId",
        entity = ChoiceEntity::class
    )
    val choices: List<ChoiceEntity>
)

data class ParsedQuestionDraft(
    val prompt: String,
    val subject: String,
    val answerLabel: String?,
    val choices: List<ParsedChoiceDraft>
)

data class ParsedChoiceDraft(
    val label: String,
    val content: String
)

data class BundleQuestionDraft(
    val prompt: String,
    val subject: String,
    val answerLabel: String?,
    val sourceName: String,
    val choices: List<ParsedChoiceDraft>
)
