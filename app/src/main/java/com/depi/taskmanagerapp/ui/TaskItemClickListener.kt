package com.depi.taskmanagerapp.ui

import com.depi.taskmanagerapp.dataModel.TaskItem

interface TaskItemClickListener {
    fun editTaskItem(taskItem: TaskItem)
    fun completeTaskItem(taskItem: TaskItem)
}