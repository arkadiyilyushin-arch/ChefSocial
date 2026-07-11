package com.chefsocial.model

enum class RecipeCategory(val id: String) {
    ALL("all"),
    BAKING("baking"),
    ITALIAN("italian"),
    HEALTHY("healthy"),
    GRILL("grill"),
    HOME("home"),
    ;

    companion object {
        fun fromId(id: String): RecipeCategory =
            entries.find { it.id == id } ?: HOME
    }
}
