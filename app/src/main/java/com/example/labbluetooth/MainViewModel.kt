package com.example.labbluetooth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.labbluetooth.database.DaoSession
import com.example.labbluetooth.database.Device
import com.example.labbluetooth.database.DeviceTag
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
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

@HiltViewModel
class MainViewModel @Inject constructor(
    private val daoSession: DaoSession,
    private val statsLoader: StatsLoader,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
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

    init {

    }

    fun changeUsername(newUsername: String) {
        username = newUsername
        _uiState.value = _uiState.value?.copy(username = newUsername)
    }

    fun exportStats(): Uri {
        return saveImage(context, generateStatsImage(statsLoader.data))
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
            val repository = MessageRepository(device, statsLoader, daoSession, context)
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