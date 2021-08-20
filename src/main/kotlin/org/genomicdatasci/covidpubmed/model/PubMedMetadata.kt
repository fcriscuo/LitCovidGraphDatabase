/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.model

import org.apache.commons.csv.CSVRecord

data class PubMedMetadata(
    val doi: String, val pmcid: String, val pubmedId: String,
    val abstract: String, val journal: String, val url: String
    /*
    ord_uid,sha,source_x,title,doi,pmcid,pubmed_id,license,abstract,publish_time,
    authors,journal,mag_id,who_covidence_id,arxiv_id,pdf_json_files,pmc_json_files,url,s2_id
     */
) {
    companion object {

        fun parseCSVRecord(record: CSVRecord): PubMedMetadata =
            PubMedMetadata(
                record.get("doi"),
                record.get("pmcid"),
                record.get("pubmed_id"),
                record.get("abstract"),
                record.get("journal"),
                record.get("url"),
                )

    }
}
