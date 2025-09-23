package com.example.essence

import android.net.Uri

object PropertySingleton {
    var imageUris: ArrayList<Uri> = ArrayList()
    var identityDocUri: Uri? = null
    var addressDocUri: Uri? = null
    var editPropertyId: String? = null

    var titleDeedUri: Uri? = null
    var nonDisputeUri: Uri? = null
    var encumbranceUri: Uri? = null
    var propertyTaxUri: Uri? = null
    var mutationUri: Uri? = null
    var possessionUri: Uri? = null
    var nocUri: Uri? = null
    var utilityBillUri: Uri? = null

    // ADDED property details fields:
    var title: String? = null
    var description: String? = null
    var price: String? = null
    var address: String? = null
    var yearBuilt: String? = null
    var propertyType: String? = null
    var propertySize: String? = null
    var jointOwners: Any? = null // List<Map<...>> or whatever you use for joint owners
}
