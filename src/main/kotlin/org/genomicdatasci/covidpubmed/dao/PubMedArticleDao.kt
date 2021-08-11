/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.dao

import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.lib.modifyInternalQuotes
import org.genomicdatasci.covidpubmed.model.PubMedArticle
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService

/*
Class is responsible for all database functions regarding PubMedArticle nodes
 */
class PubMedArticleDao (val article: PubMedArticle) {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();
     private val mergeTemplate = "MERGE (pma:PubMedArticle { pubmed_id: PMAID," +
             "pmc_id: \"PMCID\", doiid: \"DOIID\", article_title: \"TITLE\", " +
        "abstract: \"ABSTRACT\" }) RETURN pma.pubmed_id"




    private fun generateCypherMergeCommand(): String =
        mergeTemplate.replace("PMAID", article.pubmedId)
            .replace("PMCID", article.pmcId)
            .replace("DOIID", article.doiId)
            .replace("TITLE", modifyInternalQuotes(article.articleTitle))
            .replace("ABSTRACT", modifyInternalQuotes(article.abstract))

   private fun mergePubMedArticle():String {
        val mergeCypher = generateCypherMergeCommand()
        // display generated Cypher during development
        // TODO: remove logging stmt
        logger.atInfo().log(mergeCypher)
        return Neo4jConnectionService.executeCypherCommand(mergeCypher)
    }

    private fun setLabels():String {
        val labels = article.labels.joinToString (separator = ":")
        val setLabelsCypher = "MATCH (p:PubMedArticle{pubmed_id: ${article.pubmedId} })" +
                " SET p:${labels} RETURN labels(p) AS labels"
        logger.atInfo().log(setLabelsCypher)
        return Neo4jConnectionService.executeCypherCommand(setLabelsCypher)
    }

    fun persistPubMedArticle () = run {
        val mergeResult = mergePubMedArticle()
        logger.atInfo().log("Merge completed for PubMed Id: $mergeResult")
        val setResult = setLabels()
        logger.atInfo().log("Labels for PubMedId: ${article.pubmedId} = $setResult")
    }

}