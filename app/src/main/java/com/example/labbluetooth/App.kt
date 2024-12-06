package com.example.labbluetooth

import android.app.Application
import com.example.labbluetooth.database.DaoMaster
import com.example.labbluetooth.database.DaoSession

class App: Application() {
    lateinit var daoSession: DaoSession private set

    override fun onCreate() {
        super.onCreate()

        val helper = DaoMaster.DevOpenHelper(this, "bluetooth-chat-db")
        val db = helper.writableDb
        daoSession = DaoMaster(db).newSession()
    }
}