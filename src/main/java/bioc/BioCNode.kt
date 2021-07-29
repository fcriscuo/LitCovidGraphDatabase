package bioc

class BioCNode {
    /**
     * Id of an annotated object or another relation. Typically there will be one
     * label for each ref_id.
     */
    var refid: String
    var role: String

    constructor() {
        refid = ""
        role = ""
    }

    constructor(node: BioCNode) {
        refid = node.refid
        role = node.role
    }

    constructor(refid: String, role: String) {
        this.refid = refid
        this.role = role
    }

    override fun toString(): String {
        var s = "refid: $refid"
        s += "\n"
        s += "role: $role"
        s += "\n"
        return s
    }
}