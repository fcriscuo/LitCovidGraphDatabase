/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.app

import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.lib.CsvRecordStreamSupplier
import org.genomicdatasci.covidpubmed.lib.pubMedNodeExistsPredicate
import org.genomicdatasci.covidpubmed.model.PubMedMetadata
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService
import java.nio.file.Paths
import kotlin.streams.asSequence

class MetadataLoader {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass();

    private val updatePubMedArticleCommand = "MATCH (pma: PubMedArticle{pubmed_id: PUBMEDID} ) " +
            " SET pma.pmc_id = \"PMCID\", pma.doiid = \"DOIID\", pma.url = \"URL\" , pma.journal = \"JOURNAL\" " +
            " RETURN pma.id"

    fun generateMetadataSequence(filename: String): Sequence<PubMedMetadata> {
        val supplier = CsvRecordStreamSupplier(Paths.get(filename))
        val metadata = supplier.get()
            .map { PubMedMetadata.parseCSVRecord(it) }
        return metadata.asSequence()
    }

    fun updatePubMedArticle(metadataSequence: Sequence<PubMedMetadata>) {
        var count = 0
        metadataSequence
            .filter { it.pubmedId.isNotEmpty() }
            .filter { pubMedNodeExistsPredicate(it.pubmedId) }
            .forEach { meta ->
                val command = updatePubMedArticleCommand.replace("PUBMEDID", meta.pubmedId)
                    .replace("PMCID", meta.pmcid)
                    .replace("DOIID", meta.doi)
                    .replace("URL", meta.url)
                    .replace("JOURNAL", meta.journal)
                logger.atInfo().log("+++Cypher command: $command")
                count += 1
                val result = Neo4jConnectionService.executeCypherCommand(command)
            }
        logger.atInfo().log("Updated $count PubMedArticle nodes")
    }
}

fun main() {
    val loader = MetadataLoader()
    val sequence = loader.generateMetadataSequence("./data/metadata.csv")
    loader.updatePubMedArticle(sequence)
}