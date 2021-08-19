/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.dao

import org.genomicdatasci.covidpubmed.lib.processNodeLabels
import org.genomicdatasci.covidpubmed.model.Author
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService

class AuthorDao(private val author: Author): LitCovidDao(){
    /*
    val labels: List<String>,
    val pubmedId: String,
    val surname: String,
    val givenName: String = "",
    val id: Int
     */

    private val mergeTemplate = "MERGE (au:Author { id: AUID}) " +
            " SET au.surname = \"SURNAME\", au.given_name = \"FIRST_NAME\" " +
            " RETURN au.id"

    private fun generateCypherMergeCommand(): String =
        mergeTemplate.replace("AUID", author.id.toString())
            .replace("SURNAME", author.surname)
            .replace("FIRST_NAME",author.givenName)

    /*
    Use the surname as an auxiliary label
     */
    private fun setLabels():String {
        val labels = processNodeLabels(author.labels)
        val setLabelsCypher = "MATCH (au:Author{id: ${author.id.toString()} })" +
                " SET au:${labels} RETURN labels(au) AS labels"
        logger.atInfo().log(setLabelsCypher)
        return Neo4jConnectionService.executeCypherCommand(setLabelsCypher)
    }

    /*
   Define the neo4j Relationship between the PubMedArticle and the Author nodes
    */
    private fun setRelationshipToPubMedArticle(): String {
        val relationshipCypher = "MATCH (pma:PubMedArticle), (au:Author) WHERE " +
                "pma.pubmed_id = ${author.pubmedId} AND au.id = ${author.id} " +
                "MERGE (pma) - [r:HAS_AUTHOR] -> (au) RETURN r"
        logger.atInfo().log(relationshipCypher)
        return Neo4jConnectionService.executeCypherCommand(relationshipCypher)
    }

    fun persistAuthor() = run {
        if(author.isValid()) {
            val mergeResult = executeMergeCommand(generateCypherMergeCommand())
            logger.atInfo().log("Merge completed for Author: $mergeResult")
            val setResult = setLabels()
            logger.atInfo().log("Labels for Author ${author.id} = $setResult")
            val relResult = setRelationshipToPubMedArticle()
            logger.atInfo().log(
                "Relationship from PubMedArticle ${author.pubmedId} to author " +
                        " ${author.id} = $relResult"
            )
        }
    }
}