package com.example.supertodolist.ui.deleteallcompleted

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supertodolist.data.TaskDao
import kotlinx.coroutines.launch


class DeleteAllCompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {
    fun deleteAllCompleted() = viewModelScope.launch {
        taskDao.deleteAllCompleted()
    }
}