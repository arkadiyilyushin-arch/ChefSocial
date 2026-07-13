package com.chefsocial.model

enum class MessagePrivacy(val id: String) {
    EVERYONE("everyone"),
    FOLLOWERS_ONLY("followers"),
    ;

    companion object {
        fun fromId(id: String): MessagePrivacy =
            entries.find { it.id == id } ?: EVERYONE
    }
}
