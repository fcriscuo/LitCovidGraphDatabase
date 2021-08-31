/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.dao

import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.lib.modifyInternalQuotes
import org.genomicdatasci.covidpubmed.lib.processNodeLabels
import org.genomicdatasci.covidpubmed.model.LitCovidAnnotation
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService

class AnnotationDao (val annotation: LitCovidAnnotation) {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    private val mergeTemplate = "MERGE (a: Annotation {id: ANNID}) " +
            "SET a.type =  \"TYPE\", a.identifier = \"IDENTIFIER\", " +
            " a.text = \"TEXT\" RETURN a.id"

    private fun generateCypherMergeCommand(): String =
        mergeTemplate.replace("ANNID", annotation.id.toString())
            .replace("TYPE", annotation.type)
            .replace("IDENTIFIER", annotation.identifier)
            .replace("TEXT", modifyInternalQuotes(annotation.text))

    private fun mergeAnnotation():String {
        val mergeCypher = generateCypherMergeCommand()
        // display generated Cypher during development
        // TODO: remove logging stmt
       // logger.atInfo().log(mergeCypher)
        return Neo4jConnectionService.executeCypherCommand(mergeCypher).toString()
    }
    private fun setLabels():String {
        // confirm that labels are novel
        val labelExistsQueryTemplate = "MATCH (a:Annotation{id: ${annotation.id.toString()} })" +
                "RETURN apoc.label.exists(a, \"LABEL\") AS output;"
        val novelLabels = annotation.labels.map{
             labelExistsQueryTemplate.replace("LABEL", it) }
            .filter { Neo4jConnectionService.executeCypherCommand(it) == "false" }
        if(novelLabels.isNotEmpty()) {
            val labels = processNodeLabels(novelLabels)
            val setLabelsCypher = "MATCH (a:Annotation{id: ${annotation.id.toString()} })" +
                    " SET a:${labels} RETURN labels(a) AS labels"
           // logger.atInfo().log(setLabelsCypher)
            return Neo4jConnectionService.executeCypherCommand(setLabelsCypher)
        }
       return ""
    }
    /*
   Define the neo4j Relationship between the PubMedArticle and the Annotation nodes
    */
    private fun setRelationshipToPubMedArticle(): String {
        val relationshipCypher = "MATCH (pma:PubMedArticle), (a:Annotation) WHERE " +
                "pma.pubmed_id = ${annotation.pubmedId} AND a.id = ${annotation.id} " +
                "MERGE (pma) - [r:HAS_ANNOTATION] -> (a) RETURN r"
        //logger.atInfo().log(relationshipCypher)
        return Neo4jConnectionService.executeCypherCommand(relationshipCypher)
    }

    fun persistAnnotation () = run {
        if(annotation.isValid()) {
            val mergeResult = mergeAnnotation()
            logger.atInfo().log("Merge completed for Annotation: $mergeResult")
            val setResult = setLabels()
            logger.atInfo().log("Labels for Annotation $annotation = $setResult")
            val relResult = setRelationshipToPubMedArticle()
            logger.atInfo().log(
                "Relationship from PubMedArticle ${annotation.pubmedId} to Annotation " +
                        " ${annotation.id} = $relResult"
            )
        }
    }
}