/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.app

import arrow.core.Either
import bioc.BioCDocument
import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.dao.PubMedArticleDao
import org.genomicdatasci.covidpubmed.io.BioCDocumentSupplier
import org.genomicdatasci.covidpubmed.lib.processBioCDocument
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
                    logger.atWarning().log(" ${retEither.value.message}")
                    logger.atWarning().log("Document count = $count")
                    exitProcess(0)
                }
            }
        }
    }


    /*
    If the document contains a passage with basic PubMedArticle properties
    map those data to a PubMedArticle object and load it into the database
     */
    private fun loadPubMedArticle(document: BioCDocument) {
        processBioCDocument(document)?.
        let{ PubMedArticleDao(it) }?.persistPubMedArticle()
    }
}

fun main(args: Array<String>) {
    val filename = if (args.size > 0) args[0] else "data/xml/sample_litcovid2pubtator.xml"
    LitCovidDatabaseLoader().processBioCFileForPubMedArticles(filename)
}