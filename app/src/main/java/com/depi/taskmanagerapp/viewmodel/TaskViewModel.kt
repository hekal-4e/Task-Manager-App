package com.depi.taskmanagerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.depi.taskmanagerapp.dataModel.TaskItem
import com.depi.taskmanagerapp.repository.TaskItemRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class TaskViewModel(private val repo: TaskItemRepository) : ViewModel() {
    private val _taskItems: LiveData<List<TaskItem>>
        = repo.allTaskItems.asLiveData()
    val taskItems: LiveData<List<TaskItem>> = _taskItems

    fun addTaskItem(newTask: TaskItem) = viewModelScope.launch {
        repo.insertTaskItem(newTask)
    }

    fun updateTaskItem(taskItem: TaskItem) = viewModelScope.launch {
        repo.updateTaskItem(taskItem)
    }

    fun deleteTaskItem(taskItem: TaskItem) = viewModelScope.launch {
        repo.deleteTaskItem(taskItem)
    }

    fun setCompleted(taskItem: TaskItem) = viewModelScope.launch {
        val isCompleted = !taskItem.isCompleted()
        val updatedTask = taskItem.copy(
            completedDateString = if (isCompleted) {
                TaskItem.dateFormatter.format(Calendar.getInstance().time)
            } else {
                null
            }
        )
        repo.updateTaskItem(updatedTask)
    }

    class TaskItemModelFactory(private val repository: TaskItemRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java))
                return TaskViewModel(repository) as T

            throw IllegalArgumentException("Unknown Class For View Model")
        }
    }
}
