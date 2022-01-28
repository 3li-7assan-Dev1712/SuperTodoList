package com.example.supertodolist.ui.task

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.supertodolist.R
import com.example.supertodolist.data.Task
import com.example.supertodolist.databinding.TasksFragmentBinding
import com.example.supertodolist.ui.SortOrder
import com.example.supertodolist.ui.TaskViewModel
import com.example.supertodolist.util.exhaustive
import com.example.supertodolist.util.onQueryTextChanged
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.tasks_fragment), TasksAdapter.OnItemClickListener {

    // we make this global property, so we can access in all the file
    private lateinit var searchView: SearchView

    private val viewModel: TaskViewModel by viewModels()

    // listen to the user interaction and delegate the work to the ViewModel
    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    // listen to the user interaction then let the ViewModel do the business logic.
    override fun onCheckBoxClicked(task: Task, checkedState: Boolean) {
        viewModel.onCheckBoxClicked(task, checkedState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tasksAdapter = TasksAdapter(this)
        val binding = TasksFragmentBinding.bind(view)
        setFragmentResultListener("add_edit_task_request") { _, bundle ->
            val result = bundle.getInt("add_edit_task_result_flag")
            viewModel.onAddEditResult(result)
        }

        binding.apply {
            tasksRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                adapter = tasksAdapter
            }

            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(tasksRecycler)
        }
        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        // all business logic we delegate it the ViewModel which will in tern
        // tell the fragment what to do, and we collect every command from the ViewModel
        // below:
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    /*
                     * when the user click undo in the SnackBar the fragment say for the ViewModel
                     * the user clicks in the UNDO button, the ViewModel judge what to do
                     * then tell the fragment about it which we collect it via Channel using
                     * Flow Kotlin's feature.
                     */
                    is TaskViewModel.TasksEvent.ShowUndoTaskMessage -> {
                        Snackbar.make(requireView(), "task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteTaskClick(event.task)
                            }.show()
                    }
                    /*
                    when the user click to Add task fab in the screen, the fragment tell the ViewModel
                    about that then the ViewModel tell the fragment the action that should be taken.
                     */
                    TaskViewModel.TasksEvent.NavigateToAddTask -> {
                        val action =
                            TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(
                                null,
                                "New Task"
                            )
                        findNavController().navigate(action)
                    }
                    /*
                    When the user click to a specific task intending to edit it, the fragment delegate that
                    to the ViewModel, then the ViewModel tell the fragment to do this:
                     */
                    is TaskViewModel.TasksEvent.NavigateToEditTask -> {
                        val action =
                            TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(
                                event.task,
                                "Edit Task"
                            )
                        findNavController().navigate(action)
                    }
                    /*
                    Confirmation message should be appeared when the user click to (delete all completed)
                    from the main menu at the top of the screen, so the fragment tell the ViewModel about
                    that then the ViewModel make the decision to be applied as below:
                     */
                    is TaskViewModel.TasksEvent.ShowConfirmationMsg -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                    }
                    TaskViewModel.TasksEvent.ShowDeleteAllCompletedMessage -> {
                        val action =
                            TaskFragmentDirections.actionGlobalDeleteAllCompletedFragmentDialog()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        // when the user click on the fab, tell the ViewModel about it to make the decision
        binding.fabAddTask.setOnClickListener {
            viewModel.onAddTaskFabClick()
        }
        // this method will let our fragment has its own custom menu :)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            // expandActionView make the search item on the menu appear just like when the user
            // click to make a search
            searchItem.expandActionView()
            // write the string that was written before in the searchView
            searchView.setQuery(pendingQuery, false)
        }
        searchView = searchItem.actionView as SearchView
        searchView.onQueryTextChanged {
            // update search query
            viewModel.searchQuery.value = it


        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            /* all the work is delegated to the ViewModel to have a better architecture and
            separation of concern the fragment here is just responsible for receiving inputs from
            the user -tell as what the user click in the menu-, then the logic and the rest of
            the flow is the ViewModel's responsibility.
             */
            R.id.action_sort_by_name -> {
                viewModel.updateSortOrder(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.updateSortOrder(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_task -> {
                item.isChecked = !item.isChecked
                viewModel.updateHideCompleted(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_task -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        // remove the listener from the SearchView when its fragment is destroyed.
        searchView.setOnQueryTextListener(null)
    }
}