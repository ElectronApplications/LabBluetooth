package com.example.labbluetooth

import android.content.Context
import com.example.labbluetooth.database.DaoSession
import com.example.labbluetooth.database.Device

class MessageRepository(
    val device: Device,
    private val statsLoader: StatsLoader,
    private val daoSession: DaoSession,
    context: Context
) {
    private var messageLoader = MessageLoader(context, device.name)
    private var sentMessages = 0
    private var deletedMessages = 0

    val messages: List<Pair<String, MessageType>> get() = messageLoader.messages

    fun saveMessages() {
        messageLoader.save()
        daoSession.deviceDao.save(device)

        val deviceId = statsLoader.data.indexOfFirst { it.deviceName == device.name }
        if (deviceId != -1) {
            statsLoader.data[deviceId] = statsLoader.data[deviceId].copy(
                totalMessages = statsLoader.data[deviceId].totalMessages + sentMessages,
                deletedMessages = statsLoader.data[deviceId].deletedMessages + deletedMessages
            )
        } else {
            statsLoader.data.add(
                StatsLoader.Item(
                    deviceName = device.name,
                    totalMessages = sentMessages,
                    deletedMessages = deletedMessages
                )
            )
        }

        sentMessages = 0
        deletedMessages = 0

        statsLoader.save()
    }

    fun addMessage(message: String) {
        messageLoader.messages.add(Pair(message, MessageType.SENT))
        messageLoader.messages.add(Pair(message.reversed(), MessageType.RECEIVED))

        sentMessages += 2
        device.messagesAmount += 2
    }

    fun deleteMessage(position: Int) {
        messageLoader.messages.removeAt(position)
        deletedMessages += 1
    }
}