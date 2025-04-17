package com.example.labbluetooth

import android.content.Context
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


fun InputStream.readBoolean(): Boolean {
    val byte = this.read()
    return byte != 0
}

fun OutputStream.writeBoolean(value: Boolean) {
    this.write(
        if (value) {
            1
        } else {
            0
        }
    )
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

class MessageLoader(
    context: Context,
    chat: String,
    loadedCallback: (self: MessageLoader) -> Unit,
    password: String? = null
) {
    private val file: File = File(context.filesDir, "${chat}.chat")

    var encrypted: Boolean = false
        private set
    lateinit var messages: MutableList<Pair<String, MessageType>>

    var password: String? = password
        set(value) {
            field = value
            encrypted = value != null
        }

    private var currentThread: Thread

    init {
        currentThread = Thread {
            if (file.isFile()) {
                file.inputStream().use { stream ->
                    encrypted = stream.readBoolean()

                    val nestedStream: InputStream = if (encrypted) {
                        throw NotImplementedError()

//                    if (password == null) {
//                        throw Exception("Password was not provided for encrypted file")
//                    }
//
//                    val cipher = Cipher.getInstance("AES/GCM/PKCS5Padding")
//
//                    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
//                    val spec: KeySpec = PBEKeySpec(password.toCharArray())
//                    val tmp = factory.generateSecret(spec)
//                    val secret = SecretKeySpec(tmp.encoded, "AES")
//
//                    cipher.init(Cipher.DECRYPT_MODE, secret)
//                    CipherInputStream(stream, cipher)
                    } else {
                        stream
                    }

                    nestedStream.use {
                        val messagesAmount = stream.readInt()
                        messages = (0 until messagesAmount).map {
                            val type = MessageType.fromId(stream.readInt())!!
                            val message = stream.readString()
                            Pair(message, type)
                        }.toMutableList()
                    }
                }
            } else {
                encrypted = false
                messages = mutableListOf()
            }
            loadedCallback(this@MessageLoader)
        }
        currentThread.start()
    }

    fun stop() {
        currentThread.interrupt()
    }

    fun delete() {
        if (currentThread.isAlive) {
            currentThread.join()
        }

        file.delete()
    }

    fun save() {
        if (currentThread.isAlive) {
            currentThread.join()
        }

        currentThread = Thread {
            file.outputStream().use { stream ->
                stream.writeBoolean(encrypted)

                val nestedStream: OutputStream = if (encrypted) {
                    throw NotImplementedError()

//                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
//
//                val factory = SecretKeyFactory.getInstance("PBEwithHmacSHA256AndAES_256")
//                val spec: KeySpec = PBEKeySpec(password!!.toCharArray())
//                val tmp = factory.generateSecret(spec)
//                val secret = SecretKeySpec(tmp.encoded, "AES")
//
//                cipher.init(Cipher.ENCRYPT_MODE, secret)
//                CipherOutputStream(stream, cipher)
                } else {
                    stream
                }

                nestedStream.use {
                    stream.writeInt(messages.size)

                    messages.forEach { (message, type) ->
                        stream.writeInt(type.id)
                        stream.writeString(message)
                    }
                }
            }
        }
        currentThread.start()
    }
}