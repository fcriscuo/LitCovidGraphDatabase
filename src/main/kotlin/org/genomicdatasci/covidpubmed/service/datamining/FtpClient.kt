package org.genomicdatasci.covidpubmed.service.datamining

import arrow.core.Either
import com.google.common.flogger.FluentLogger
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.genomicdatasci.covidpubmed.lib.LitCovidFileUtils
import org.genomicdatasci.covidpubmed.lib.RefinedFilePath
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.net.URL

/**
 * Created by fcriscuo on 7/28/21.
 */
const val FTP_USER = "anonymous"
const val FTP_PASSWORD = "batteryparkdev@gmail.com" //TODO get from environment
const val FTP_PORT = 21
private  val logger: FluentLogger = FluentLogger.forEnclosingClass();


/*
FTP client class responsible for establishing a anonymous connection to an FTP server
and retrieving a specified remote file to a local file.
If the specified local file already exists, it will be overwritten
 */
data class FtpClient(val server: String) {
    private val ftp = FTPClient()

    init {
        ftp.addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out)))
        ftp.enterLocalPassiveMode()
    }

    fun downloadRemoteFile(remoteFilePath: String, localFilePath: RefinedFilePath): Either<Exception, String> {
        ftp.connect(server, FTP_PORT)
        val replyCode = ftp.replyCode
        if (FTPReply.isPositiveCompletion(replyCode)) {
            ftp.login(FTP_USER, FTP_PASSWORD)
            ftp.setFileType(FTP.ASCII_FILE_TYPE)
            try {
                val outputStream = FileOutputStream(localFilePath.getPath().toFile(),false)
                ftp.retrieveFile(remoteFilePath,outputStream)
                when (localFilePath.exists()){
                    true -> return Either.Right("Remote file: $remoteFilePath has been downloaded to ${localFilePath.filePathName}")
                    false -> return Either.Left(IOException("Download of remote file: $remoteFilePath to ${localFilePath.filePathName} failed"))
                }
            } catch (e: Exception) {
                return Either.Left(e)
            } finally {
                ftp.logout()
                ftp.disconnect()
            }
        }
        return Either.Left(IOException("FTP server $server refused anonymous connection"))
    }
}

/*
Function to access a remote file via anonymous FTP and copy its contents to
the local filesystem at a specified location.
Parameters: ftpUrl - Complete URL for remote file
            localFilePath - local filesystem location
Returns: An Either - Left is an Exception, Right is a success message
 */
fun retrieveRemoteFileByFtpUrl(ftpUrl: String, localFilePath: RefinedFilePath): Either<Exception, String> {
    val urlConnection = URL(ftpUrl)
    urlConnection.openConnection()
    // the FileUtils method closes the input stream
    return try {
        FileUtils.copyInputStreamToFile(urlConnection.openStream(), localFilePath.getPath().toFile())
        if (FilenameUtils.getExtension(localFilePath.filePathName) in LitCovidFileUtils.compressedFileExtensions) {
            LitCovidFileUtils.gunzipFile(localFilePath.filePathName)
        }
        Either.Right("$ftpUrl downloaded to  $localFilePath")
    } catch (e: Exception) {
        Either.Left(e)
    }
}
