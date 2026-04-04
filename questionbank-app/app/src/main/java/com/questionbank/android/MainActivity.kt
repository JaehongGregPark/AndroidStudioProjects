package com.questionbank.android

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.questionbank.android.data.QuestionBankDatabase
import com.questionbank.android.data.QuestionImportRepository
import com.questionbank.android.data.QuestionWithChoices
import com.questionbank.android.databinding.ActivityMainBinding
import com.questionbank.android.ui.QuestionAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: QuestionImportRepository
    private lateinit var adapter: QuestionAdapter

    private var allQuestions: List<QuestionWithChoices> = emptyList()
    private var selectedSubjectFilter: String = ALL_SUBJECTS

    private val openBundle = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            importBundle(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = QuestionImportRepository(
            context = this,
            dao = QuestionBankDatabase.getInstance(this).questionDao()
        )

        setupList()
        setupActions()
        refreshQuestions()
    }

    private fun setupList() {
        adapter = QuestionAdapter(::showQuestionDetail)
        binding.questionRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.questionRecyclerView.adapter = adapter
    }

    private fun setupActions() {
        binding.importButton.setOnClickListener {
            openBundle.launch(arrayOf("application/json", "text/plain"))
        }

        binding.refreshButton.setOnClickListener { refreshQuestions() }
        binding.searchEditText.doAfterTextChanged { renderQuestions() }
        binding.subjectFilterView.setOnItemClickListener { _, _, position, _ ->
            val picked = (binding.subjectFilterView.adapter.getItem(position) as? String).orEmpty()
            selectedSubjectFilter = if (picked.isBlank()) ALL_SUBJECTS else picked
            renderQuestions()
        }
    }

    private fun importBundle(uri: Uri) {
        setLoading(true)
        lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { repository.importBundleFromUri(uri) }
            }.onSuccess { result ->
                binding.lastImportText.text = getString(
                    R.string.last_import_format,
                    result.sourceName,
                    result.defaultSubject.ifBlank { getString(R.string.subject_unknown) },
                    result.questionCount,
                    result.rawLength
                )
                Snackbar.make(
                    binding.root,
                    getString(R.string.import_success_format, result.questionCount),
                    Snackbar.LENGTH_LONG
                ).show()
                refreshQuestions()
            }.onFailure { error ->
                setLoading(false)
                Snackbar.make(
                    binding.root,
                    error.message ?: getString(R.string.import_failed),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun refreshQuestions() {
        setLoading(true)
        lifecycleScope.launch {
            val questions = withContext(Dispatchers.IO) { repository.loadAll() }
            val subjects = withContext(Dispatchers.IO) { repository.loadSubjects() }
            allQuestions = questions
            updateSubjectFilter(subjects)
            renderQuestions()
            setLoading(false)
        }
    }

    private fun updateSubjectFilter(subjects: List<String>) {
        val options = listOf(getString(R.string.subject_filter_all)) + subjects
        binding.subjectFilterView.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        )
        if (selectedSubjectFilter == ALL_SUBJECTS || selectedSubjectFilter !in subjects) {
            selectedSubjectFilter = ALL_SUBJECTS
            binding.subjectFilterView.setText(getString(R.string.subject_filter_all), false)
        } else {
            binding.subjectFilterView.setText(selectedSubjectFilter, false)
        }
    }

    private fun renderQuestions() {
        val query = binding.searchEditText.text?.toString()?.trim().orEmpty().lowercase()
        val filtered = allQuestions.filter { item ->
            val subjectMatches = selectedSubjectFilter == ALL_SUBJECTS || item.question.subject == selectedSubjectFilter
            val queryMatches = query.isBlank() || buildSearchBlob(item).contains(query)
            subjectMatches && queryMatches
        }

        adapter.submitList(filtered)
        binding.totalQuestionText.text = getString(R.string.total_question_format, allQuestions.size)
        binding.totalChoiceText.text = getString(R.string.total_choice_format, allQuestions.sumOf { it.choices.size })
        binding.visibleCountText.text = getString(R.string.visible_count_format, filtered.size)
        binding.emptyStateText.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun buildSearchBlob(item: QuestionWithChoices): String {
        return buildString {
            append(item.question.prompt)
            append(' ')
            append(item.question.subject)
            append(' ')
            append(item.question.answerLabel.orEmpty())
            append(' ')
            item.choices.forEach { choice ->
                append(choice.label)
                append(' ')
                append(choice.content)
                append(' ')
            }
        }.lowercase()
    }

    private fun showQuestionDetail(item: QuestionWithChoices) {
        val message = buildString {
            appendLine(item.question.prompt)
            appendLine()
            appendLine(getString(R.string.detail_subject_format, item.question.subject))
            appendLine(
                item.question.answerLabel?.let { getString(R.string.detail_answer_format, it) }
                    ?: getString(R.string.detail_answer_empty)
            )
            appendLine()
            item.choices.forEach { choice ->
                appendLine("${choice.label}. ${choice.content}")
            }
        }.trim()

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.detail_title_format, item.question.id))
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.importButton.isEnabled = !loading
        binding.refreshButton.isEnabled = !loading
    }

    private companion object {
        private const val ALL_SUBJECTS = "__ALL__"
    }
}
