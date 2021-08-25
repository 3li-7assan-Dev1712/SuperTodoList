package com.example.supertodolist.ui.task

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.example.supertodolist.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskFragment : Fragment (R.layout.tasks_fragment) {

    private val viewModel : ViewModel by viewModels()
}