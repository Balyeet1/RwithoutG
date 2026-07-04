package com.example.rewardwithoutguilt.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {
    private const val GREETING_IMAGE_NAME = "custom_greeting_image.jpg"

    fun copyUriToInternalStorage(context: Context, uri: Uri): String? {
        val file = File(context.filesDir, GREETING_IMAGE_NAME)
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
