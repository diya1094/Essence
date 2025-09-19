package com.example.essence

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Property(
    var title: String = "",
    var propertyId: String = "",
    var description: String = "",
    var price: String = "",
    var address: String = "",
    var yearBuilt: String = "",
    var propertyType: String = "",
    var status: String = "pending",
    var userId: String = "",
    var sellerName: String = "",
    var sellerEmail: String = "",
    var jointOwners: List<Owner> = emptyList(),

    var adminMessage: String? = null,

    var showApproveButton: Boolean = true,
    var showRejectButton: Boolean = true,
    var showRequestChangeButton: Boolean = true
) : Parcelable

@Parcelize
data class Owner(
    var name: String = "",
    var email: String = "",
    var phone: String = ""
) : Parcelable
