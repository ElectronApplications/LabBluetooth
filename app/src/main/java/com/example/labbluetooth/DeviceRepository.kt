package com.example.labbluetooth

import android.content.Context
import com.example.labbluetooth.database.DaoSession
import com.example.labbluetooth.database.Device
import com.example.labbluetooth.database.DeviceTag
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.random.Random

class DeviceRepository @Inject constructor(
    private val daoSession: DaoSession,
    @ApplicationContext private val context: Context
) {
    fun createDevice(name: String) {
        val device = Device(null, name, 0)
        daoSession.deviceDao.save(device)

        defaultTags.toList().shuffled().take(Random.nextInt(defaultTags.size))
            .forEach { tagName ->
                val tag = DeviceTag(null, tagName, device.id)
                daoSession.deviceTagDao.save(tag)
            }

        MessageLoader(context, name).save()
    }

    fun deleteDevice(device: Device) {
        MessageLoader(context, device.name).delete()
        daoSession.deviceDao.delete(device)
    }

    fun loadDevices(): List<Device> {
        return daoSession.deviceDao.loadAll()
    }
}