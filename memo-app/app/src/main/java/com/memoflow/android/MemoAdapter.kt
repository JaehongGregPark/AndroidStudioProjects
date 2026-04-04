package com.memoflow.android

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.memoflow.android.databinding.ItemMemoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemoAdapter(
    private val onMemoClicked: (Memo) -> Unit,
    private val onEditClicked: (Memo) -> Unit,
    private val onDeleteClicked: (Memo) -> Unit
) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    private val items = mutableListOf<Memo>()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

    fun submitList(memos: List<Memo>) {
        items.clear()
        items.addAll(memos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MemoViewHolder(
        private val binding: ItemMemoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(memo: Memo) {
            binding.titleText.text = memo.effectiveTitle()
            binding.contentText.text = memo.content.replace("\n", " ")
            binding.timeText.text = if (memo.updatedAt != null) {
                "\uC218\uC815\uB428 ${formatter.format(Date(memo.updatedAt))}"
            } else {
                "\uC791\uC131\uB428 ${formatter.format(Date(memo.createdAt))}"
            }

            binding.editButton.setOnClickListener { onEditClicked(memo) }
            binding.deleteButton.setOnClickListener { onDeleteClicked(memo) }
            binding.root.setOnClickListener { onMemoClicked(memo) }
        }
    }
}
