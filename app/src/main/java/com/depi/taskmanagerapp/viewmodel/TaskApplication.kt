package com.depi.taskmanagerapp.viewmodel

import android.app.Application
import com.depi.taskmanagerapp.db.TaskItemDB
import com.depi.taskmanagerapp.repository.TaskItemRepository

class TaskApplication : Application() {

    val database: TaskItemDB by lazy { TaskItemDB(this) }

    val repository: TaskItemRepository by lazy {
        TaskItemRepository(database.taskItemDao())
    }
}
