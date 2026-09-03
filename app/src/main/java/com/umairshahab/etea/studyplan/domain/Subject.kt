package com.umairshahab.etea.studyplan.domain

enum class Subject(val displayName: String) {
    Maths("Maths"),
    Physics("Physics"),
    Chemistry("Chemistry"),
    English("English");

    companion object {
        fun fromName(name: String): Subject {
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
            } ?: Maths
        }
    }
}
