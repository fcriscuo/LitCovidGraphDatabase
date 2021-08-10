/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.service.graphdb

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Result
import com.google.common.flogger.FluentLogger;


/**
 * Responsible for establishing a connection to a local Neo4j database
 * Executes supplied Cypher commands
 *
 * Created by fcriscuo on 2021Aug06
 */
object Neo4jConnectionService {
    private val neo4jAccount = System.getenv("NEO4J_ACCOUNT")
    private val neo4jPassword = System.getenv("NEO4J_PASSWORD")
    private const val uri = "bolt://localhost:7687"
    private val driver = GraphDatabase.driver( uri, AuthTokens.basic( neo4jAccount, neo4jPassword))
     val logger: FluentLogger = FluentLogger.forEnclosingClass();
   //val logger = KotlinLogging.logger {}

    fun close() = driver.close()

    /*
    Constraint definitions fo not return a result
     */
    fun defineDatabaseConstraint(command: String) {
        val session = driver.session()
        session.use {
             session.writeTransaction { tx -> tx.run(command)
            }!!
        }
    }

    fun executeCypherCommand(command: String):String {
        val session = driver.session()
        session.use {
            val resultString = session.writeTransaction { tx ->
                val result: Result = tx.run(command)
                result.single()[0].toString()
            }!!
            return resultString
        }
    }
}

fun main() {
    val query = "MATCH (N) RETURN COUNT(N)"
    val result = Neo4jConnectionService.executeCypherCommand(query)
   Neo4jConnectionService.logger.atInfo().log("Number of database nodes = $result")
    Neo4jConnectionService.close()
}