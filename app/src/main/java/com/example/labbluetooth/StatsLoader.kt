package com.example.labbluetooth

import android.content.Context
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class StatsLoader(context: Context, loadedCallback: (self: StatsLoader) -> Unit) {
    data class Item(
        val deviceName: String,
        val totalMessages: Int,
        val deletedMessages: Int
    )

    private val file = File(context.filesDir, "stats.csv")
    lateinit var data: MutableList<Item>

    private var currentThread: Thread

    init {
        currentThread = Thread {
            data = if (file.isFile()) {
                BufferedReader(InputStreamReader(file.inputStream())).use { reader ->
                    reader.readLine() // Header
                    reader.lineSequence()
                        .filter { it.isNotBlank() }
                        .map {
                            val (device, total, deleted) = it.split(
                                ',',
                                ignoreCase = false,
                                limit = 3
                            )
                            Item(device.trim(), total.trim().toInt(), deleted.trim().toInt())
                        }.toMutableList()
                }
            } else {
                mutableListOf()
            }
            loadedCallback(this@StatsLoader)
        }
        currentThread.start()
    }

    fun stop() {
        currentThread.interrupt()
    }

    fun save() {
        if (currentThread.isAlive) {
            currentThread.join()
        }

        currentThread = Thread {
            BufferedWriter(OutputStreamWriter(file.outputStream())).use { writer ->
                writer.write("DeviceName,TotalMessages,DeletedMessages")
                for (item in data) {
                    writer.newLine()
                    writer.write("${item.deviceName},${item.totalMessages},${item.deletedMessages}")
                }
            }
        }
        currentThread.start()
    }
}