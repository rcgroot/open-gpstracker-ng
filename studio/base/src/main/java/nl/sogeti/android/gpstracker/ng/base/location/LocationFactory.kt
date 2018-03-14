package nl.sogeti.android.gpstracker.ng.base.location

interface LocationFactory {

    fun getLocationCoordinates(): LatLng?

    fun getLocationName(): String?

}