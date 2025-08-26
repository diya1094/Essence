package com.example.essence

// Data class representing a property
data class Property(
    var propertyId: String = "",
    var title: String = "",
    var description: String = "",
    var price: String = "",
    var location: String = "",
    var sellerId: String = "",
    var sellerName: String = "",
    var imageUrl: String = "",
    var status: String = "pending"   // default until admin approves
)
