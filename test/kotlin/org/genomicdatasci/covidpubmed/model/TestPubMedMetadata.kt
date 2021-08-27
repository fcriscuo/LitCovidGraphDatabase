/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.model

import org.genomicdatasci.covidpubmed.lib.CsvRecordStreamSupplier
import java.nio.file.Paths
import kotlin.streams.asSequence


fun metadataSequence(): Sequence<PubMedMetadata>  {
    val supplier = CsvRecordStreamSupplier(Paths.get("./data/metadata_sample.csv"))
    val metadata = supplier.get()
        .map { PubMedMetadata.parseCSVRecord(it) }
    return metadata.asSequence()
}

    fun main() {
        metadataSequence().take(100).forEach {  println("PubMed Id: ${it.pubmedId}   URL: ${it.url}") }

//        val supplier = CsvRecordStreamSupplier(Paths.get("./data/metadata_sample.csv"))
//        supplier.get()
//            .map { PubMedMetadata.parseCSVRecord(it) }
//            .limit(100)
//            .forEach { println("PubMed Id: ${it.pubmedId}   URL: ${it.url}") }
    }
