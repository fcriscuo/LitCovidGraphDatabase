/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.dao

import com.google.common.flogger.FluentLogger
import org.genomicdatasci.covidpubmed.service.graphdb.Neo4jConnectionService

abstract class LitCovidDao {
   val logger: FluentLogger = FluentLogger.forEnclosingClass();

    fun executeMergeCommand(mergeCommand: String ):String {
        // display generated Cypher during development
        // TODO: remove logging stmt
        logger.atInfo().log(mergeCommand)
        return Neo4jConnectionService.executeCypherCommand(mergeCommand).toString()
    }


}