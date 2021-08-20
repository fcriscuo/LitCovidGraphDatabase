package org.genomicdatasci.covidpubmed.lib

import org.apache.commons.lang3.StringUtils.replace

/**
 * Created by fcriscuo on 7/28/21.
 */

/*
Function that modifies a collection of node labels
to meet Neo4j requirements
 */
fun processNodeLabels(labels: List<String>): String =
    when (labels.size) {
        0 -> " "
        1 -> labels[0]
        else -> labels.joinToString(separator = ":")
            .trim()
            .replace(" ", "_")
    }


/*
Double quotes (i.e. ") inside a text field causes Cypher
processing errors
 */
fun modifyInternalQuotes(text: String): String =
    text.replace("\"", "'")