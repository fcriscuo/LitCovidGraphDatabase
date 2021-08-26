/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.ogm

import com.google.common.flogger.FluentLogger
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory


class OgmSessionManager {

    val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private val neo4jAccount = System.getenv("NEO4J_ACCOUNT")
    private val neo4jPassword = System.getenv("NEO4J_PASSWORD")
    private val ogmPackage = "org.genomicdatasci.covidpubmed.ogm"

    val configuration: Configuration = Configuration.Builder()
        .uri("bolt://localhost")
        .credentials(neo4jAccount, neo4jPassword)
        .build()

    val sessionFactory = SessionFactory(configuration, ogmPackage)

    fun openSession(): Session = sessionFactory.openSession()
    fun closeSession() = sessionFactory.close()

}

fun main() {
    val manager = OgmSessionManager()
    val session = manager.openSession()
    val testPubMedId = "28704402"
    val article = session.load(org.genomicdatasci.covidpubmed.ogm.PubMedArticle::class.java,testPubMedId)
    println(article.title)
}