package bioc

/**
 * The connection to the original text can be made through the `offset`,
 * `length`, and possibly the `text` fields.
 */
class BioCLocation {
    /**
     * Type of annotation. Options include "token", "noun phrase", "gene", and
     * "disease". The valid values should be described in the `key` file.
     */
    var offset = 0

    /**
     * The length of the annotated text. While unlikely, this could be zero to
     * describe an annotation that belongs between two characters.
     */
    var length = 0

    constructor() {}
    constructor(location: BioCLocation) : this(location.offset, location.length) {}
    constructor(offset: Int, length: Int) {
        this.offset = offset
        this.length = length
    }

    override fun toString(): String {
        return "$offset:$length"
    }
}