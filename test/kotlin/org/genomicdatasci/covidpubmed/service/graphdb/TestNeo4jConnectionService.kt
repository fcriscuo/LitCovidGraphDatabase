/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.service.graphdb

import com.google.common.flogger.FluentLogger

fun main() {
    System.setProperty(
        "flogger.backend_factory", "com.google.common.flogger.backend.log4j.Log4jBackendFactory#getInstance");
    val logger: FluentLogger = FluentLogger.forEnclosingClass();
    // demonstrate that the service will recover from an invalid Cypher command
    val badQuery = "MATCH (N) RETURN COUNT(X)"
    val badResult = Neo4jConnectionService.executeCypherCommand(badQuery)
    val goodQuery = "MATCH (N) RETURN COUNT(N)"
    val result = Neo4jConnectionService.executeCypherCommand(goodQuery)
    Neo4jConnectionService.logger.atInfo().log("Number of database nodes = $result")
    Neo4jConnectionService.close()
}