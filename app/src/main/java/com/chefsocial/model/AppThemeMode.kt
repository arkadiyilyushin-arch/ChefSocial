package com.chefsocial.model

enum class AppThemeMode(val id: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system"),
    ;

    companion object {
        fun fromId(id: String): AppThemeMode =
            entries.find { it.id == id } ?: SYSTEM
    }
}
