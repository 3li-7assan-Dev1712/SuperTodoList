package com.example.supertodolist.ui.task

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.supertodolist.data.Task
import com.example.supertodolist.databinding.TaskItemBinding

class TasksAdapter (private val listener: OnItemClickListener) : ListAdapter<Task, TasksAdapter.TaskViewHolder>(DiffCallback()) {

    inner class TaskViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root){

        init {
            binding.apply {
                root.setOnClickListener{
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }


                checkTaskDone.setOnClickListener{
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION){
                        val task = getItem(position)
                        listener.onCheckBoxClicked(task, checkTaskDone.isChecked)
                    }
                }
            }
        }
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

    interface OnItemClickListener{

        fun onItemClick(task: Task)

        fun onCheckBoxClicked(task: Task, checkedState: Boolean)

    }
}