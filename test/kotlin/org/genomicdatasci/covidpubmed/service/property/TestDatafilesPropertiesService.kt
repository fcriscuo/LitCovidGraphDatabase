/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.service.property

fun main() {
    val baseOpt = DatafilesPropertiesService.resolvePropertyAsPathOption("base.data.path")
    DatafilesPropertiesService.filterProperties("disgenet").forEach { it -> println("URL: $it")}
    println("Datafile base $baseOpt")
    println("------framework.properties----------------------")
    FrameworkPropertiesService.displayProperties()
    println("------datafile.properties----------------------")
    DatafilesPropertiesService.displayProperties()
    // invalid property
    val badProp = FrameworkPropertiesService.resolvePropertyAsInt("no.such.property")
    println("no.such.property =  $badProp")
}