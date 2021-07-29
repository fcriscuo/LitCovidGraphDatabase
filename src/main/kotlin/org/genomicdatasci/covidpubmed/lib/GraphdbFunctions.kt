package org.genomicdatasci.covidpubmed.lib

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Relationship
import java.util.function.BiConsumer

/**
 * Created by fcriscuo on 7/27/21.
 */

class GraphdbFunctions ( val graphDb: GraphDatabaseService){

    var defineRelationshipPropertyConsumer =
        BiConsumer { rel: Relationship, relPair: Pair<String?, String?> ->
            graphDb.beginTx().use({ tx -> rel.setProperty(relPair.first, relPair.second) }) }

    fun defineRelationshipProperty(rel: Relationship, relPair: Pair<String, String>) =
        graphDb.beginTx().use { tx -> rel.setProperty(relPair.first, relPair.second) }
}