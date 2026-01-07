package com.oc.maker.cat.emoji.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oc.maker.cat.emoji.data.local.dao.UserDao
import com.oc.maker.cat.emoji.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}