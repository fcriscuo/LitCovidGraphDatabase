/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.lib

import arrow.core.Either
import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.model.PubMedArticle
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


/*
This function is prone to Exceptions due to invalid input data
 */
fun pubMedNodeExistsPredicate (pubmedId:String): Boolean {
    val cypher = "OPTIONAL MATCH (p:PubMedArticle{pubmed_id: $pubmedId }) " +
            " RETURN p IS NOT NULL AS Predicate"
    try {
        val predicate = Neo4jConnectionService.executeCypherCommand(cypher)
        when (predicate.lowercase(Locale.getDefault())) {
            "true" -> return true
             "false" -> return false
        }
    } catch (e: Exception) {
        org.genomicdatasci.covidpubmed.neo4j.logger.atSevere().log(e.message.toString())
        return false
    }
    return false
}

//fun retrievePubMedArticleByPubMedId(pubmedId: String): Either<Exception, PubMedArticle >{
//    val query = "MATCH (pma:PubMedArticle {pubmed_id: $pubmedId} return pma"
//    if (pubMedNodeExistsPredicate(pubmedId)) {
//        val article = Neo4jConnectionService.executeCypherCommand(query)
//    }
//
//}

/*
stand-alone testing
 */
fun main() {
    val count = countPubMedArticleNodes()
    println(count)
}