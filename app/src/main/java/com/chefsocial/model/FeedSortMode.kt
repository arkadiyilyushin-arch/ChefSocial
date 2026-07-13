package com.chefsocial.model

enum class FeedSortMode(val id: String) {
    NEWEST("newest"),
    POPULAR("popular"),
    ;

    companion object {
        fun fromId(id: String): FeedSortMode =
            entries.find { it.id == id } ?: NEWEST
    }
}
