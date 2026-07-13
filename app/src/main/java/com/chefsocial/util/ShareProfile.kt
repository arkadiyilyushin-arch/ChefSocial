package com.chefsocial.util

import android.content.Context
import android.content.Intent
import com.chefsocial.data.ChefEntity

fun shareProfile(context: Context, chef: ChefEntity) {
    val text = buildString {
        append("Смотри профиль ${chef.name} (@${chef.username}) в Chefly!")
        if (chef.specialty.isNotBlank()) append("\n${chef.specialty}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, null))
}
