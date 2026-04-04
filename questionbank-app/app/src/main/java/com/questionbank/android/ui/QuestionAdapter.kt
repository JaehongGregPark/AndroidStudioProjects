package com.questionbank.android.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.questionbank.android.R
import com.questionbank.android.data.QuestionWithChoices
import com.questionbank.android.databinding.ItemQuestionBinding

class QuestionAdapter(
    private val onQuestionClick: (QuestionWithChoices) -> Unit
) : ListAdapter<QuestionWithChoices, QuestionAdapter.QuestionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = ItemQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class QuestionViewHolder(
        private val binding: ItemQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuestionWithChoices) {
            val context = binding.root.context
            binding.questionNumberText.text = context.getString(
                R.string.question_number_format,
                bindingAdapterPosition + 1
            )
            binding.promptText.text = item.question.prompt
            binding.answerText.text = item.question.answerLabel?.let {
                context.getString(R.string.detail_answer_format, it)
            } ?: context.getString(R.string.detail_answer_empty)
            binding.metaText.text = context.getString(
                R.string.question_meta_format,
                item.question.subject,
                item.choices.size,
                item.question.sourceName
            )
            binding.root.setOnClickListener { onQuestionClick(item) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<QuestionWithChoices>() {
        override fun areItemsTheSame(oldItem: QuestionWithChoices, newItem: QuestionWithChoices): Boolean {
            return oldItem.question.id == newItem.question.id
        }

        override fun areContentsTheSame(oldItem: QuestionWithChoices, newItem: QuestionWithChoices): Boolean {
            return oldItem == newItem
        }
    }
}
