package com.example.supertodolist.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.supertodolist.data.PreferencesManager
import com.example.supertodolist.data.Task
import com.example.supertodolist.data.TaskDao
import com.example.supertodolist.ui.addedittask.ADD_TASK_OK
import com.example.supertodolist.ui.addedittask.EDIT_TASK_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
        ) : ViewModel()
{


    private val tasksEventChanel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChanel.receiveAsFlow()

    val searchQuery = state.getLiveData("searchQuery", "")
    private val preferencesFlow = preferencesManager.preferencesFlow

    private val taskFlow = combine(searchQuery.asFlow(),
    preferencesFlow) { query, filterPreferences ->
        Pair (query, filterPreferences)
    }.flatMapLatest { (query, filter) ->
        taskDao.getTasks(query, filter.sortOrder, filter.hideCompleted)
    }
    val tasks = taskFlow.asLiveData()

    fun onCheckBoxClicked(task: Task, checkedState: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy( completed = checkedState ))
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChanel.send(TasksEvent.NavigateToEditTask(task))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChanel.send(TasksEvent.ShowUndoTaskMessage(task))
    }

    fun updateSortOrder (sortOrder: SortOrder) {
        viewModelScope.launch {
            preferencesManager.updateSortOrder(sortOrder)
        }
    }
    fun updateHideCompleted(hideCompleted: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateHideCompleted(hideCompleted)
        }
    }
    fun onUndoDeleteTaskClick(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }
    fun onAddTaskFabClick() {
        viewModelScope.launch {
            tasksEventChanel.send(TasksEvent.NavigateToAddTask)
        }
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_OK -> {
                showConfirmationMessage("task added")
            }
            EDIT_TASK_OK -> {
                showConfirmationMessage("task updated")
            }
        }
    }

    private fun showConfirmationMessage(confirmationMsg: String) {
        viewModelScope.launch {
            tasksEventChanel.send(TasksEvent.ShowConfirmationMsg(msg = confirmationMsg))
        }

    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChanel.send(TasksEvent.ShowDeleteAllCompletedMessage)
    }


    sealed class TasksEvent {
        data class ShowUndoTaskMessage(val task: Task): TasksEvent()
        data class NavigateToEditTask(val task: Task): TasksEvent()
        data class ShowConfirmationMsg(val msg: String): TasksEvent()
        object ShowDeleteAllCompletedMessage : TasksEvent()

        object NavigateToAddTask: TasksEvent()
    }

}
enum class SortOrder { BY_DATE, BY_NAME }