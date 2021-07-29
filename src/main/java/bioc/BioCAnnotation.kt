package bioc

import java.util.ArrayList
import java.util.HashMap

/**
 * Stand off annotation. The connection to the original text can be made
 * through the `location` and the `text` fields.
 */
open class BioCAnnotation {
    /**
     * Id used to identify this annotation in a [Relation].
     */
    var iD: String
    var infons: MutableMap<String, String>
     var locations: MutableList<BioCLocation>

    /**
     * The annotated text.
     */
    var text: String

    constructor() {
        iD = ""
        infons = HashMap()
        locations = ArrayList()
        text = ""
    }

    constructor(annotation: BioCAnnotation) {
        iD = annotation.iD
        infons = HashMap(annotation.infons)
        locations = ArrayList(annotation.locations)
        text = annotation.text
    }


    fun clearInfons() {
        infons.clear()
    }

    fun getInfon(key: String): String? {
        return infons[key]
    }

    fun putInfon(key: String, value: String) {
        infons[key] = value
    }

    fun removeInfon(key: String) {
        infons.remove(key)
    }

    fun clearLocations() {
        locations.clear()
    }

    fun addLocation(location: BioCLocation) {
        locations.add(location)
    }

    fun setLocation(location: BioCLocation) {
        val locationList = ArrayList<BioCLocation>()
        locationList.add(location)
        locations = locationList
    }

    fun setLocation(offset: Int, length: Int) {
        setLocation(BioCLocation(offset, length))
    }

    override fun toString(): String {
        var s = "id: " + iD
        s += "\n"
        s += infons
        s += "locations: $locations"
        s += "\n"
        s += "text: $text"
        s += "\n"
        return s
    }
}