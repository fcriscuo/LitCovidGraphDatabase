package org.genomicdatasci.covidpubmed.service.property

/**
 * * Services responsible for resolving property values from
 * specified properties files
 *
 */
object DatafilesPropertiesService : AbstractPropertiesService() {
    private const val PROPERTIES_FILE = "/datafiles.properties"

    init {
        resolveFrameworkProperties(PROPERTIES_FILE)
    }
}

object FrameworkPropertiesService : AbstractPropertiesService() {
    private const val PROPERTIES_FILE = "/framework.properties"

    init {
        resolveFrameworkProperties(PROPERTIES_FILE)
    }
}

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