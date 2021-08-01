/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.lib

import bioc.BioCDocument
import bioc.BioCPassage
import org.genomicdatasci.covidpubmed.model.*

/**
 * Created by fcriscuo on 2021Jul30
 */

object DocumentConstants{
    const val PUBMED_ID_KEY = "article-id_pmid"
    const val PASSAGE_TYPE_KEY = "type"
    const val PASSAGE_TYPE_VALUE_FRONT = "front"
    const val INFON_SECTION_TYPE_KEY = "section_type"
    const val INFON_SECTION_TYPE_VALUE_TITLE = "TITLE"
    const val DOI_KEY = "article_id_doi"
    const val JOURNAL_KEY = "journal"
    const val PMC_ID_KEY = "article-id_pmc"

}

fun resolveDoi (passage: BioCPassage): String =
    passage.infons.getOrDefault(DocumentConstants.DOI_KEY,"")

fun resolvePubMedId(passage: BioCPassage): String =
    passage.infons.getOrDefault(DocumentConstants.PUBMED_ID_KEY,"")


fun resolvePmcId (passage: BioCPassage): String =
    passage.infons.getOrDefault(DocumentConstants.PMC_ID_KEY,"")

fun resolvePubMedPassage (document: BioCDocument): BioCPassage? =
    document.passages.find{
        it.infons.get(DocumentConstants.INFON_SECTION_TYPE_KEY)
            .equals(DocumentConstants.INFON_SECTION_TYPE_VALUE_TITLE)
}

fun resolveArticleJournal(pubmedId: String, passage: BioCPassage): JournalIssue {
    val journalText = passage.infons.getOrDefault(DocumentConstants.JOURNAL_KEY,"")
    return JournalIssue.parseJournalString(pubmedId, journalText)
}

fun resolveAuthorList(pubmedId: String, passage: BioCPassage): List<Author> {
    val matchingPredicate: (String) -> Boolean = { it.startsWith("name_") }
    val authorList= passage.infons.filterKeys(matchingPredicate).map {Author.parseAuthorString(pubmedId,it.value)}
    return authorList
}

// scan every passage in the document for annotations

fun resolveDocumentAnnotations( pubmedId: String, document: BioCDocument): Map<Int, PubMedAnnotation> {
    val annotationMap = mutableMapOf<Int,PubMedAnnotation >()
    document.passages.forEach { it.annotations.forEach { it
        run {
            val pubmedAnnotation = PubMedAnnotation.parseBioCAnnotation(pubmedId, it)
            if (!annotationMap.contains(pubmedAnnotation.id)) {
                annotationMap[pubmedAnnotation.id] = pubmedAnnotation
            }
        }
    }
    }
    return annotationMap.toMap()
}

fun resolveAnnotationList(pubmedId: String, passage: BioCPassage): List<PubMedAnnotation>
   = passage.annotations.map { it -> PubMedAnnotation.parseBioCAnnotation(pubmedId, it) }



fun processBioCDocument(document: BioCDocument): PubMedArticle {
   val pubMedPassage = resolvePubMedPassage(document)
    val article = processPubMedPassage(pubMedPassage)
}

fun processPubMedPassage(passage: BioCPassage?): PubMedArticle {
        val pubmedId = resolvePubMedId(passage)
        val pmcId = resolvePmcId(passage)
        val doi = resolveDoi(passage)
        val title = passage.text
        // the abstract text is in a subsequent passage
        val abstractPlaceHolder = ""
        val journal = resolveArticleJournal(pubmedId, passage)
        val authorList = resolveAuthorList(pubmedId, passage)
        // need to scan the entire document for annotations and references
        val annotationList = mutableListOf<PubMedAnnotation>()
        val references = mutableListOf<PubMedReference>()
       return PubMedArticle(pubmedId,pmcId,doi, title, abstractPlaceHolder,
           authorList,journal,annotationList, references )
}


