package com.example.supertodolist.ui.task

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.supertodolist.R
import com.example.supertodolist.data.PreferencesManager
import com.example.supertodolist.data.Task
import com.example.supertodolist.databinding.FragmentAddEditTaskBinding
import com.example.supertodolist.databinding.TasksFragmentBinding
import com.example.supertodolist.ui.SortOrder
import com.example.supertodolist.ui.TaskViewModel
import com.example.supertodolist.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskFragment : Fragment (R.layout.tasks_fragment) , TasksAdapter.OnItemClickListener{

    private val viewModel : TaskViewModel by viewModels()

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClicked(task: Task, checkedState: Boolean) {
        viewModel.onCheckBoxClicked(task, checkedState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tasksAdapter = TasksAdapter(this)
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
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.onQueryTextChanged {
            // update search query
            viewModel.searchQuery.value = it


        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_sort_by_name -> {
                viewModel.sortOrder.value = SortOrder.BY_NAME
                true
            }
            R.id.action_sort_by_date_created-> {
                viewModel.sortOrder.value = SortOrder.BY_DATE
                true
            }
            R.id.action_hide_completed_task-> {
                item.isChecked = !item.isChecked
                viewModel.hideCompleted.value = item.isChecked
                true
            }
            R.id.action_delete_all_completed_task -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    }
}