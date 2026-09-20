package com.umairshahab.etea.studyplan.domain

enum class Subject(val displayName: String) {
    Maths("Maths"),
    Physics("Physics"),
    Chemistry("Chemistry"),
    English("English");

    companion object {
        fun fromNameOrNull(name: String?): Subject? {
            if (name == null) return null
            return entries.firstOrNull {
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
            }
        }

        fun fromName(name: String): Subject {
            return fromNameOrNull(name) ?: Maths
        }
    }
}
