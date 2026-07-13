package com.chefsocial.model

enum class ProfileVisibility(val id: String) {
    PUBLIC("public"),
    FOLLOWERS_ONLY("followers"),
    ;

    companion object {
        fun fromId(id: String): ProfileVisibility =
            entries.find { it.id == id } ?: PUBLIC
    }
}
