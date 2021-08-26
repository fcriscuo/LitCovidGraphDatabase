/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.service.graphdb

import com.google.common.flogger.FluentLogger
import com.google.common.flogger.StackSize
import org.neo4j.driver.*
import java.io.File
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

/**
 * Responsible for establishing a connection to a local Neo4j database
 * Executes supplied Cypher commands
 *
 * Created by fcriscuo on 2021Aug06
 */
object Neo4jConnectionService {

    val logger: FluentLogger = FluentLogger.forEnclosingClass();
    private val neo4jAccount = System.getenv("NEO4J_ACCOUNT")
    private val neo4jPassword = System.getenv("NEO4J_PASSWORD")
    // TODO: make this a property
    private const val logsDir = "/tmp/logs"
    private val cypherPath = "$logsDir/" + generateCypherLogFileName()
    private val cypherFileWriter = File(cypherPath).bufferedWriter()
    private const val uri = "bolt://localhost:7687"
    val config:Config = Config.builder().withLogging(Logging.slf4j()).build()
    private val driver = GraphDatabase.driver(uri, AuthTokens.basic(neo4jAccount, neo4jPassword),
        config)

    fun close()  {
        driver.close()
        cypherFileWriter.close()
    }
        /*
        Constraint definitions fo not return a result
         */
        fun defineDatabaseConstraint(command: String) {
            val session = driver.session()
            session.use {
                session.writeTransaction { tx ->
                    tx.run(command)
                }!!
            }
        }

        fun executeCypherCommand(command: String): String {
            Neo4jConnectionService.cypherFileWriter.write("$command\n")
            val session = driver.session()
            lateinit var resultString:String
            session.use {
                try {
                    session.writeTransaction { tx ->
                        val result: Result = tx.run(command)
                        resultString = when (result.hasNext()) {
                            true -> result.single()[0].toString()
                            false -> ""
                        }
                    }!!
                    return resultString.toString()
                } catch (e: Exception) {
                    logger.atSevere().withStackTrace(StackSize.FULL)
                        .withCause(e).log(e.message)
                    logger.atSevere().log("Cypher command: $command")
                }
            }
            return ""
        }
    }

fun generateCypherLogFileName( prefix: String = "litcovod_cypher")
    = prefix +"_"  +LocalDateTime.now().toString() +".log"

fun main() {
    System.setProperty(
        "flogger.backend_factory", "com.google.common.flogger.backend.log4j.Log4jBackendFactory#getInstance");
    // demonstrate that the service will recover from an invalid Cypher command
    val badQuery = "MATCH (N) RETURN COUNT(X)"
    val badResult = Neo4jConnectionService.executeCypherCommand(badQuery)
    val goodQuery = "MATCH (N) RETURN COUNT(N)"
    val result = Neo4jConnectionService.executeCypherCommand(goodQuery)
    Neo4jConnectionService.logger.atInfo().log("Number of database nodes = $result")
    Neo4jConnectionService.close()
}