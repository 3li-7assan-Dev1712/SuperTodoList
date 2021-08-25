package com.example.supertodolist.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.supertodolist.data.TaskDao

class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
        ) : ViewModel() {
}