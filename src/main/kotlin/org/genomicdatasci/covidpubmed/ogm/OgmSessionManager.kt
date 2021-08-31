/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.ogm

import com.google.common.flogger.FluentLogger
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory


object OgmSessionManager {

    val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private val neo4jAccount = System.getenv("NEO4J_ACCOUNT")
    private val neo4jPassword = System.getenv("NEO4J_PASSWORD")
    private const val ogmPackage = "org.genomicdatasci.covidpubmed.ogm"
    val configuration: Configuration = Configuration.Builder()
        .uri("bolt://localhost")
        .credentials(neo4jAccount, neo4jPassword)
        .build()
    val sessionFactory = SessionFactory(configuration, ogmPackage)
    fun openSession(): Session = sessionFactory.openSession()
    fun closeSession() = sessionFactory.close()
}

fun main() {
    val session =OgmSessionManager.openSession()
    val testPubMedId = 287359
    val article = session.load(org.genomicdatasci.covidpubmed.ogm.PubMedArticle::class.java,testPubMedId.toLong())
    println("Title: ${article.title}   PubMed Id: ${article.pubMedId} ")
    article.annotations.forEach { it -> println("Type: ${it.type}  Text: ${it.text}") }
    println("Reference Count ${article.references.size}")
    article.references.forEach { it -> println("Reference: ${it.title}  PubMedId: ${it.pubMedId}") }
    OgmSessionManager.closeSession()
}