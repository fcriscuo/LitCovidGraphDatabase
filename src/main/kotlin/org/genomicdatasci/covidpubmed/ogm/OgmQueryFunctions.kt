/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.ogm

import com.google.common.flogger.FluentLogger
import io.ktor.util.reflect.*
import org.neo4j.ogm.model.Result
import kotlin.reflect.KProperty1


val logger: FluentLogger = FluentLogger.forEnclosingClass();

// Function to find PubMedArticle by PubMedId
fun findPubMedArticleByPubMedId(pubMedId: Long): PubMedArticle? {
    val query = "MATCH (pma:PubMedArticle{pubmed_id: $pubMedId}) RETURN pma"
    val result = OgmSessionManager.openSession().query(query, emptyMap<String, Any>())
    result.queryResults().forEach {
        if (it.containsKey("pma") && it["pma"] is PubMedArticle) {
            return it["pma"] as PubMedArticle
        }
    }
    return null
}

private fun executeQuery(query: String): Result =
    OgmSessionManager.openSession().query(query, emptyMap<String, Any>())

fun findAnnotationByTypeAndPubMedId(type: String, pubmedId: Long): List<Annotation> {
    val annotationList = mutableListOf<Annotation>()
    val query = "MATCH (annot:Annotation{type:\"$type\"})<-[:HAS_ANNOTATION]-" +
            " (p:PubMedArticle{pubmed_id:$pubmedId}) return annot"
    val result = executeQuery(query)
    result.queryResults().forEach {
        if (it.containsKey("annot") && it["annot"] is Annotation) {
            annotationList.add(it["annot"] as Annotation)
        }
    }
    return annotationList
}



fun findAuthorsByPubMedId(pubmedId: Long): List<Author> {
    val authorList = mutableListOf<Author>()
    val query = "MATCH (aut:Author)<-[:HAS_AUTHOR]-" +
            " (p:PubMedArticle{pubmed_id:$pubmedId}) return aut"
    val result = executeQuery(query)
    result.queryResults().forEach {
        if (it.containsKey("aut") && it["aut"] is Author) {
            authorList.add(it["aut"] as Author)
        }
    }
    return authorList
}

fun findArticlesByAuthorName(givenName: String, surname: String): List<PubMedArticle> {
    val articleList = mutableListOf<PubMedArticle>()
    val query = "MATCH (pma:PubMedArticle) - [HAS_AUTHOR] -> " +
            " (a:Author{given_name:\"$givenName\", surname: \"$surname\"}) return pma"
    val result = executeQuery(query)
    result.queryResults().forEach {
        if (it.containsKey("pma") && it["pma"] is PubMedArticle) {
            articleList.add(it["pma"] as PubMedArticle)
        }
    }
    return articleList
}

fun findJournalIssueByPubMedId(pubmedId: Long): JournalIssue? {
    val query = "MATCH (ji:JournalIssue) <- [HAS_JOURNAL_ISSUE] - " +
            "(pma: PubMedArticle {pubmed_id:$pubmedId}) return ji"
    val result = executeQuery(query)
    result.queryResults().forEach {
        if (it.containsKey("ji") && it["ji"] is JournalIssue) {
            return it["ji"] as JournalIssue
        }
    }
    return null
}


fun selectOgmData(query: String, className:String): List<Any> {
    val ogmList = mutableListOf<Any>()
    val result = executeQuery(query)
    val key = query.substringAfterLast(" ", "xxx").replace(";","")
    result.queryResults().forEach {
        if (it.containsKey(key) && it[key]?.javaClass?.simpleName.equals(className)) {
            it[key]?.let { it1 -> ogmList.add(it1) }
        }
    }
    return ogmList
}

