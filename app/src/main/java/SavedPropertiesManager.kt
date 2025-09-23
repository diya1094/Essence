package com.example.essence

import android.content.Context

object SavedPropertiesManager {
    private const val PREFS_NAME = "essence_prefs"
    private const val SAVED_KEY = "saved_property_ids"

    // Save property ID persistently
    fun addProperty(context: Context, property: Property) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedIds = prefs.getStringSet(SAVED_KEY, mutableSetOf()) ?: mutableSetOf()
        savedIds.add(property.propertyId)
        prefs.edit().putStringSet(SAVED_KEY, savedIds).apply()
    }

    // Remove property ID persistently
    fun removeProperty(context: Context, property: Property) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedIds = prefs.getStringSet(SAVED_KEY, mutableSetOf()) ?: mutableSetOf()
        savedIds.remove(property.propertyId)
        prefs.edit().putStringSet(SAVED_KEY, savedIds).apply()
    }

    // Get saved IDs from persistent storage
    fun getSavedPropertyIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(SAVED_KEY, mutableSetOf()) ?: mutableSetOf()
    }

    // Build saved property objects from main source (call this in SavedActivity)
    fun getSavedProperties(context: Context, allProperties: List<Property>): List<Property> {
        val savedIds = getSavedPropertyIds(context)
        return allProperties.filter { it.propertyId in savedIds }
    }
}
