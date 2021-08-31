/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.ogm

import org.neo4j.ogm.annotation.*

@NodeEntity(label = "PubMedArticle")
class PubMedArticle(
    @Property(name = "pubmed_id") var pubMedId: Long,
    @Property(name = "article_title") var title: String,
    @Property(name = "journal") var journal: String,
    @Property(name = "pmc_id") var pmcId: String,
    @Property(name = "doiid") var doiId: String,
    @Property(name = "url") var url: String
) {
    @Id
    @GeneratedValue
    var identity: Long? = null

    @Relationship(type = "HAS_ANNOTATION", direction = "OUTGOING")
    var annotations = mutableSetOf<Annotation>()

    @Relationship(type = "HAS_AUTHOR", direction = "OUTGOING")
    var authors = mutableSetOf<Author>()

    @Relationship(type = "HAS_JOURNAL_ISSUE", direction = "OUTGOING")
    var journalIssues = mutableSetOf<JournalIssue>()

    @Relationship(type = "HAS_REFERENCE", direction = "OUTGOING")
    var references = mutableSetOf<PubMedArticle>()

    @Relationship(type = "HAS_REFERENCE", direction = "INCOMING")
    var citations = mutableSetOf<PubMedArticle>()
}

@NodeEntity(label = "JournalIssue")
class JournalIssue(
    @Property(name = "journal_name") var journalName: String,
    @Property(name = "id") var id: Int,
    @Property(name = "doiid") var doiId: String,
    @Property(name = "journal_issue") var journalIssue: String
) {
    @Id
    @GeneratedValue
    var identity: Long? = null
    @Relationship(type = "HAS_JOURNAL_ISSUE", direction = "INCOMING")
    var pubMedArticles = mutableSetOf<PubMedArticle>()
}


@NodeEntity(label = "Author")
class Author(
    @Property(name = "id") var id: Int,
    @Property(name = "given_name") var givenName: String,
    @Property(name = "surname") var surname: String
) {
    @Id
    @GeneratedValue
    var identity: Long? = null
    @Relationship(type = "HAS_AUTHOR", direction = "INCOMING")
    var pubMedArticles = mutableSetOf<PubMedArticle>()
}

@NodeEntity(label = "Annotation")
class Annotation(
    @Property(name = "identifier") var identifier: String,
    @Property(name = " text") var text: String,
    @Property(name = "id") var id: Int,
    @Property(name = "type") var type: String
) {
    @Id
    @GeneratedValue
    var identity: Long? = null

    @Relationship(type = "HAS_ANNOTATION", direction = "INCOMING")
    var pubMedArticles = mutableSetOf<PubMedArticle>()
}

