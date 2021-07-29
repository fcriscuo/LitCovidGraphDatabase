package bioc

import java.util.ArrayList
import java.util.HashMap

/**
 * One sentence in a [BioCPassage].
 *
 * It may contain the original text of the sentence or it might be
 * [BioCAnnotation]s and possibly [BioCRelation]s on the text of the
 * passage.
 *
 * There is no code to keep those possibilities mutually exclusive. However the
 * currently available DTDs only describe the listed possibilities
 */
class BioCSentence {
    /**
     * A [BioCDocument] offset to where the sentence begins in the
     * [BioCPassage]. This value is the sum of the passage offset and the local
     * offset within the passage.
     */
    var offset: Int

    /**
     * The original text of the sentence.
     */
    var text: String
    var infons: MutableMap<String, String>? = null

    /**
     * [BioCAnnotation]s on the original text
     */
   var annotations: MutableList<BioCAnnotation>

    /**
     * Relations between the annotations and possibly other relations on the text
     * of the sentence.
     */
     var relations: MutableList<BioCRelation>

    constructor() {
        offset = -1
        text = ""
        infons = HashMap()
        annotations = ArrayList()
        relations = ArrayList()
    }

    constructor(sentence: BioCSentence) {
        offset = sentence.offset
        text = sentence.text
        annotations = ArrayList()
        for (ann in sentence.annotations) {
            annotations.add(BioCAnnotation(ann))
        }
        relations = ArrayList()
        for (rel in sentence.relations) {
            relations.add(BioCRelation(rel))
        }
    }

    fun clearInfons() {
        infons!!.clear()
    }

    fun getInfon(key: String): String? {
        return infons!![key]
    }

    fun putInfon(key: String, value: String) {
        infons!![key] = value
    }

    fun removeInfon(key: String) {
        infons!!.remove(key)
    }

    fun clearAnnotations() {
        annotations.clear()
    }

    fun getAnnotation(index: Int): BioCAnnotation {
        return annotations[index]
    }

    fun addAnnotation(annotation: BioCAnnotation) {
        annotations.add(annotation)
    }

    fun removeAnnotation(annotation: BioCAnnotation) {
        annotations.remove(annotation)
    }

    fun removeAnnotation(index: Int) {
        annotations.removeAt(index)
    }

    /**
     * @return iterator over annotations
     */
    fun annotationIterator(): Iterator<BioCAnnotation> {
        return annotations.iterator()
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

    fun removeRelation(index: Int) {
        relations.removeAt(index)
    }

    /**
     * @return iterator over relations
     */
    fun relationIterator(): Iterator<BioCRelation> {
        return relations.iterator()
    }

    override fun toString(): String {
        var s = "offset: $offset"
        s += "\n"
        s = "infons: $infons"
        s += "\n"
        s += "text: $text"
        s += "\n"
        s += annotations
        s += "\n"
        s += relations
        s += "\n"
        return s
    }
}