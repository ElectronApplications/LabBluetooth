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
    private val deviceRepository = DeviceRepository(daoSession, application)
    private var messageRepository: MessageRepository? = null

    var username: String = "Guest"
        private set

    private var chats: List<Device> = deviceRepository.loadDevices()

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
        deviceRepository.createDevice(name)

        if (Random.nextFloat() > 0.75 && chats.isNotEmpty()) {
            val lastDevice = chats.last()
            deviceRepository.deleteDevice(lastDevice)
        }

        chats = deviceRepository.loadDevices()
        _uiState.value = _uiState.value?.copy(chats = chats)
    }

    fun saveCurrentMessages() {
        messageRepository?.saveMessages()
    }

    fun switchChat(deviceName: String) {
        saveCurrentMessages()
        try {
            val device = chats.first { it.name == deviceName }
            val repository = MessageRepository(device, statsLoader, daoSession, application)
            messageRepository = repository

            _uiState.value = _uiState.value?.copy(
                messages = repository.messages,
                tags = device.deviceTags
            )
        } catch (e: Exception) {
            e.printStackTrace()
            messageRepository = null
        }
    }

    fun addMessage(message: String) {
        messageRepository?.let { repository ->
            repository.addMessage(message)
            _uiState.value = _uiState.value?.copy(messages = repository.messages)
        }
    }

    fun deleteMessage(position: Int) {
        messageRepository?.let { repository ->
            repository.deleteMessage(position)
            _uiState.value = _uiState.value?.copy(messages = repository.messages)
        }
    }
}