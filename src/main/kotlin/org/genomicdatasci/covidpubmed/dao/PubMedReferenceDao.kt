/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.dao

import org.genomicdatasci.covidpubmed.lib.modifyInternalQuotes
import org.genomicdatasci.covidpubmed.lib.processNodeLabels
import org.genomicdatasci.covidpubmed.model.PubMedReference
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService

class PubMedReferenceDao( val reference: PubMedReference): LitCovidDao() {

    private val mergeTemplate = "MERGE (pmr:PubMedArticle { pubmed_id: PMAID}) " +
            "SET  pmr.doiid = \"DOIID\", pmr.article_title = \"TITLE\"" +
            "  RETURN pmr.pubmed_id"

    private fun generateCypherMergeCommand(): String =
        mergeTemplate.replace("PMAID", reference.pubmedId)
            .replace("DOIID", reference.doiId)
            .replace("TITLE", modifyInternalQuotes(reference.articleTitle))

    private fun mergePubMedReference(): String {
        val mergeCypher = generateCypherMergeCommand()
        return Neo4jConnectionService.executeCypherCommand(mergeCypher)
    }

    private fun setLabels():String {
        // confirm that labels are novel
        val labelExistsQueryTemplate = "MATCH (pmr:PubMedArticle{id: ${reference.pubmedId} }) " +
                "RETURN apoc.label.exists(pmr, \"LABEL\") AS output;"
        val novelLabels = reference.labels.map{
            labelExistsQueryTemplate.replace("LABEL", it) }
            .filter { Neo4jConnectionService.executeCypherCommand(it) == "false" }
        if(novelLabels.isNotEmpty()) {
            val labels = processNodeLabels(novelLabels)
            val setLabelsCypher = "MATCH (pmr:PubMedArticle{id: ${reference.pubmedId} })" +
                    " SET pmr:${labels} RETURN labels(pmr) AS labels "
           // logger.atInfo().log(setLabelsCypher)
            return Neo4jConnectionService.executeCypherCommand(setLabelsCypher)
        }
        return ""
    }

    private fun setRelationshipToPubMedArticle(): String {
        val relationshipCypher = "MATCH (pma:PubMedArticle), (pmr:PubMedReference) WHERE " +
                "pma.pubmed_id = ${reference.parentPubMedId} AND pmr.pubmed_id = ${reference.pubmedId} " +
                "MERGE (pma) - [r:HAS_REFERENCE] -> (pmr) RETURN r"
        //logger.atInfo().log(relationshipCypher)
        return Neo4jConnectionService.executeCypherCommand(relationshipCypher)
    }

    fun persistPubMedReference() = run {
        // Load the PubMedArticle node
        val mergeResult = mergePubMedReference()
        logger.atInfo().log("Merge completed for PubMedReference Id: $mergeResult")
        setRelationshipToPubMedArticle()
        val setResult = setLabels()
        logger.atInfo().log("Labels for PubMedId: ${reference.pubmedId} = $setResult")
        /* load the JournalIssue node
         */
        JournalIssueDao(reference.journal).persistJournalIssue()
        /*
        Load the annotations
         */
        reference.annotations.values.filter { it.isValid() }
            .forEach { it -> AnnotationDao(it).persistAnnotation() }
        /*
        Load the authors
         */
        reference.authors.filter { it.isValid() }
            .forEach { it -> AuthorDao(it).persistAuthor() }

    }

}