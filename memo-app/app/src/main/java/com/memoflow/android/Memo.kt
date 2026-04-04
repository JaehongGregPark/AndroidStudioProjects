package com.memoflow.android

data class Memo(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long? = null
) {
    fun effectiveTitle(): String = if (title.isBlank()) "\uC81C\uBAA9 \uC5C6\uB294 \uBA54\uBAA8" else title
}
