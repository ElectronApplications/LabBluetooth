package com.example.labbluetooth

import android.content.Context
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer

fun InputStream.readBoolean(): Boolean {
    val byte = this.read()
    return byte != 0
}

fun OutputStream.writeBoolean(value: Boolean) {
    this.write(if (value) {1} else {0})
}

fun InputStream.readInt(): Int {
    val bytes = ByteArray(4)
    this.read(bytes)
    return ByteBuffer.wrap(bytes).getInt()
}

fun OutputStream.writeInt(value: Int) {
    val bytes = ByteBuffer.allocate(4).putInt(value).array()
    this.write(bytes)
}

fun InputStream.readString(): String {
    val size = this.readInt()
    val bytes = ByteArray(size)
    this.read(bytes)
    return String(bytes)
}

fun OutputStream.writeString(value: String) {
    this.writeInt(value.length)
    this.write(value.toByteArray())
}

class MessageLoader(context: Context, chat: String) {
    private val file: File = File(context.filesDir, "${chat}.chat")

    var encrypted: Boolean private set
    var messages: MutableList<Pair<String, MessageType>>

    init {
        if (file.isFile()) {
            file.inputStream().use { stream ->
                encrypted = stream.readBoolean()
                if (encrypted) {
                    // TODO
                }

                val messagesAmount = stream.readInt()
                messages = (0 until messagesAmount).map {
                    val type = MessageType.fromId(stream.readInt())!!
                    val message = stream.readString()
                    Pair(message, type)
                }.toMutableList()
            }
        } else {
            encrypted = false
            messages = mutableListOf()
        }
    }

    fun delete() {
        file.delete()
    }

    fun save() {
        file.outputStream().use { stream ->
            stream.writeBoolean(encrypted)
            if (encrypted) {
                // TODO
            }

            stream.writeInt(messages.size)

            messages.forEach { (message, type) ->
                stream.writeInt(type.id)
                stream.writeString(message)
            }
        }
    }
}

fun activeChats(context: Context): Array<Pair<String, Int?>> {
    val senders = context.fileList().filter { it.endsWith(".chat") }
    return senders.map {
        val name = it.dropLast(".chat".length)
        Pair(name, MessageLoader(context, name).messages.size)
    }.toTypedArray()
}