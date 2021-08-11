/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.lib

import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService

/*
Function to determine the number of PubMedArticle nodes in the database
The intent is to be able to skip that number of document elements in the
BioC input if an interrupted loading is restarted using the same input file.
 */

fun countPubMedArticleNodes():Int {
    val cypher = "MATCH (p:PubMedArticle) RETURN COUNT(p)"
    return Neo4jConnectionService.executeCypherCommand(cypher).toInt()
}

/*
stand-alone testing
 */
fun main() {
    val count = countPubMedArticleNodes()
    println(count)
}