package com.depi.taskmanagerapp.repository

import androidx.annotation.WorkerThread
import com.depi.taskmanagerapp.dataModel.TaskItem
import com.depi.taskmanagerapp.db.DAO
import com.depi.taskmanagerapp.db.TaskItemDB
import kotlinx.coroutines.flow.Flow

class TaskItemRepository(private val taskItemDao: DAO) {
    val allTaskItems: Flow<List<TaskItem>> = taskItemDao.allTaskItems()

    @WorkerThread
    suspend fun insertTaskItem(taskItem: TaskItem) = taskItemDao.insertTaskItem(taskItem)

    @WorkerThread
    suspend fun updateTaskItem(taskItem: TaskItem) = taskItemDao.updateTaskItem(taskItem)

    @WorkerThread
    suspend fun deleteTaskItem(taskItem: TaskItem) = taskItemDao.deleteTaskItem(taskItem)
}