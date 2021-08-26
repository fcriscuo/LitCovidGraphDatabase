/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.app

import arrow.core.Either
import bioc.BioCDocument
import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.dao.PubMedArticleDao
import org.genomicdatasci.covidpubmed.dao.PubMedReferenceDao
import org.genomicdatasci.covidpubmed.io.BioCDocumentSupplier
import org.genomicdatasci.covidpubmed.lib.*
import org.genomicdatasci.covidpubmed.model.PubMedReference
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService
import kotlin.system.exitProcess

/*
Responsible for loading the data from a specified BioC-formatted XML file
into a Neo4j database.
 */
class LitCovidDatabaseLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    /*
    First pass through file is for document passages that contain PubMedArticles
     */
    fun processBioCFileForPubMedArticles(filename: String) {
        val supplier = BioCDocumentSupplier(filename)
        var count = 0
        while (true) {

            when (val retEither = supplier.get()) {
                is Either.Right -> {
                    val document = retEither.value
                    loadPubMedArticle(document)
                    logger.atInfo().log("Loaded document id: ${document.iD}")
                    count += 1
                }
                is Either.Left -> {
                    logger.atInfo().log(" ${retEither.value.message}")
                    logger.atWarning().log("Document count = $count")
                    processBioCFileForPubMedReferences(filename)
                }
            }
        }
    }

    /*
    2nd pass is to process PubMed references
    A separate pass is needed to distinguish PubMed entries that are LitCovid entries
    which are referenced by other LitCovid entries from independent references
    All the LitCovid entries must have been loaded into the database to make that distinction
     */
    fun processBioCFileForPubMedReferences(filename: String) {
        val supplier = BioCDocumentSupplier(filename)
        while (true) {
            when (val retEither = supplier.get()) {
                is Either.Right -> {
                    val document = retEither.value
                    parsePubMedReferences(document)
                }
                is Either.Left -> {
                    logger.atInfo().log("All PubMed References have been processed")
                    exitProcess(0)

                }
            }
        }
    }

    private fun parsePubMedReferences(document: BioCDocument) {
        val pubMedEither = resolvePubMedPassage(document)
        when (pubMedEither) {
            is Either.Right -> {
                val pubmedPassage = pubMedEither.value
                val pubmedId = resolvePubMedId(pubmedPassage)
                val refList = resolveReferenceList(document, pubmedId)
                if (refList.isNotEmpty()) {
                    refList.filter { it.isValid() }
                        .forEach {
                        when (pubMedNodeExistsPredicate(it.pubmedId)) {
                            true -> processInternalReference(it)
                            false -> PubMedReferenceDao(it).persistPubMedReference()
                        }
                    }
                }
            }
            is Either.Left -> {
                println(pubMedEither.value.message)
            }
        }
    }

    /*
    Function to establish HAS_REFERENCE and CITED_BY relationships between two (2)
    PubMedArticle nodes. Add a REFERENCE label to the node representing the reference
     */
    private fun processInternalReference(reference: PubMedReference) {
        val refRelationshipCypher = "MATCH (parent:PubMedArticle), (ref:PubMedArticle) WHERE " +
                "parent.pubmed_id = ${reference.parentPubMedId} AND ref.pubmed_id = ${reference.pubmedId} " +
                "MERGE (parent) - [r:HAS_REFERENCE] -> (ref) RETURN r"
        Neo4jConnectionService.executeCypherCommand(refRelationshipCypher)
        val citedRelationshipCypher = "MATCH (parent:PubMedArticle), (ref:PubMedArticle) WHERE " +
                "parent.pubmed_id = ${reference.parentPubMedId} AND ref.pubmed_id = ${reference.pubmedId} " +
                "MERGE (ref) - [r:CITED_BY] -> (parent) RETURN r"
        Neo4jConnectionService.executeCypherCommand(citedRelationshipCypher)
        // label as REFERENCE if not labeled already
        val labelExistsQuery = "MATCH (pmr:PubMedArticle{id: ${reference.pubmedId} }) " +
                "RETURN apoc.label.exists(pmr, \"REFERENCE\") AS output;"
        if (Neo4jConnectionService.executeCypherCommand(labelExistsQuery) == "false") {
            val setLabelsCypher = "MATCH (pmr:PubMedArticle{id: ${reference.pubmedId} })" +
                    " SET pmr:REFERENCE RETURN labels(pmr) AS labels "
        }
        logger.atInfo().log(
            "Established internal reference relationship between PubMed Id: " +
                    " ${reference.parentPubMedId} and reference PubMed Id: ${reference.pubmedId} "
        )
    }

    /*
    If the document contains a passage with basic PubMedArticle properties
    map those data to a PubMedArticle object and load it into the database
     */
    private fun loadPubMedArticle(document: BioCDocument) {
        processBioCDocument(document)?.let { PubMedArticleDao(it) }?.persistPubMedArticle()
    }
}

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "data/xml/sample_litcovid2pubtator.xml"
    logger.atInfo().log("Processing BioC file: $filename")
    LitCovidDatabaseLoader().processBioCFileForPubMedArticles(filename)
    LitCovidDatabaseLoader().processBioCFileForPubMedReferences(filename)
}