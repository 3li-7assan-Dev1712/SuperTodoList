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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * This is the ViewModel class to apply the MVVM architecture, it will handle all
 * the delegated work from the TaskFragment.
 */
class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {


    // the Channel is used to make communication between the fragment and the ViewModel
    // the fragment tell teh ViewModel about the interaction and the VieModel tell the fragment
    // what to do.
    private val tasksEventChanel = Channel<TasksEvent>()
    // change the channel into flow to be able to be collected from the fragment
    val tasksEvent = tasksEventChanel.receiveAsFlow()

    val searchQuery = state.getLiveData("searchQuery", "")
    private val preferencesFlow = preferencesManager.preferencesFlow

    /*
     * Flow is a kotlin language feature to apply to the concept of reactive programming
     * which means that keep track of a specific source of data and listen to any changes
     * then react accordingly.
     *
     */
    // combine the two flows and get the latest values to be used for getting
    // the desired data from the database.
    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filter) ->
        taskDao.getTasks(query, filter.sortOrder, filter.hideCompleted)
    }
    // make a LiveData version from the database to be used in the fragment; because it is
    // a lifecycle observer -will not trigger changes when the lifecycle of the fragment is destroyed
    // or stopped-.
    val tasks = taskFlow.asLiveData()

    /*
     the fragment receive the user inputs and interaction, then tell the ViewModel about them
     the ViewModel make the decision; here when the checkbox of a task is clicked the view model
     update the task in the database to be completed
     */
    fun onCheckBoxClicked(task: Task, checkedState: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = checkedState))
    }

    /*
    This function will be called when the user click in a task, so that the view model
    tell the fragment to navigate to the AddEditTaskFragment.
     */
    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChanel.send(TasksEvent.NavigateToEditTask(task))
    }

    /*
    When the user swipe a task in the tasks list, the fragment tell this ViewModel, then this ViewModel
    delete the task from the database, plus tell the fragment to show an UNDO message using the
    flow Channel.
     */
    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChanel.send(TasksEvent.ShowUndoTaskMessage(task))
    }

    /*
    this function for updating the sort order of the tasks list, the user select the sort order
    from the fragment menu
     */
    fun updateSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            preferencesManager.updateSortOrder(sortOrder)
        }
    }

    // from the TaskFragment menu the user click in hide all completed, then this function will be called.
    fun updateHideCompleted(hideCompleted: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateHideCompleted(hideCompleted)
        }
    }

    /*
    When the user delete a task, a SnackBar appear directly to provide a feedback along with
    an UNDO button to undo the deletion. This function will be invoked when the UNDO
    is clicked.
     */
    fun onUndoDeleteTaskClick(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    /*
    The FAB in the TaskFragment to add new tasks, when it is clicked
    the method is called, so that the ViewModel tell the TaskFramgnet
    what to do.
     */
    fun onAddTaskFabClick() {
        viewModelScope.launch {
            tasksEventChanel.send(TasksEvent.NavigateToAddTask)
        }
    }

    /*
    When the user add/edit a task the method is called holing a flag indicating
    to the operation (add or edit a task).
     */
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

    /*
    this method is just responsible for telling the TaskFragment to show a confirmation message
    by using the Flow Channel.
     */
    private fun showConfirmationMessage(confirmationMsg: String) {
        viewModelScope.launch {
            tasksEventChanel.send(TasksEvent.ShowConfirmationMsg(msg = confirmationMsg))
        }

    }

    /*
    When the method is called it will tell the TaskFragment to make a dialog to ask
    a user if they really want to delete all completed tasks.
     */
    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChanel.send(TasksEvent.ShowDeleteAllCompletedMessage)
    }


    /*
    sealed class (sealed means closed) it is a closed class meaning that we don't make objects out of
    it; it is useful when we want to make subclass inherit from it and at the same time we don't
    make objects out of it.
     */
    sealed class TasksEvent {
        data class ShowUndoTaskMessage(val task: Task) : TasksEvent()
        data class NavigateToEditTask(val task: Task) : TasksEvent()
        data class ShowConfirmationMsg(val msg: String) : TasksEvent()
        object ShowDeleteAllCompletedMessage : TasksEvent()

        object NavigateToAddTask : TasksEvent()
    }

}
// basic enum for the SortOrder of the tasks.
enum class SortOrder { BY_DATE, BY_NAME }