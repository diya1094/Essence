package com.example.essence

data class Property(
    var propertyId: String = "",
    var description: String = "",
    var price: String = "",
    var address: String = "",
    var yearBuilt: String = "",
    var propertyType: String = "",
    var status: String = "pending",
    var userId: String = "",

    var sellerName: String = "",
    var sellerEmail: String = ""
)
