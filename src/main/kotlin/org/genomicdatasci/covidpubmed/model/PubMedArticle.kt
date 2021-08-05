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
data class PubMedArticle(val pubmedId: String, val pmcId: String= "",
                         val doiId:String="", val articleTitle:String,
                         var abstract:String,
                         val authors:List<Author>,
                         val journal: JournalIssue,
                         val annotations: Map<Int,PubMedAnnotation>,
                         val references: List<PubMedReference>
)

data class PubMedReference (val parentPubMedId: String,
                            val pubmedId: String,
                            val doiId:String="",
                            val articleTitle:String,
                            val authors:List<Author>,
                            val journalName: String,
                            val journalYear: String,
                            val journalVolume: String,
                            val annotations: Map<Int,PubMedAnnotation>,
                            ) {
    fun isValid()  = pubmedId.isNotEmpty() || doiId.isNotEmpty()


    companion object : LitCovidModel {
        fun parseBioCReferncePassage(parentPubmedId: String, passage: BioCPassage): PubMedReference {
            val pubmedId = resolveReferencePubMedId(passage)
            val doi = resolveReferenceDoiId(passage)
            val title = passage.text
            val authors = resolveAuthorList(pubmedId, passage)
            val annotations = processRefAnnotations(pubmedId, passage)
            val journalName = resolvePassageInfonValue("source", passage)
            val year = resolvePassageInfonValue("year", passage)
            val volume = resolvePassageInfonValue("volume", passage)
            return PubMedReference(
                parentPubmedId, pubmedId, doi, title,
                authors, journalName, year, volume, annotations
            )
        }

        private fun processRefAnnotations(pubmedId: String, passage: BioCPassage): Map<Int, PubMedAnnotation> {
            val annotationMap = mutableMapOf<Int, PubMedAnnotation>()
            passage.annotations.forEach {
                run {
                    val pubmedAnnotation = PubMedAnnotation.parseBioCAnnotation(pubmedId, it)
                    if (!annotationMap.contains(pubmedAnnotation.id)) {
                        annotationMap[pubmedAnnotation.id] = pubmedAnnotation
                    }
                }
            }
            return annotationMap.toMap()
        }
    }
}

data class PubMedAnnotation(val pubmedId: String, val id: Int, val type: String, val identifier: String, val text: String) {
    fun isValid(): Boolean  = (type.isNotEmpty() && identifier.isNotEmpty())

    companion object: LitCovidModel {
        fun parseBioCAnnotation( pubmedId: String,biocAnn: BioCAnnotation):PubMedAnnotation {
            val identifier = biocAnn.infons.getOrDefault("identifier","")
            val type = biocAnn.infons.getOrDefault("type","")
            val text = biocAnn.text
            val id = (identifier+type).hashCode()
            return PubMedAnnotation( pubmedId,id, type, identifier, text)
        }
    }
}

data class JournalIssue(val pubmedId: String, val journalName: String,
            val journalIssue:String)      {
    companion object:LitCovidModel{
        fun parseJournalString(pubmedId: String, journalText:String): JournalIssue{
            val tokens = parseStringOnSemiColon(journalText)
            val name = tokens.get(0)
            if (tokens.size > 1) {
                val sublist = tokens.subList(1, tokens.lastIndex)
                val issue = sublist.joinToString(" ")
                return JournalIssue(pubmedId, name, issue)
            }
            return JournalIssue(pubmedId, name, "")
        }
    }
}

data class Author (val pubmedId: String, val surname:String, val givenName: String ="",
            val id: Int ){
    /*
    sample Author BioC entry: <infon key="name_1">surname:Yamamoto;given-names:Shigeru</infon>
     */
    fun isValid() =surname.isNotBlank()
        companion object: LitCovidModel {
            fun parseAuthorString(pubmedId: String, authorText: String) :Author {
                var sn = " "
                var gn = ""
                parseStringOnSemiColon(authorText).forEach{
                    val name = parseStringOnColon(it)
                    if (name[0] == "surname" ) {
                        sn = name[1]
                    } else {
                        gn = name[1]
                    }
                }
                return Author(pubmedId,sn,gn, authorText.hashCode())
            }
        }
}

fun main () {
    val authorTestString1 = "surname:Sakaguti;given-names:Syuiti"
    val pubmedid = "32911311"
    val author1:Author = Author.parseAuthorString(pubmedid,authorTestString1)
    println(author1)
    val authorTestString2 = "surname:Watanabe"
    println(Author.parseAuthorString(pubmedid,authorTestString2))
    val author3 = Author.parseAuthorString(pubmedid,"surname:")
    println("Bad author object: $author3")
    println("Is bad author object valid?  ${author3.isValid()}")

}
