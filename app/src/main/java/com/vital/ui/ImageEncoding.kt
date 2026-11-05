package com.vital.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Downscales a picked or captured photo and hands back a JPEG base64 string for the AI
 * clients. Kept small (longest edge [MAX_DIM]) so a food shot stays a reasonable request.
 */
object ImageEncoding {

    private const val MAX_DIM = 1024
    private const val QUALITY = 80

    /** Reads a gallery pick; null when the stream cannot be decoded. */
    fun uriToImage(context: Context, uri: Uri): Bitmap? = try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    } catch (e: Exception) {
        null
    }

    fun toBase64Jpeg(bitmap: Bitmap): String {
        val scaled = scaleDown(bitmap)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    fun Bitmap.toDisplay(): ImageBitmap = this.asImageBitmap()

    private fun scaleDown(bitmap: Bitmap): Bitmap {
        val longer = max(bitmap.width, bitmap.height)
        if (longer <= MAX_DIM) return bitmap
        val ratio = MAX_DIM.toFloat() / longer
        val w = (bitmap.width * ratio).toInt().coerceAtLeast(1)
        val h = (bitmap.height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }
}
