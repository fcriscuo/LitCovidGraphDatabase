package org.genomicdatasci.covidpubmed.lib

import arrow.core.Either
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.genomicdatasci.covidpubmed.lib.LitCovidFileUtils.retrieveRemoteFileByDatafileProperty
import org.genomicdatasci.covidpubmed.service.property.DatafilesPropertiesService
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.zip.GZIPInputStream
import javax.annotation.Nullable

/**
 * A collection of File-related functions
 * Created by fcriscuo on 7/27/21.
 */

object LitCovidFileUtils {
    private val BASE_DATA_DIRECTORY = DatafilesPropertiesService.resolvePropertyAsString("base.data.path") ?: "/tmp"
    private val BASE_SUBDIRECTORY_NAME = DatafilesPropertiesService.resolvePropertyAsString("base.subdirectory.name") ?: "data"
    private val fileSeparator:String = System.getProperty("file.separator")
    val compressedFileExtensions = listOf<String>("gz","zip")

    @JvmStatic
/*
Function to delete a directory recursively
 */
    fun deleteDirectoryRecursively(path: Path): Either<Exception, String> {
        return try {
            FileUtils.deleteDirectory(path.toFile())
            Either.Right("${path.fileName} and children have been deleted")
        } catch (e: Exception) {
            Either.Left(e)
        }
    }

    fun resolveDataSourceFromUrl(url: URL): String {
        val host = url.host.uppercase(Locale.getDefault())
        return when {
            host.contains("EBI") -> "EBI"
            host.contains("ENSEMBL") -> "ENSEMBL"
            host.contains("GENCODE") -> "GenCode"
            host.contains("INTACT") -> "IntAct"
            host.contains("UNIPROT") -> "UniProt"
            host.contains("DRUGBANK") -> "DrugBank"
            host.contains("SEQUENCEONTOLOGY") -> "SequenceOntology"
            host.contains("PHARMGKB") -> "PharmgKB"
            host.contains("juniper.health.unm.ed".toUpperCase()) -> "DisGeNET"
            host.contains("disgenet".toUpperCase()) -> "DisGeNET"
            else -> "UNSPECIFIED"
        }
    }

    fun resolveLocalFileNameFromPropertyPair(propertyPair: Pair<String, String>): Either<Exception, String> {
        val subdirectory = resolveDataSubDirectoryFromPropertyName(propertyPair.first)
        val localPath = subdirectory + fileSeparator + resolveSourceFileName(propertyPair.second)
        return Either.Right(localPath)
    }

    fun resolveSourceFileName(remotePath: String) =
        remotePath.split(fileSeparator).last()


    private fun resolveDataSubDirectoryFromPropertyName(propertyName: String): String {
        if (propertyName.startsWith(BASE_SUBDIRECTORY_NAME.toString())) {
            return BASE_DATA_DIRECTORY.toString() + fileSeparator + propertyName.replace(".", fileSeparator)
        }
        return BASE_DATA_DIRECTORY.toString() + fileSeparator + BASE_SUBDIRECTORY_NAME.toString() +
                fileSeparator + propertyName.replace(".", fileSeparator)
    }

    /*
    Read the contents of a resource file as a Stream
     */
    @Nullable
    fun readFileAsLinesUsingGetResourceAsStream(fileName: String) =
        this::class.java.getResourceAsStream(fileName).bufferedReader().readLines()

    /*
    Function to access a remote file via anonymous FTP and copy its contents to
    the local filesystem at a specified location.
    Parameters: ftpUrl - Complete URL for remote file
    Returns: Either whose Left side is an Exception, and whose Right side contains a success message
     */
    fun retrieveRemoteFileByDatafileProperty(propertyPair: Pair<String, String>): Either<Exception, String> {
        val urlConnection = URL(propertyPair.second)
        when (val local = resolveLocalFileNameFromPropertyPair(propertyPair)) {
            is Either.Right -> {
                val localFilePath = local.value
                urlConnection.openConnection()
                return try {
                    FileUtils.copyInputStreamToFile(urlConnection.openStream(), File(localFilePath))
                    if (FilenameUtils.getExtension(localFilePath) in compressedFileExtensions) {
                        gunzipFile(localFilePath)
                    }
                    Either.Right("${propertyPair.second} downloaded to  $localFilePath")
                } catch (e: Exception) {
                    Either.Left(e)
                }
            }
            is Either.Left -> return Either.Left(local.value)
        }
    }

    /*
    unzip a compressed file
    the expanded file is given the same filename without the .gz or .zip extension
    and the compressed file is deleted
    this code is a simple refactoring of a Java example
     */
    //TODO: make this asynchronous
    fun gunzipFile(compressedFile: String): Either<Exception, String> {
        val buffer = ByteArray(1024)
        val expandedFile = FilenameUtils.removeExtension(compressedFile)
        val gzis = GZIPInputStream(FileInputStream(compressedFile))
        val out = FileOutputStream(expandedFile)
        try {
            var len: Int
            while (true) {
                len = gzis.read(buffer)
                if (len > 0) {
                    out.write(buffer, 0, len)
                } else {
                    //delete compressed file
                    FileUtils.forceDelete(File(compressedFile))
                    return Either.Right("$compressedFile expanded to $expandedFile")
                }
            }
        } catch (e: Exception) {
            return Either.Left(e)
        } finally {
            gzis.close()
            out.close()
        }
    }
}

fun main() {
    ///data.disgenet.curated.gene-disease.associations=https://www.disgenet.org/static/disgenet_ap1/files/downloads/curated_gene_disease_associations.tsv.gz
    val result = retrieveRemoteFileByDatafileProperty(Pair("data.mint.human",
        "http://www.ebi.ac.uk/Tools/webservices/psicquic/mint/webservices/current/search/query/species:human" ))
    when (result){
        is Either.Right -> println(result.value)
        is Either.Left -> println(result.value.message)
    }
}
