package com.example.essence

object SavedPropertiesManager {
    private val savedList = mutableListOf<Property>()

    fun addProperty(property: Property) {
        if (!savedList.contains(property)) {
            savedList.add(property)
        }
    }

    fun getSavedProperties(): List<Property> {
        return savedList
    }
}
