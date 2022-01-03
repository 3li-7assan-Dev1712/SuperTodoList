package com.example.supertodolist.ui

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.supertodolist.data.PreferencesManager
import com.example.supertodolist.data.Task
import com.example.supertodolist.data.TaskDao
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

    fun onTaskSelected(task: Task) {

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
    sealed class TasksEvent {
        data class ShowUndoTaskMessage(val task: Task): TasksEvent()
    }

}
enum class SortOrder { BY_DATE, BY_NAME }