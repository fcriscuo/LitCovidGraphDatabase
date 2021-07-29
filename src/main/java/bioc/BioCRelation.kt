package bioc

import java.util.ArrayList
import java.util.HashMap

/**
 * Relationship between multiple [BioCAnnotation]s and possibly other
 * `BioCRelation`s.
 */
class BioCRelation : Iterable<BioCNode?> {
    /**
     * Used to refer to this relation in other relationships.
     */
    var iD: String

    /**
     * Information of relation. Implemented examples include abbreviation long
     * forms and short forms and protein events.
     */
   var infons: MutableMap<String, String>

    /**
     * Describes how the referenced annotated object or other relation
     * participates in the current relationship.
     */
    var nodes: MutableList<BioCNode>

    constructor() {
        iD = ""
        infons = HashMap()
        nodes = ArrayList()
    }

    constructor(relation: BioCRelation) {
        iD = relation.iD
        infons = HashMap(relation.infons)
        nodes = ArrayList(relation.nodes)
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

    fun addNode(node: BioCNode) {
        nodes.add(node)
    }

    fun addNode(refId: String, role: String) {
        addNode(BioCNode(refId, role))
    }

    override fun toString(): String {
        var s = "id: " + iD
        s += "\n"
        s += "infons: $infons"
        s += "\n"
        s += "nodes: $nodes"
        s += "\n"
        return s
    }

    override fun iterator(): MutableIterator<BioCNode> {
        return nodes.iterator()
    }
}