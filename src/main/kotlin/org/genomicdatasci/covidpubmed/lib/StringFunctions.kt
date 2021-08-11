package org.genomicdatasci.covidpubmed.lib

/**
 * Created by fcriscuo on 7/28/21.
 */
fun parseGeneOntologyEntry(goEntry:String):Pair<String,String> {
    val index:Int = goEntry.indexOf('[')+1
    val index2 = Math.max(0,index-1)
    return Pair(goEntry.substring(0,index2).trim(),
        goEntry.slice(index..index+10 ))
}

fun displayGeneOntologyList(title:String, list:List<String>): Unit {
    list.stream()
        .map { entry -> parseGeneOntologyEntry(entry) }
        .forEach { pair -> println("GO Entry: title ${pair.first}  ${pair.second}") }
}

/*
Double quotes (i.e. ") inside a text field causes Cypher
processing errors
 */
fun modifyInternalQuotes(text:String): String =
    text.replace("\"","'")