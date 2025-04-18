package com.example.labbluetooth

import android.app.Application
import com.example.labbluetooth.database.DaoSession
import com.example.labbluetooth.database.Device
import com.example.labbluetooth.database.DeviceTag
import kotlin.random.Random

class DeviceRepository(private val daoSession: DaoSession, private val application: Application) {
    fun createDevice(name: String) {
        val device = Device(null, name, 0)
        daoSession.deviceDao.save(device)

        defaultTags.toList().shuffled().take(Random.nextInt(defaultTags.size))
            .forEach { tagName ->
                val tag = DeviceTag(null, tagName, device.id)
                daoSession.deviceTagDao.save(tag)
            }

        MessageLoader(application, name).save()
    }

    fun deleteDevice(device: Device) {
        MessageLoader(application, device.name).delete()
        daoSession.deviceDao.delete(device)
    }

    fun loadDevices(): List<Device> {
        return daoSession.deviceDao.loadAll()
    }
}