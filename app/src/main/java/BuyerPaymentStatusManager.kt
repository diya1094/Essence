package com.example.essence

import android.content.Context

object BuyerPaymentStatusManager {
    private const val PREFS_NAME = "buyer_payments"
    private const val STATUS_KEY_PREFIX = "paid_property_"

    fun setPaymentDone(context: Context, propertyId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(STATUS_KEY_PREFIX + propertyId, true)
            .apply()
    }

    fun isPaymentDone(context: Context, propertyId: String): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(STATUS_KEY_PREFIX + propertyId, false)
    }
}
