/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.lib

import arrow.core.Either
import bioc.BioCDocument
import bioc.BioCPassage
import io.kotlintest.matchers.startWith
import org.genomicdatasci.covidpubmed.io.BioCDocumentSupplier
import org.genomicdatasci.covidpubmed.model.*
import kotlin.system.exitProcess

/**
 * Created by fcriscuo on 2021Jul30
 */

object DocumentConstants {
    const val PUBMED_ID_KEY = "article-id_pmid"
    const val PASSAGE_TYPE_KEY = "type"
    const val PASSAGE_TYPE_VALUE_FRONT = "front"
    const val INFON_SECTION_TYPE_KEY = "section_type"
    const val INFON_SECTION_TYPE_VALUE_TITLE = "TITLE"
    const val DOI_KEY = "article-id_doi"
    const val JOURNAL_KEY = "journal"
    const val PMC_ID_KEY = "article-id_pmc"
    const val ABSTRACT_SECTION_TYPE_VALUE = "ABSTRACT"
    const val REF_SECTION_TYPE_VALUE = "REF"
    const val ABSTRACT_TYPE_VALUE = "abstract"
}

fun resolveDoi(passage: BioCPassage): String {
    if (passage.infons.containsKey(DocumentConstants.DOI_KEY)) {
        return passage.infons.getOrDefault(DocumentConstants.DOI_KEY, "")
    }
    // attempt to resolve missing DOI from Journal attribute
    val journalText = passage.infons.getOrDefault(DocumentConstants.JOURNAL_KEY, "")
    if (journalText.isNotEmpty()) {
        val doi = journalText.split(" ")
            .find { it.startsWith("doi:") }
        if (doi != null) {
            return doi
        }
    }
    return ""
}

fun resolvePubMedId(passage: BioCPassage): String =
    passage.infons.getOrDefault(DocumentConstants.PUBMED_ID_KEY, "")

fun resolvePmcId(passage: BioCPassage): String =
    passage.infons.getOrDefault(DocumentConstants.PMC_ID_KEY, "")

/*
The PubMed passage is essential for processing a BioCDocument
 */

fun displayPassageInfons(document: BioCDocument) {
    document.passages.forEach { it ->
        it.infons.forEach { println(" key:  ${it.key}  value: ${it.value}") }
    }
}

fun resolvePubMedPassage(document: BioCDocument): Either<Exception, BioCPassage> {

    val bioc = document.passages
        .filter { it -> it.infons.containsKey(DocumentConstants.INFON_SECTION_TYPE_KEY) }
        .find {
            it.infons.getValue(DocumentConstants.INFON_SECTION_TYPE_KEY)
                .equals(DocumentConstants.INFON_SECTION_TYPE_VALUE_TITLE)
        }
    /*
    Some documents have an abbreviated front passage
     */
    if (bioc != null) {
        return Either.Right(bioc)
    } else {
        val bioc2 = document.passages
            .filter { it -> it.infons.containsKey(DocumentConstants.PASSAGE_TYPE_KEY) }
            .find {
                it.infons.getValue(DocumentConstants.PASSAGE_TYPE_KEY) == "title"
            }
        if (bioc2 != null) {
            // ensure that the PubMed Id is set
            bioc2.infons[DocumentConstants.PUBMED_ID_KEY] = document.iD
            return Either.Right(bioc2)
        }
    }
    return Either.Left(Exception("A PubMed passage is not available for document id ${document.iD}"))
}

fun resolveArticleJournal(pubmedId: String, passage: BioCPassage): JournalIssue {
    val journalText = passage.infons.getOrDefault(DocumentConstants.JOURNAL_KEY, "")
    return JournalIssue.parseJournalString(pubmedId, journalText)
}

fun resolveAuthorList(pubmedId: String, passage: BioCPassage): List<Author> {
    val matchingPredicate: (String) -> Boolean = { it.startsWith("name_") }
    val authorList = passage.infons.filterKeys(matchingPredicate).map { Author.parseAuthorString(pubmedId, it.value) }
    return authorList
}

// scan every passage in the document, except for annotations that
// belong to references

