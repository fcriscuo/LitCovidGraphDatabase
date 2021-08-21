/*
 * Copyright (c) 2021 GenomicDataSci.org
 */

package org.genomicdatasci.covidpubmed.io

import arrow.core.Either
import bioc.BioCDocument
import bioc.io.BioCDocumentReader
import bioc.io.standard.BioCDocumentReaderImpl
import java.io.ByteArrayInputStream
import java.io.FileReader
import java.io.InputStream
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamReader
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stax.StAXSource
import javax.xml.transform.stream.StreamResult
import kotlin.system.exitProcess

/**
 * Created by fcriscuo on 2021Aug02
 */
class BioCDocumentSupplier(val fileName: String) {
    lateinit var xsr: XMLStreamReader
    lateinit var t: Transformer

    init {
        println("Processing BioC file: $fileName")
        val xif = XMLInputFactory.newInstance()
        xsr = xif.createXMLStreamReader(FileReader(fileName))
        while (!xsr.isStartElement) {
            xsr.next()
        }
        val tf = TransformerFactory.newInstance()
        t = tf.newTransformer()
    }

    /*
    Function to retrieve the next BioCDocument from the specified BioC file
    An Either object is returned to indicate when all the BioCDocuments in the
    file have been supplied
     */
    fun get(): Either<Exception, BioCDocument> {
        while (xsr.hasNext()) {
            if (xsr.isStartElement && xsr.localName === "document") {
                val result = StreamResult(StringWriter())
                t.transform(StAXSource(xsr), result)
                val xmlString = result.writer.toString()
                val inputXml: InputStream = ByteArrayInputStream(xmlString.toByteArray(StandardCharsets.UTF_8))
                val reader: BioCDocumentReader = BioCDocumentReaderImpl(inputXml)
                val document = reader.readDocument()
                return Either.Right(document)
            }
            xsr.next()
        }
        return Either.Left(Exception("All BioCDocuments in the supplied file have been processed"))
    }
}

fun main(args: Array<String>) {
    val filename = if (args.isNotEmpty()) args[0] else "data/xml/NLMIAT.BioC.xml"
    val supplier = BioCDocumentSupplier(filename)
    var count = 0
    while (true) {
        val retEither = supplier.get()
        when (retEither) {
            is Either.Right -> {
                val document = retEither.value
                println(document.iD)
                count += 1
            }
            is Either.Left -> {
                println("ERROR: ${retEither.value.message}")
                println("Document count = $count")
                exitProcess(0)
            }
        }
    }
}