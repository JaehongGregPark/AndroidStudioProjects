package com.memoflow.android

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.memoflow.android.databinding.ActivityMainBinding
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storage: MemoStorage
    private lateinit var adapter: MemoAdapter

    private val allMemos = mutableListOf<Memo>()
    private var editingMemoId: String? = null
    private var isListVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = MemoStorage(this)
        allMemos.addAll(storage.loadMemos())

        setupRecyclerView()
        setupListeners()
        renderMemos()
        updateEditMode()
    }

    private fun setupRecyclerView() {
        adapter = MemoAdapter(
            onMemoClicked = { memo -> showMemoDetail(memo) },
            onEditClicked = { memo -> startEditing(memo) },
            onDeleteClicked = { memo -> deleteMemo(memo) }
        )
        binding.memoRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.memoRecyclerView.adapter = adapter
    }

    private fun setupListeners() {
        binding.saveButton.setOnClickListener { saveMemo() }
        binding.clearButton.setOnClickListener { clearForm() }
        binding.cancelEditButton.setOnClickListener { clearForm(showMessage = false) }
        binding.deleteAllButton.setOnClickListener { confirmDeleteAll() }
        binding.closeListButton.setOnClickListener { toggleListPanel(forceVisible = false) }
        binding.searchEditText.doAfterTextChanged { renderMemos() }
        binding.toggleListButton.setOnClickListener { toggleListPanel() }
    }

    private fun saveMemo() {
        val title = binding.titleEditText.text?.toString()?.trim().orEmpty()
        val content = binding.contentEditText.text?.toString()?.trim().orEmpty()

        if (content.isBlank()) {
            binding.contentInputLayout.error = getString(R.string.error_content_required)
            return
        }

        if (title.length > 60) {
            binding.titleInputLayout.error = getString(R.string.error_title_too_long)
            return
        }

        if (content.length > 500) {
            binding.contentInputLayout.error = getString(R.string.error_content_too_long)
            return
        }

        binding.titleInputLayout.error = null
        binding.contentInputLayout.error = null
        val now = System.currentTimeMillis()

        if (editingMemoId == null) {
            allMemos.add(
                0,
                Memo(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    createdAt = now
                )
            )
        } else {
            val index = allMemos.indexOfFirst { it.id == editingMemoId }
            if (index >= 0) {
                val current = allMemos[index]
                allMemos[index] = current.copy(
                    title = title,
                    content = content,
                    updatedAt = now
                )
            }
        }

        storage.saveMemos(allMemos)
        clearForm(showMessage = false)
        renderMemos()
        Snackbar.make(binding.root, getString(R.string.message_saved), Snackbar.LENGTH_SHORT).show()
    }

    private fun startEditing(memo: Memo) {
        editingMemoId = memo.id
        binding.titleEditText.setText(memo.title)
        binding.contentEditText.setText(memo.content)
        binding.statusText.text = getString(R.string.status_editing, memo.effectiveTitle())
        updateEditMode()
        binding.titleEditText.requestFocus()
        if (isListVisible) {
            toggleListPanel(forceVisible = false)
        }
    }

    private fun deleteMemo(memo: Memo) {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, memo.effectiveTitle()))
            .setPositiveButton(R.string.dialog_confirm_delete) { _, _ ->
                allMemos.removeAll { it.id == memo.id }
                storage.saveMemos(allMemos)
                if (editingMemoId == memo.id) {
                    clearForm(showMessage = false)
                }
                renderMemos()
                Snackbar.make(binding.root, getString(R.string.message_deleted), Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun confirmDeleteAll() {
        if (allMemos.isEmpty()) {
            return
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_all_title)
            .setMessage(R.string.dialog_delete_all_message)
            .setPositiveButton(R.string.dialog_confirm_delete_all) { _, _ ->
                allMemos.clear()
                storage.saveMemos(allMemos)
                clearForm(showMessage = false)
                renderMemos()
                Snackbar.make(binding.root, getString(R.string.message_all_deleted), Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .show()
    }

    private fun clearForm(showMessage: Boolean = true) {
        editingMemoId = null
        binding.titleEditText.text?.clear()
        binding.contentEditText.text?.clear()
        binding.titleInputLayout.error = null
        binding.contentInputLayout.error = null
        binding.statusText.text = getString(R.string.status_new_memo)
        updateEditMode()

        if (showMessage) {
            Snackbar.make(binding.root, getString(R.string.message_cleared), Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun renderMemos() {
        val query = binding.searchEditText.text?.toString()?.trim().orEmpty().lowercase()
        val filtered = if (query.isBlank()) {
            allMemos
        } else {
            allMemos.filter { memo ->
                "${memo.effectiveTitle()} ${memo.content}".lowercase().contains(query)
            }
        }

        adapter.submitList(filtered)
        binding.totalCountText.text = getString(R.string.count_total_format, allMemos.size)
        binding.visibleCountText.text = getString(R.string.count_visible_format, filtered.size)
        binding.emptyStateText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateEditMode() {
        val editing = editingMemoId != null
        binding.saveButton.text = getString(if (editing) R.string.save_edit else R.string.save_memo)
        binding.cancelEditButton.isEnabled = editing
        binding.cancelEditButton.alpha = if (editing) 1f else 0.5f
    }

    private fun toggleListPanel(forceVisible: Boolean? = null) {
        isListVisible = forceVisible ?: !isListVisible
        binding.summaryCard.visibility = if (isListVisible) View.GONE else View.VISIBLE
        binding.editorCard.visibility = if (isListVisible) View.GONE else View.VISIBLE
        binding.listPanelCard.visibility = if (isListVisible) View.VISIBLE else View.GONE
        binding.listContentCard.visibility = if (isListVisible) View.VISIBLE else View.GONE
        binding.toggleListButton.text = getString(
            if (isListVisible) R.string.toggle_list_close else R.string.toggle_list_open
        )
    }

    private fun showMemoDetail(memo: Memo) {
        AlertDialog.Builder(this)
            .setTitle(R.string.detail_title)
            .setMessage(getString(R.string.detail_message_format, memo.effectiveTitle(), memo.content))
            .setPositiveButton(android.R.string.ok, null)
            .setNeutralButton(R.string.edit) { _, _ -> startEditing(memo) }
            .show()
    }
}
