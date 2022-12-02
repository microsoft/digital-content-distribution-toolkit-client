// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_sdk

import android.util.Log
import com.msr.bine_sdk.hub.UpdateDownloadProgress
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer

fun InputStream.getInt(): Int {
    val tempBytes = ByteArray(4)
    readToArray(tempBytes)
    return tempBytes.getInt()
}

@Throws(IOException::class)
fun InputStream.getLong(): Long {
    val tempBytes = ByteArray(8)
    readToArray(tempBytes)
    return tempBytes.getLong()
}

@Throws(IOException::class)
fun InputStream.getString(stringLength: Int): String? {
    val stringBytes = ByteArray(stringLength)
    readToArray(stringBytes)
    return String(stringBytes)
}

fun ByteArray.getLong(): Long {
    val buffer = ByteBuffer.wrap(this)
    return buffer.long
}

fun ByteArray.getInt(): Int {
    val buffer = ByteBuffer.wrap(this)
    return buffer.int
}

@Throws(IOException::class)
fun InputStream.readToArray(destination: ByteArray) {
    var read = 0
    while (read < destination.size) {
        val tempRead = this.read(destination, read, destination.size - read)
        if (tempRead < 0) {
            throw IOException("Remote stream stopped: read: " + read + " out of " + destination.size)
        }
        read += tempRead
    }
}

fun InputStream.saveFile(filePath: String,
                         fileName: String,
                         fileSize: Long,
                         updateProgress: UpdateDownloadProgress?,
                         completion: (()->Unit)?): String? {
    var read: Long = 0
    val outputDir = File(filePath)
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }
    val output = File(outputDir, fileName)
    if (output.exists()) {
        output.delete()
    }
    if (!output.parentFile.exists()) output.parentFile.mkdirs()
    if (!output.exists() && output.createNewFile()) {

        val outputStream = FileOutputStream(output)
        val buffer = ByteArray(1024*1024)
        try{
            while (read < fileSize) {
                val toRead = if (buffer.size < fileSize - read) buffer.size else (fileSize - read).toInt()
                val tempRead = read(buffer, 0, toRead)
                if (tempRead < 0) {
                    throw IOException("Could not read form remote stream while writing to file")
                }
                updateProgress?.onProgress(tempRead)
                read += tempRead.toLong()
                outputStream.write(buffer, 0, tempRead)
            }

            if (completion != null) {
                completion()
            }
            Log.d("IOStreamUtil", "Saving file - " + output.absoluteFile)
        }
        finally {
            outputStream.flush()
            outputStream.close()
            close()
        }
    }
    return output.absolutePath
}