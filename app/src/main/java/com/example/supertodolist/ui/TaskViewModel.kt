package com.example.supertodolist.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.supertodolist.data.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
        ) : ViewModel()
{
    val searchQuery = MutableStateFlow("")
    private val taskFlow = searchQuery.flatMapLatest {
        taskDao.getTasks(it)
    }
    val tasks = taskFlow.asLiveData()
}