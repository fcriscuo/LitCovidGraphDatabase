/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.ogm

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship

@NodeEntity (label = "PubMedArticle")
class PubMedArticle (
        @Property(name="pubmed_id") val pubMedId: String,
        @Property(name="article_title") val title: String,
        @Property(name="journal") val journal: String,
        @Property(name = "pmc_id") val pmcId: String,
        @Property(name = "doiid") val doiId: String,
        @Property(name = "url") val url:String
        ){
       @Relationship(type="HAS_ANNOTATION", direction = "OUTGOING")
       var annotations =  mutableSetOf<Annotation>()
        @Relationship(type="HAS_AUTHOR", direction = "OUTGOING")
        var authors =  mutableSetOf<Author>()
        @Relationship(type="HAS_JOURNAL_ISSUE", direction = "OUTGOING")
        var journalIssues =  mutableSetOf<JournalIssue>()
        @Relationship(type="HAS_REFERENCE", direction = "OUTGOING")
        var references=  mutableSetOf<PubMedArticle>()
        @Relationship(type="HAS_REFERENCE", direction = "INCOMING")
        var citations=  mutableSetOf<PubMedArticle>()

}

@NodeEntity (label = "JournalIssue")
class JournalIssue (
        @Property (name = "journal_name") val journalName: String,
        @Property (name = "id") val id: Int,
        @Property (name = "doiid") val doiId :String,
        @Property (name = "journal_issue") val journalIssue: String
        ){
        @Relationship (type="HAS_JOURNAL_ISSUE", direction = "INCOMING")
        var pubMedArticles = mutableSetOf<PubMedArticle>()
}


@NodeEntity (label="Author")
class Author (
        @Property(name = "id") val id: Int,
        @Property(name = "given_name") val givenName: String,
        @Property(name = "surname") val surname: String
        ){
        @Relationship (type="HAS_AUTHOR", direction = "INCOMING")
        var pubMedArticles = mutableSetOf<PubMedArticle>()
}

@NodeEntity (label= "Annotation")
class Annotation(
        @Property(name = "identifier") val identifier: String,
        @Property (name= " text") val text: String,
        @Property( name = "id") val id: Int,
        @Property(name = "type") val type: String
) {
   @Relationship (type="HAS_ANNOTATION", direction = "INCOMING")
   var pubMedArticles = mutableSetOf<PubMedArticle>()
}

