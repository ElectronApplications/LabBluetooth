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
    val chats: List<Device>
)

class MainViewModel(private val application: Application) : AndroidViewModel(application) {
    private val daoSession: DaoSession = (application as App).daoSession
    private val statsLoader = StatsLoader(application)

    var username: String = "Guest"
        private set

    private var chats: List<Device> = daoSession.deviceDao.loadAll()

    private val _uiState = MutableLiveData(
        MainUiState(
            username = username,
            stats = statsLoader.data,
            chats = chats
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
}