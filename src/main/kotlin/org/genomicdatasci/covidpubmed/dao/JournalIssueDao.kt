/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.dao

import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.lib.processNodeLabels
import org.genomicdatasci.covidpubmed.model.JournalIssue
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService


class JournalIssueDao (private val journalIssue: JournalIssue): LitCovidDao() {

    private val mergeTemplate = "MERGE (ji:JournalIssue { id: JIID}) " +
            " SET ji.doiid = \"DOIID\", ji.journal_name = \"NAME\", " +
            "ji.journal_issue = \"ISSUE\"  RETURN ji.id"

    private fun generateCypherMergeCommand(): String =
        mergeTemplate.replace("JIID", journalIssue.id.toString())
            .replace("DOIID", journalIssue.doiId)
            .replace("NAME",journalIssue.journalName)
            .replace("ISSUE", journalIssue.journalIssue)

    private fun setLabels():String {
        val labels = processNodeLabels(journalIssue.labels)
        val setLabelsCypher = "MATCH (ji:JournalIssue{id: ${journalIssue.id.toString()} })" +
                " SET ji:${labels} RETURN labels(ji) AS labels"
        logger.atInfo().log(setLabelsCypher)
        return Neo4jConnectionService.executeCypherCommand(setLabelsCypher)
    }

    /*
    Define the neo4j Relationship between the PubMedArticle and the JournalIssue nodes
     */
    private fun setRelationshipToPubMedArticle(): String {
        val relationshipCypher = "MATCH (pma:PubMedArticle), (ji:JournalIssue) WHERE " +
                "pma.pubmed_id = ${journalIssue.pubmedId} AND ji.id = ${journalIssue.id} " +
                "MERGE (pma) - [r:HAS_JOURNAL_ISSUE] -> (ji) RETURN r"
        logger.atInfo().log(relationshipCypher)
        return Neo4jConnectionService.executeCypherCommand(relationshipCypher)
    }

    fun persistJournalIssue () = run {
        if(journalIssue.isValid()) {
            val mergeResult = executeMergeCommand(generateCypherMergeCommand())
          //  logger.atInfo().log("Merge completed for Journal Issue: $mergeResult")
            //val setResult = setLabels()
           // logger.atInfo().log("Labels for JournalIssue ${journalIssue.id} = $setResult")
            val relResult = setRelationshipToPubMedArticle()
            logger.atInfo().log(
                "Relationship from PubMedArticle ${journalIssue.pubmedId} to JournalIssue " +
                        " ${journalIssue.id} = $relResult"
            )
        }
    }
}