fun resolveDocumentAnnotations(pubmedId: String, document: BioCDocument): Map<Int, PubMedAnnotation> {
    val annotationMap = mutableMapOf<Int, PubMedAnnotation>()
    document.passages
        .filter { it -> it.infons.containsKey(DocumentConstants.INFON_SECTION_TYPE_KEY) }
        .filter { it.infons.getValue(DocumentConstants.INFON_SECTION_TYPE_KEY) != DocumentConstants.REF_SECTION_TYPE_VALUE }
        .forEach {
            it.annotations.forEach {
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

fun resolveAnnotationList(pubmedId: String, passage: BioCPassage): List<PubMedAnnotation> =
    passage.annotations.map { it -> PubMedAnnotation.parseBioCAnnotation(pubmedId, it) }


fun processBioCDocument(document: BioCDocument): PubMedArticle? {
    val pubMedEither = resolvePubMedPassage(document)
    when (pubMedEither) {
        is Either.Right -> {
            val pubmedPassage = pubMedEither.value
            return processPubMedPassage(document, pubmedPassage)
        }
        is Either.Left -> {
            println(pubMedEither.value.message)
        }
    }
    return null
}

/*
Function to resolve all unique annotations in a BioCDocument and
associate them with a PubMed Id
 */
fun resolveAnnotationMap(document: BioCDocument, pubmedId: String): Map<Int, PubMedAnnotation> {
    val annotationMap = mutableMapOf<Int, PubMedAnnotation>()
    document.passages.forEach { it ->
        run {
            it.annotations.forEach { it ->
                run {
                    val annotation = PubMedAnnotation.parseBioCAnnotation(pubmedId, it)
                    if (!annotationMap.containsKey(annotation.id)) {
                        annotationMap.put(annotation.id, annotation)
                    }
                }
            }
        }
    }
    return annotationMap.toMap()
}

/*
Function to resolve the article abstract.
There are usually >1 BioCPassages that have a type of abstract
Usually the first one has the correct text
 */

fun resolveArticleAbstract(document: BioCDocument): String {
    val abstract = document.passages
        .filter { it.infons.containsKey(DocumentConstants.INFON_SECTION_TYPE_KEY) }
        .filter { it.infons.getValue(DocumentConstants.INFON_SECTION_TYPE_KEY) == DocumentConstants.ABSTRACT_SECTION_TYPE_VALUE }
        .find {
            it.infons.getValue(DocumentConstants.PASSAGE_TYPE_KEY) == DocumentConstants.ABSTRACT_TYPE_VALUE
        }?.text
    if (abstract != null) {
        return abstract
    }
    return ""
}

fun processPubMedPassage(document: BioCDocument, passage: BioCPassage): PubMedArticle {
    val pubmedId = resolvePubMedId(passage)
    val pmcId = resolvePmcId(passage)
    val doi = resolveDoi(passage)
    val title = passage.text
    val abstract = resolveArticleAbstract(document)
    val journal = resolveArticleJournal(pubmedId, passage)
    val authorList = resolveAuthorList(pubmedId, passage)
    // need to scan the entire document for annotations and references
    // create a placeholder list
    val annotationMap = resolveAnnotationMap(document, pubmedId)
    val references = mutableListOf<PubMedReference>()
    return PubMedArticle(
        pubmedId, pmcId, doi, title, abstract,
        authorList, journal, annotationMap, references
    )
}

/*
Main function to test functions in DocumentFunctions.kt
 */
fun main(args: Array<String>) {
    val filename = if (args.size > 0) args[0] else "data/xml/NLMIAT.BioC.xml"
    val supplier = BioCDocumentSupplier(filename)
    var count = 0
    while (true) {
        val retEither = supplier.get()
        when (retEither) {
            is Either.Right -> {
                val document = retEither.value
                //displayPassageInfons(document)
                val pubMedArticle = processBioCDocument(document)
                println(pubMedArticle)
                count += 1
            }
            is Either.Left -> {
                println(" ${retEither.value.message}")
                println("Document count = $count")
                exitProcess(0)
            }
        }
    }
}


