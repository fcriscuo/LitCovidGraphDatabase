package org.genomicdatasci.covidpubmed.service.graphdb

import org.genomicdatasci.covidpubmed.service.property.FrameworkPropertiesService
import org.neo4j.configuration.GraphDatabaseSettings
import org.neo4j.dbms.api.DatabaseManagementService
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Label
import org.neo4j.io.fs.FileUtils
import java.io.File
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * Created by fcriscuo on 7/28/21.
 */

private val DEFAULT_TEST_DATABSE_DIRECTORY = File("target/test-litcovid-graph-db")

enum class RunMode {
    PROD, READ_ONLY, TEST
}

/*
To support legacy Java classes, implement the Java Supplier interface
TODO: refactor to use Kotlin public getter for graphdb
 */
class LitCovidGraphDatabaseSupplier(dbDir: File = DEFAULT_TEST_DATABSE_DIRECTORY,
                               dbName: String = GraphDatabaseSettings.DEFAULT_DATABASE_NAME,
                               runMode: RunMode = RunMode.TEST) : Supplier<GraphDatabaseService> {
    private val graphdb = LitCovidGraphDatabaseServiceInitializer(dbDir, dbName, runMode).graphdb

    override fun get(): GraphDatabaseService {
        return graphdb
    }
}

private class LitCovidGraphDatabaseServiceInitializer(dbDir: File,
                                                 dbName: String,
                                                 runMode: RunMode) {
    private val databaseDir = dbDir
    private val databaseName = dbName
    private val mode = runMode
    val graphdb: GraphDatabaseService = createTestGraphDatabaseService()

    fun createTestGraphDatabaseService(): GraphDatabaseService {
        return when (mode) {
            RunMode.TEST -> configureTestDatabase()
            RunMode.PROD -> configureProdDatabase()
            RunMode.READ_ONLY -> configureReadOnlyDatabase()

        }
    }

    private fun configureReadOnlyDatabase(): GraphDatabaseService {
        val databaseManagementServiceBuilder = DatabaseManagementServiceBuilder(databaseDir)
        databaseManagementServiceBuilder.setConfig(GraphDatabaseSettings.read_only, true)
        val managementService = DatabaseManagementServiceBuilder(databaseDir).build()
        registerShutdownHook(managementService)
        return managementService.database(databaseName)
    }

    private fun configureProdDatabase(): GraphDatabaseService {
        val managementService = DatabaseManagementServiceBuilder(databaseDir).build()
        registerShutdownHook(managementService)
        return managementService.database(databaseName)
    }

    private fun configureTestDatabase(): GraphDatabaseService {
        FileUtils.deleteDirectory(databaseDir.toPath())
        val managementService = DatabaseManagementServiceBuilder(databaseDir).build()
        registerShutdownHook(managementService)
        return managementService.database(databaseName)
    }

    fun registerShutdownHook(managementService: DatabaseManagementService) {
        Runtime.getRuntime().addShutdownHook(Thread() {
            fun run() {
                managementService.shutdown()
            }
        })
    }
}
fun main(args: Array<String>): Unit {

    val pathOpt = FrameworkPropertiesService.resolvePropertyAsPathOption("readonly.db.path")
        .orNull()
    val dbDir = if (pathOpt != null) {
        pathOpt.toFile()
    } else {
        DEFAULT_TEST_DATABSE_DIRECTORY
    }
    val graphDb = LitCovidGraphDatabaseSupplier(dbDir, "test.db", RunMode.READ_ONLY).get()
    try {
        graphDb.beginTx().use { tx ->
            tx.allLabels
                .forEach(Consumer { label: Label -> println(label.name()) })
            println("Node count = " + tx.allNodes.stream().count())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}