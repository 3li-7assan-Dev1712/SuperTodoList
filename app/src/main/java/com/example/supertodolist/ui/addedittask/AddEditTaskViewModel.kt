package com.example.supertodolist.ui.addedittask

import android.app.Activity
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supertodolist.data.Task
import com.example.supertodolist.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


const val ADD_TASK_OK = Activity.RESULT_FIRST_USER
const val EDIT_TASK_OK = Activity.RESULT_FIRST_USER + 1

class AddEditTaskViewModel @ViewModelInject constructor(
    val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {
    val addEditTasksEventsChannel = Channel<AddEditTaskEvents>()
    val addEditTaskEvents = addEditTasksEventsChannel.receiveAsFlow()

    fun onFabSaveTaskClick() {
        if (taskName.isBlank()) {
            // show input valid message
            showInvalidMessage("name cannot be empty")
            return
        }
        if (task != null) {
            // update the task
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
            // navigate back
        } else {
            // create new task
            val newTask = Task(name = taskName, important = taskImportance)
            addNewTask(newTask)
            // navigate back
        }
    }

    private fun showInvalidMessage(msg: String) = viewModelScope.launch {
        addEditTasksEventsChannel.send(AddEditTaskEvents.ShowInvalidMessage(msg))
    }

    private fun updateTask(updatedTask: Task) {

        viewModelScope.launch {
            taskDao.update(updatedTask)
            addEditTasksEventsChannel.send(AddEditTaskEvents.NavigateBackWithResult(EDIT_TASK_OK))
        }
    }

    private fun addNewTask(newTask: Task) {
        viewModelScope.launch {
            taskDao.insert(newTask)
            addEditTasksEventsChannel.send(AddEditTaskEvents.NavigateBackWithResult(ADD_TASK_OK))
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
        } sealed class AddEditTaskEvents {
        data class ShowInvalidMessage(val msg: String) : AddEditTaskEvents()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvents()
    }


}