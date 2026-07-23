package com.chefsocial.util

import android.content.Context
import android.content.Intent
import com.chefsocial.data.NewsPostEntity

fun shareNews(context: Context, post: NewsPostEntity) {
    val text = buildString {
        append(post.title)
        if (post.summary.isNotBlank()) {
            append("\n\n")
            append(post.summary)
        } else if (post.body.isNotBlank()) {
            append("\n\n")
            append(post.body.take(280))
            if (post.body.length > 280) append("…")
        }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, post.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
