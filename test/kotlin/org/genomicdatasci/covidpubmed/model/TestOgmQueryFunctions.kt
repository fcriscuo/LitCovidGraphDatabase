/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.model

import org.genomicdatasci.covidpubmed.ogm.*
import org.genomicdatasci.covidpubmed.ogm.Author

fun main() {

    val pubmedId01 = 31978945
    val article = findPubMedArticleByPubMedId(pubmedId01.toLong())
    if (article != null) {
        println(article.title)
    }
    val pubmedId02 = 32292259
    findAnnotationByTypeAndPubMedId("Gene", pubmedId02.toLong())
        .forEach { println("Annotation type: ${it.type}  text: ${it.text}") }
    findAnnotationByTypeAndPubMedId("Disease", pubmedId02.toLong())
        .forEach { println("Annotation type: ${it.type}  text: ${it.text}") }
    findAuthorsByPubMedId(pubmedId02.toLong()).forEach { it ->
        println("Author given name: ${it.givenName}  surname: ${it.surname}")
        findArticlesByAuthorName(it.givenName, it.surname).forEach {
            println("  **Authored PubMed Article: ${it.pubMedId}  ${it.title}")
        }
    }
    val journal = findJournalIssueByPubMedId("34173600".toLong())
    if (journal != null) {
        println ("Journal: ${journal.journalName}  DOI: ${journal.doiId}  Issue: ${journal.journalIssue}")
    }

    val authorList = findAuthorsByPubMedId(32373339L)
    println("author list size = ${authorList.size}")
    val query = "MATCH (aut:Author)<-[:HAS_AUTHOR]-" +
            " (p:PubMedArticle{pubmed_id:32373339}) return aut"
    val className =  "Author"
    val ogmList = selectOgmData(query, className)
    println(ogmList.size)
    ogmList.map { it -> it as Author }
        .forEach { println("Author given name: ${it.givenName}  surname ${it.surname}") }
}