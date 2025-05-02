package com.example.labbluetooth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

class StatsLoader @Inject constructor(@ApplicationContext context: Context) {
    data class Item(
        val deviceName: String,
        val totalMessages: Int,
        val deletedMessages: Int
    )

    private val file = File(context.filesDir, "stats.csv")
    var data: MutableList<Item> = if (file.isFile()) {
        BufferedReader(InputStreamReader(file.inputStream())).use { reader ->
            reader.readLine() // Header
            reader.lineSequence()
                .filter { it.isNotBlank() }
                .map {
                    val (device, total, deleted) = it.split(',', ignoreCase = false, limit = 3)
                    Item(device.trim(), total.trim().toInt(), deleted.trim().toInt())
                }.toMutableList()
        }
    } else {
        mutableListOf()
    }

    fun save() {
        BufferedWriter(OutputStreamWriter(file.outputStream())).use { writer ->
            writer.write("DeviceName,TotalMessages,DeletedMessages")
            for (item in data) {
                writer.newLine()
                writer.write("${item.deviceName},${item.totalMessages},${item.deletedMessages}")
            }
        }
    }
}