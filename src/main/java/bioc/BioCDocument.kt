package bioc

import java.util.ArrayList
import java.util.HashMap

/**
 * Each `BioCDocument` in the [BioCCollection].
 *
 * An id, typically from the original corpus, identifies the particular
 * document. It includes [BioCPassage]s in the document and
 * possibly [BioCRelation]s over annotations on the document.
 */
class BioCDocument : Iterable<BioCPassage?> {
    /**
     * @return the id
     */
    /**
     * @param id the id to set
     */
    /**
     * Id to identify the particular `Document`.
     */
    var iD: String
    var infons: MutableMap<String, String>

    /**
     * List of passages that comprise the document.
     *
     * For PubMed references, they might be "title" and "abstract". For full text
     * papers, they might be Introduction, Methods, Results, and Conclusions. Or
     * they might be paragraphs.
     */
    var passages: MutableList<BioCPassage>

    /**
     * Relations between the annotations and possibly other relations on the text
     * of the document.
     */
    var relations: MutableList<BioCRelation>

    constructor() {
        iD = ""
        infons = HashMap()
        passages = ArrayList()
        relations = ArrayList()
    }

    constructor(document: BioCDocument) {
        iD = document.iD
        infons = HashMap(document.infons)
        passages = ArrayList(document.passages)
        relations = ArrayList()
        for (rel in document.relations) {
            relations.add(BioCRelation(rel))
        }
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

    fun clearPassages() {
        passages.clear()
    }

    val size: Int
        get() = passages.size

    fun getPassage(index: Int): BioCPassage {
        return passages[index]
    }

    /**
     * @param passage the passage to add
     */
    fun addPassage(passage: BioCPassage) {
        passages.add(passage)
    }

    fun removePassage(passage: BioCPassage) {
        passages.remove(passage)
    }

    override fun iterator(): MutableIterator<BioCPassage> {
        return passages.iterator()
    }



    fun clearRelations() {
        relations.clear()
    }

    fun getRelation(index: Int): BioCRelation {
        return relations[index]
    }

    fun addRelation(relation: BioCRelation) {
        relations.add(relation)
    }

    fun removeRelation(relation: BioCRelation) {
        relations.remove(relation)
    }

    /**
     * @return iterator over relations
     */
    fun relationIterator(): Iterator<BioCRelation> {
        return relations.iterator()
    }

    override fun toString(): String {
        var s = "id: " + iD
        s += "\n"
        s += "infon: $infons"
        s += "\n"
        s += passages
        s += "\n"
        s += relations
        s += "\n"
        return s
    }
}