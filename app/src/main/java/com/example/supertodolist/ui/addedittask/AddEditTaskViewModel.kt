package com.example.supertodolist.ui.addedittask

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supertodolist.data.Task
import com.example.supertodolist.data.TaskDao
import kotlinx.coroutines.launch

class AddEditTaskViewModel @ViewModelInject constructor(
    val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
): ViewModel() {
    fun onFabSaveTaskClick() {
        if (taskName.isBlank()) {
            // show input valid message
        }
        if (task != null) {
            // update the task
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
            // navigate back
        } else {
            // create new task
            val newTask = Task(name  = taskName, important = taskImportance)
            addNewTask(newTask)
            // navigate back
        }
    }

    private fun updateTask(updatedTask: Task) {

        viewModelScope.launch {
            taskDao.update(updatedTask)
        }
    }

    private fun addNewTask(newTask: Task) {
        viewModelScope.launch {
            taskDao.insert(newTask)
        }

    }

    val task = state.get<Task>("task")
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }
    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
    set(value) {
        field = value
        state.set("taskImportance", value)
    }

}