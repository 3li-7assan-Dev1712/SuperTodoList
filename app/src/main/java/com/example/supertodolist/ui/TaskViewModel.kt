package com.example.supertodolist.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.supertodolist.data.Task
import com.example.supertodolist.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
        ) : ViewModel()
{


    val searchQuery = MutableStateFlow("")
    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    val hideCompleted = MutableStateFlow(false)

    private val taskFlow = combine(searchQuery,
    sortOrder,
    hideCompleted) {
        query, sortOrder, hideCompleted ->
        Triple (query, sortOrder, hideCompleted)
    }.flatMapLatest { (query, sortOrder, hideCompleted) ->
        taskDao.getTasks(query, sortOrder, hideCompleted)
    }
    val tasks = taskFlow.asLiveData()

    fun onCheckBoxClicked(task: Task, checkedState: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy( completed = checkedState ))
    }

    fun onTaskSelected(task: Task) {

    }

}
enum class SortOrder { BY_DATE, BY_NAME }