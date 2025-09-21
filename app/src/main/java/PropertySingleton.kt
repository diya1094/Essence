import android.net.Uri

object PropertySingleton {
    var imageUris: ArrayList<Uri> = ArrayList()
    var identityDocUri: Uri? = null
    var addressDocUri: Uri? = null

    var titleDeedUri: Uri? = null
    var nonDisputeUri: Uri? = null
    var encumbranceUri: Uri? = null
    var propertyTaxUri: Uri? = null
    var mutationUri: Uri? = null
    var possessionUri: Uri? = null
    var nocUri: Uri? = null
    var utilityBillUri: Uri? = null
}
