package com.example.labbluetooth

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.labbluetooth.database.DaoSession
import com.example.labbluetooth.database.Device
import com.example.labbluetooth.database.DeviceTag
import kotlin.random.Random

val defaultTags =
    arrayOf("Лучший друг", "Семья", "Друг", "Незнакомец", "sugar daddy", "sugar mommy")

data class MainUiState(
    val username: String,
    val stats: List<StatsLoader.Item>,
    val chats: List<Device>,
    val messages: List<Pair<String, MessageType>>?,
    val tags: List<DeviceTag>?
)

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    private val daoSession: DaoSession = (application as App).daoSession
    private val statsLoader = StatsLoader(application)

    var username: String = "Guest"
        private set

    private var chats: List<Device> = daoSession.deviceDao.loadAll()

    private var currentlySelected: Device? = null
    private var messageLoader: MessageLoader? = null
    private var sentMessages = 0
    private var deletedMessages = 0

    private val _uiState = MutableLiveData(
        MainUiState(
            username = username,
            stats = statsLoader.data,
            chats = chats,
            messages = null,
            tags = null
        )
    )
    val uiState: LiveData<MainUiState> get() = _uiState

    fun changeUsername(newUsername: String) {
        username = newUsername
        _uiState.value = _uiState.value?.copy(username = newUsername)
    }

    fun exportStats(): Uri {
        return saveImage(application, generateStatsImage(statsLoader.data))
    }

    fun updateDevices() {
        val name = Random.nextInt(0x1000000).toString(16)

        val device = Device(null, name, 0)
        daoSession.deviceDao.save(device)

        defaultTags.toList().shuffled().take(Random.nextInt(defaultTags.size))
            .forEach { tagName ->
                val tag = DeviceTag(null, tagName, device.id)
                daoSession.deviceTagDao.save(tag)
            }

        MessageLoader(application, name).save()

        if (Random.nextFloat() > 0.75 && chats.isNotEmpty()) {
            val lastDevice = chats.last()
            MessageLoader(application, lastDevice.name).delete()
            daoSession.deviceDao.delete(lastDevice)
        }

        chats = daoSession.deviceDao.loadAll()
        _uiState.value = _uiState.value?.copy(chats = chats)
    }

    fun saveCurrentMessages() {
        currentlySelected?.let { current ->
            daoSession.deviceDao.save(current)
            messageLoader?.save()

            val deviceId = statsLoader.data.indexOfFirst { it.deviceName == current.name }
            if (deviceId != -1) {
                statsLoader.data[deviceId] = statsLoader.data[deviceId].copy(
                    totalMessages = statsLoader.data[deviceId].totalMessages + sentMessages,
                    deletedMessages = statsLoader.data[deviceId].deletedMessages + deletedMessages
                )
            } else {
                statsLoader.data.add(
                    StatsLoader.Item(
                        deviceName = current.name,
                        totalMessages = sentMessages,
                        deletedMessages = deletedMessages
                    )
                )
            }
        }

        sentMessages = 0
        deletedMessages = 0

        statsLoader.save()
    }

    fun switchChat(deviceName: String) {
        saveCurrentMessages()
        try {
            val device = chats.first { it.name == deviceName }
            currentlySelected = device
            val loader = MessageLoader(application, device.name)
            messageLoader = loader

            _uiState.value = _uiState.value?.copy(
                messages = loader.messages,
                tags = device.deviceTags
            )
        } catch (e: Exception) {
            e.printStackTrace()
            currentlySelected = null
        }
    }

    fun addMessage(message: String) {
        messageLoader?.let { loader ->
            loader.messages.add(Pair(message, MessageType.SENT))
            loader.messages.add(Pair(message.reversed(), MessageType.RECEIVED))

            sentMessages += 2
            currentlySelected?.let { current ->
                current.messagesAmount += 2
            }

            _uiState.value = _uiState.value?.copy(messages = loader.messages)
        }
    }

    fun deleteMessage(position: Int) {
        messageLoader?.let { loader ->
            loader.messages.removeAt(position)
            deletedMessages += 1
            _uiState.value = _uiState.value?.copy(messages = loader.messages)
        }
    }
}