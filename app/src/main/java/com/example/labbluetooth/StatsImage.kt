package com.example.labbluetooth

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.createBitmap
import java.io.IOException

fun generateStatsImage(loader: StatsLoader): Bitmap {
    val bitmap = createBitmap(800, 600)
    bitmap.eraseColor(Color.WHITE)

    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.style = Paint.Style.STROKE
    paint.textSize = 24f

    canvas.drawRect(300f, 200f, 500f, 600f, paint)
    canvas.drawRect(100f, 300f, 300f, 600f, paint)
    canvas.drawRect(500f, 400f, 700f, 600f, paint)

    canvas.drawText("1", 400f, 300f, paint)
    canvas.drawText("2", 200f, 400f, paint)
    canvas.drawText("3", 600f, 500f, paint)

    val data: MutableList<Pair<String, Int>> = loader.data.map { Pair(it.deviceName, it.totalMessages) }.toMutableList()
    data.sortByDescending { it.second }

    data.getOrNull(0)?.let {
        canvas.drawText("${it.first} (${it.second})", 350f, 100f, paint)
    }

    data.getOrNull(1)?.let {
        canvas.drawText("${it.first} (${it.second})", 50f, 200f, paint)
    }

    data.getOrNull(2)?.let {
        canvas.drawText("${it.first} (${it.second})", 650f, 300f, paint)
    }

    canvas.save()
    return bitmap
}

fun saveImage(context: Context, image: Bitmap): Uri {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}.png")
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
    }

    var uri: Uri? = null

    return runCatching {
        with(context.contentResolver) {
            insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also {
                uri = it // Keep uri reference so it can be removed on failure

                openOutputStream(it)?.use { stream ->
                    if (!image.compress(Bitmap.CompressFormat.JPEG, 95, stream))
                        throw IOException("Failed to save bitmap.")
                } ?: throw IOException("Failed to open output stream.")

            } ?: throw IOException("Failed to create new MediaStore record.")
        }
    }.getOrElse {
        uri?.let { orphanUri ->
            // Don't leave an orphan entry in the MediaStore
            context.contentResolver.delete(orphanUri, null, null)
        }

        throw it
    }
}