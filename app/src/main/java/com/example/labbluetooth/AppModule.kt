package com.example.labbluetooth

import android.content.Context
import com.example.labbluetooth.database.DaoMaster
import com.example.labbluetooth.database.DaoSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDaoSession(@ApplicationContext context: Context): DaoSession {
        val helper = DaoMaster.DevOpenHelper(context, "bluetooth-chat-db")
        val db = helper.writableDb
        return DaoMaster(db).newSession()
    }
}