package bioc

import java.util.ArrayList
import java.util.HashMap

/**
 * Collection of documents.
 *
 * Collection of documents for a project. They may be an entire corpus or some
 * portion of a corpus. Fields are provided to describe the collection.
 *
 * Documents may appear empty if doing document at a time IO.
 */
class BioCCollection : Iterable<BioCDocument?> {
    /**
     * @return the source
     */
    /**
     * @param source the source to set
     */
    /**
     * Describe the original source of the documents.
     */
    var source: String
    /**
     * @return the date
     */
    /**
     * @param date the date to set
     */
    /**
     * Date the documents obtained from the source.
     */
    var date: String
    /**
     * @return the key
     */
    /**
     * @param key the key to set
     */
    /**
     * Name of a file describing the contents and conventions used in this XML
     * file.
     */
    var key: String
     var infons: MutableMap<String, String>

    /**
     * All the documents in the collection. This will be empty if document at a
     * time IO is used to read the XML file. Any contents will be ignored if
     * written with document at a time IO.
     */
   var documents: MutableList<BioCDocument>

    constructor() {
        source = ""
        date = ""
        key = ""
        infons = HashMap()
        documents = ArrayList()
    }

    constructor(collection: BioCCollection) {
        date = collection.date
        source = collection.source
        key = collection.key
        infons = HashMap(collection.infons)
        documents = ArrayList(collection.documents)
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

    fun clearDocuments() {
        documents.clear()
    }

    val size: Int
        get() = documents.size

    fun getDocument(index: Int): BioCDocument {
        return documents[index]
    }

    /**
     * @param document the document to add
     */
    fun addDocument(document: BioCDocument) {
        documents.add(document)
    }

    fun removeDocument(document: BioCDocument) {
        documents.remove(document)
    }

    override fun toString(): String {
        var s = "source: $source"
        s += "\n"
        s += "date: $date"
        s += "\n"
        s += "key: $key"
        s += "\n"
        s += infons
        s += "\n"
        s += documents
        s += "\n"
        return s
    }

    override fun iterator(): MutableIterator<BioCDocument> {
        return documents.iterator()
    }
}