package com.questionbank.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface QuestionDao {

    @Transaction
    @Query("SELECT * FROM questions ORDER BY updatedAt DESC, createdAt DESC")
    suspend fun getAllQuestions(): List<QuestionWithChoices>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChoices(choices: List<ChoiceEntity>)

    @Query("DELETE FROM questions")
    suspend fun clearAll()

    @Query("DELETE FROM choices WHERE questionId = :questionId")
    suspend fun deleteChoicesForQuestion(questionId: Long)

    @Query("DELETE FROM questions WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: Long)

    @Query("SELECT DISTINCT subject FROM questions ORDER BY subject ASC")
    suspend fun getSubjects(): List<String>

    @Transaction
    suspend fun replaceAll(items: List<ParsedQuestionDraft>, sourceName: String, defaultSubject: String) {
        clearAll()
        items.forEach { draft ->
            val questionId = insertQuestion(
                QuestionEntity(
                    prompt = draft.prompt,
                    subject = draft.subject.ifBlank { defaultSubject },
                    answerLabel = draft.answerLabel,
                    sourceName = sourceName
                )
            )
            val choiceEntities = draft.choices.mapIndexed { index, choice ->
                ChoiceEntity(
                    questionId = questionId,
                    ordering = index + 1,
                    label = choice.label,
                    content = choice.content
                )
            }
            insertChoices(choiceEntities)
        }
    }

    @Transaction
    suspend fun replaceBundle(items: List<BundleQuestionDraft>) {
        clearAll()
        items.forEach { draft ->
            val questionId = insertQuestion(
                QuestionEntity(
                    prompt = draft.prompt,
                    subject = draft.subject,
                    answerLabel = draft.answerLabel,
                    sourceName = draft.sourceName
                )
            )
            val choiceEntities = draft.choices.mapIndexed { index, choice ->
                ChoiceEntity(
                    questionId = questionId,
                    ordering = index + 1,
                    label = choice.label,
                    content = choice.content
                )
            }
            insertChoices(choiceEntities)
        }
    }

    @Transaction
    suspend fun updateQuestionWithChoices(question: QuestionEntity, choices: List<ChoiceEntity>) {
        val updatedAt = System.currentTimeMillis()
        insertQuestion(question.copy(updatedAt = updatedAt))
        deleteChoicesForQuestion(question.id)
        insertChoices(
            choices.mapIndexed { index, choice ->
                choice.copy(id = 0, questionId = question.id, ordering = index + 1)
            }
        )
    }

    @Transaction
    suspend fun deleteQuestion(questionId: Long) {
        deleteChoicesForQuestion(questionId)
        deleteQuestionById(questionId)
    }
}
