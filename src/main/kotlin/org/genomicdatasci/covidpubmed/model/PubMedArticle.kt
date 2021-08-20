/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.model

import bioc.BioCAnnotation
import bioc.BioCPassage
import org.genomicdatasci.covidpubmed.lib.*

/**
 * Created by fcriscuo on 2021Jul29
 */
data class PubMedArticle(
    val labels: List<String>,
    val pubmedId: String, val pmcId: String = "",
    val doiId: String = "", val articleTitle: String,
    var abstract: String,
    val authors: List<Author>,
    val journal: JournalIssue,
    val annotations: Map<Int, LitCovidAnnotation>,
    val references: List<PubMedReference>
)

data class PubMedReference(
    val labels: List<String>,
    val parentPubMedId: String,
    val pubmedId: String,
    val doiId: String = "",
    val articleTitle: String,
    val authors: List<Author>,
    val journal: JournalIssue,
    val annotations: Map<Int, LitCovidAnnotation>,
) {
    fun isValid() = pubmedId.isNotEmpty() && parentPubMedId.isNotEmpty()

    companion object : LitCovidModel {
        // the article details provided in a reference passage are different from
        // those provided in a title (i.e. main) passage
        fun parseBioCReferncePassage(parentPubmedId: String, passage: BioCPassage): PubMedReference {
            val pubmedId = resolveReferencePubMedId(passage)
            val doi = resolveReferenceDoiId(passage)
            val title = passage.text
            val authors = resolveAuthorList(pubmedId, passage)
            val annotations = processRefAnnotations(pubmedId, passage)
            val journalName = resolvePassageInfonValue("source", passage)
            val year = resolvePassageInfonValue("year", passage)
            val volume = resolvePassageInfonValue("volume", passage)
            val fpage = resolvePassageInfonValue("fpage", passage)
            val lpage = resolvePassageInfonValue("lpage", passage)
            val issue = resolvePassageInfonValue("issue", passage)
            val journalIssue = JournalIssue.parseReferenceJournalData(
                pubmedId, doi,
                journalName, year, volume, issue, fpage, lpage
            )
            return PubMedReference(
                listOf<String>("PubMedArticle", "Reference"),
                parentPubmedId, pubmedId, doi, title,
                authors, journalIssue,
                annotations
            )
        }

        private fun processRefAnnotations(pubmedId: String, passage: BioCPassage): Map<Int, LitCovidAnnotation> {
            val annotationMap = mutableMapOf<Int, LitCovidAnnotation>()
            passage.annotations.forEach {
                run {
                    val pubmedAnnotation = LitCovidAnnotation.parseBioCAnnotation(pubmedId, it)
                    if (!annotationMap.contains(pubmedAnnotation.id)) {
                        annotationMap[pubmedAnnotation.id] = pubmedAnnotation
                    }
                }
            }
            return annotationMap.toMap()
        }
    }
}

data class LitCovidAnnotation(
    val labels: List<String>,
    val pubmedId: String,
    val id: Int,
    val type: String,
    val identifier: String,
    val text: String
) {
    fun isValid(): Boolean = (type.isNotEmpty() && identifier.isNotEmpty())

    companion object : LitCovidModel {
        fun parseBioCAnnotation(pubmedId: String, biocAnn: BioCAnnotation): LitCovidAnnotation {
            val identifier =  when (biocAnn.infons.keys.contains("identifier")) {
                true -> biocAnn.infons.getOrDefault("identifier", "")
                false -> biocAnn.infons.getOrDefault("Identifier", "")
            }
            val type = biocAnn.infons.getOrDefault("type", "")
            val text = biocAnn.text
            val id = (identifier + type).hashCode()
            return LitCovidAnnotation(
                listOf(type),
                pubmedId, id, type, identifier, text
            )
        }
    }
}

data class JournalIssue(
    val labels: List<String>,
    val pubmedId: String,
    val doiId: String,
    val journalName: String,
    val journalIssue: String,
    val id: Int
) {
    fun isValid(): Boolean = pubmedId.isNotEmpty() &&
            journalName.isNotEmpty() &&
            doiId.isNotEmpty()

    companion object : LitCovidModel {
        fun parseReferenceJournalData(
            pubmedId: String,
            doiId: String = " ",
            journalName: String,
            journalYear: String,
            journalVolume: String,
            journalIssue: String,
            fpage: String,
            lpage: String
        ): JournalIssue {
            var issue = ""
            if (journalYear.isNotEmpty()) issue = "$issue($journalYear)"
            if (journalVolume.isNotEmpty()) issue = " $issue $journalVolume"
            if (journalIssue.isNotEmpty()) issue = " $issue $journalIssue"
            if (fpage.isNotEmpty()) issue = " $issue pg:$fpage-$lpage"
            val id = (journalName + JournalIssue).hashCode()
            val journalNameLabel = journalName.filter {it.isLetterOrDigit()  }
            return JournalIssue(listOf("JournalIssue", journalNameLabel), pubmedId, doiId, journalName, issue, id)
        }

        fun parseJournalString(
            pubmedId: String,
            doiId: String = "",
            journalText: String
        ): JournalIssue {
            val id = journalText.hashCode()
            val tokens = parseStringOnSemiColon(journalText)
            val name = tokens[0].filter { it.isLetterOrDigit() }
            val labels = when (name.isEmpty()) {
                true -> listOf()
                false -> listOf(name)
            }
            if (tokens.size > 1) {
                val sublist = tokens.subList(1, tokens.lastIndex)
                val issue = sublist.joinToString(" ")
                return JournalIssue(labels, pubmedId, doiId, name, issue, id)
            }
            return JournalIssue(labels, pubmedId, doiId, name, "", id)
        }
    }
}

data class Author(
    val labels: List<String>,
    val pubmedId: String,
    val surname: String,
    val givenName: String = "",
    val id: Int
) {
    /*
    sample Author BioC entry: <infon key="name_1">surname:Yamamoto;given-names:Shigeru</infon>
    val journalNameLabel = journalName.filter {it.isLetterOrDigit()  }
     */
    fun isValid() = surname.isNotBlank()

    companion object : LitCovidModel {
        fun parseAuthorString(pubmedId: String, authorText: String): Author {
            var sn = " "
            var gn = ""
            parseStringOnSemiColon(authorText).forEach {
                val name = parseStringOnColon(it)
                if (name[0] == "surname") {
                    sn = name[1].filter { it.isLetterOrDigit() }
                } else {
                    gn = name[1].filter {it.isLetterOrDigit()  }
                }
            }
            return Author(listOf(sn), pubmedId, sn, gn, authorText.hashCode())
        }
    }
}

