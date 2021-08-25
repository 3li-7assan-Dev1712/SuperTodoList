package com.example.supertodolist.ui.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supertodolist.R
import com.example.supertodolist.databinding.FragmentAddEditTaskBinding
import com.example.supertodolist.databinding.TasksFragmentBinding
import com.example.supertodolist.ui.TaskViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskFragment : Fragment (R.layout.tasks_fragment) {

    private val viewModel : TaskViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tasksAdapter = TasksAdapter()
        val binding = TasksFragmentBinding.bind(view)
        binding.apply {
            tasksRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = tasksAdapter
            }

        }
        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }
    }
}