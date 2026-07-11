package com.okhamzina.routinetaskmanager.data.storage

import android.content.Context
import android.net.Uri
import java.io.File

class ImageStorage(
    private val context: Context
) {

    fun saveImageToInternalStorage(
        sourceUri: Uri,
        fileName: String
    ): String {
        val imagesDir = File(
            context.filesDir,
            "images"
        )

        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val destinationFile = File(
            imagesDir,
            fileName
        )

        context.contentResolver.openInputStream(sourceUri).use { inputStream ->
            requireNotNull(inputStream) {
                "Cannot open image input stream"
            }

            destinationFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return destinationFile.absolutePath
    }

    fun deleteImage(
        imagePath: String
    ): Boolean {
        return runCatching {
            File(imagePath).delete()
        }.getOrDefault(false)
    }
}