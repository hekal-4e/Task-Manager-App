package com.depi.taskmanagerapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.depi.taskmanagerapp.dataModel.TaskItem

@Database(entities = [TaskItem::class], version = 1, exportSchema = true)
abstract class TaskItemDB: RoomDatabase() {
    abstract fun taskItemDao() : DAO

    companion object {
        @Volatile
        private var INSTANCE: TaskItemDB? = null
        private val LOCK = Any()

        operator fun invoke(c: Context) = INSTANCE ?: synchronized(LOCK) {
            INSTANCE ?: createDB(c).also { INSTANCE = it }
        }

        private fun createDB(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            TaskItemDB::class.java,
            "Task_Item_DB"
        ).build()
    }

}