package bioc

import java.util.ArrayList
import java.util.HashMap

/**
 * One passage in a [BioCDocument].
 *
 * This might be the `text` in the passage and possibly
 * [BioCAnnotation]s over that text. It could be the
 * [BioCSentence]s in the passage. In either case it might include
 * [BioCRelation]s over annotations on the passage.
 */
class BioCPassage : Iterable<BioCSentence?> {
    /**
     * @return the offset
     */
    /**
     * @param offset the offset to set
     */
    /**
     * The offset of the passage in the parent document. The significance of the
     * exact value may depend on the source corpus. They should be sequential and
     * identify the passage's position in the document. Since pubmed is extracted
     * from an XML file, the title has an offset of zero, while the abstract is
     * assumed to begin after the title and one space.
     */
    var offset: Int
    /**
     * @return the text
     */
    /**
     * @param text the text to set
     */
    /**
     * The original text of the passage.
     */
    var text: String

    /**
     * Information of text in the passage.
     *
     * For PubMed references, it might be "title" or "abstract". For full text
     * papers, it might be Introduction, Methods, Results, or Conclusions. Or
     * they might be paragraphs.
     */
   var infons: MutableMap<String, String>

    /**
     * The sentences of the passage.
     */
    var sentences: MutableList<BioCSentence>

    /**
     * Annotations on the text of the passage.
     */
   var annotations: MutableList<BioCAnnotation>

    /**
     * Relations between the annotations and possibly other relations on the text
     * of the passage.
     */
    var relations: MutableList<BioCRelation>

    constructor() {
        offset = -1
        text = ""
        infons = HashMap()
        sentences = ArrayList()
        annotations = ArrayList()
        relations = ArrayList()
    }

    constructor(passage: BioCPassage) {
        offset = passage.offset
        text = passage.text
        infons = HashMap(passage.infons)
        sentences = ArrayList()
        for (sen in passage.sentences) {
            sentences.add(BioCSentence(sen))
        }
        annotations = ArrayList()
        for (ann in passage.annotations) {
            annotations.add(BioCAnnotation(ann))
        }
        relations = ArrayList()
        for (rel in passage.relations) {
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



    fun getSentence(index: Int): BioCSentence {
        return sentences[index]
    }

    fun removeSentence(sentence: BioCSentence) {
        sentences.remove(sentence)
    }

    fun removeSentence(index: Int) {
        sentences.removeAt(index)
    }

    fun clearSentences() {
        sentences.clear()
    }

    /**
     * @param sentence the sentence to add
     */
    fun addSentence(sentence: BioCSentence) {
        sentences.add(sentence)
    }

    override fun iterator(): MutableIterator<BioCSentence> {
        return sentences.iterator()
    }

    override fun toString(): String {
        var s = "infons: $infons"
        s += "\n"
        s += "offset: $offset"
        s += "\n"
        s += text
        s += "\n"
        s += sentences
        s += "\n"
        s += annotations
        s += "\n"
        s += relations
        s += "\n"
        return s
    }
}