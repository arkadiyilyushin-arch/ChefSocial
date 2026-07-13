package com.chefsocial.model

enum class NewsType(val id: String) {
    ALL("all"),
    ANNOUNCEMENT("announcement"),
    EVENT("event"),
    TIPS("tips"),
    UPDATE("update"),
    GENERAL("general"),
    ;

    companion object {
        fun fromId(id: String): NewsType =
            entries.find { it.id == id } ?: GENERAL

        val publishable: List<NewsType> =
            entries.filter { it != ALL }
    }
}
