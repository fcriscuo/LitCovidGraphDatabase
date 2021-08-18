/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.lib

import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService
import java.util.*

/*
Function to determine the number of PubMedArticle nodes in the database
The intent is to be able to skip that number of document elements in the
BioC input if an interrupted loading is restarted using the same input file.
 */

fun countPubMedArticleNodes():Int {
    val cypher = "MATCH (p:PubMedArticle) RETURN COUNT(p)"
    return Neo4jConnectionService.executeCypherCommand(cypher).toInt()
}

fun pubMedNodeExistsPredicate (pubmedId:String): Boolean {
    val cypher = "OPTIONAL MATCH (p:PubMedArticle{pubmed_id: $pubmedId }) " +
            " RETURN p IS NOT NULL AS Predicate"
    val predicate = Neo4jConnectionService.executeCypherCommand(cypher)
    when (predicate.lowercase(Locale.getDefault())) {
        "true" -> return true
         "false" -> return false
    }
    return false
}

/*
stand-alone testing
 */
fun main() {
    val count = countPubMedArticleNodes()
    println(count)
}