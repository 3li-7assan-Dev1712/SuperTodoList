package com.example.supertodolist.ui.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.supertodolist.data.Task
import com.example.supertodolist.databinding.TaskItemBinding

class TasksAdapter  : ListAdapter<Task, TasksAdapter.TaskViewHolder>(DiffCallback()) {

    class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind (task: Task){
            binding.apply {
                taskName.text = task.name
                checkTaskDone.isChecked = task.completed
                importantTaskSign.isVisible = task.important
                taskName.paint.isStrikeThruText = task.completed
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = getItem(position)
        holder.bind(currentTask)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task> () {
        override fun areItemsTheSame(oldItem: Task, newItem: Task)
            = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task)
            = oldItem == newItem

    }
}