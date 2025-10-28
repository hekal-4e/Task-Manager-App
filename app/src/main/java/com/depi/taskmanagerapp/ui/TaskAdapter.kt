package com.depi.taskmanagerapp.ui

import android.content.Context
import android.graphics.Paint
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.depi.taskmanagerapp.R
import com.depi.taskmanagerapp.dataModel.TaskItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskAdapter(
    private val clickListener: TaskItemClickListener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(
        val context: Context,
        itemView: View,
        val clickListener: TaskItemClickListener
    ) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.taskName)
        val dueTime: TextView = itemView.findViewById(R.id.dueTime)
        val completeButton: ImageButton = itemView.findViewById(R.id.completeButton)
        val taskCellContainer: CardView = itemView.findViewById(R.id.taskCellContainer)
    }

    private val differCallback = object : DiffUtil.ItemCallback<TaskItem>() {
        override fun areItemsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskItem, newItem: TaskItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list: List<TaskItem>?) {
        differ.submitList(list?.toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)
        return TaskViewHolder(parent.context, view, clickListener)
    }
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = differ.currentList[position]
        
        holder.taskName.text = task.name
        holder.dueTime.text = task.dueTimeString ?: ""

        updateTaskAppearance(holder, task.isCompleted())
        updateCompleteButton(holder, task)

        holder.completeButton.setOnClickListener {
            holder.clickListener.completeTaskItem(task)
        }

        holder.taskCellContainer.setOnClickListener {
            if (!task.isCompleted()) {
                holder.clickListener.editTaskItem(task)
            }
        }
    }

    override fun getItemCount() = differ.currentList.size

    private fun updateTaskAppearance(holder: TaskViewHolder, isCompleted: Boolean) {
        if (isCompleted) {
            holder.taskName.paintFlags = holder.taskName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.dueTime.paintFlags = holder.dueTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskName.alpha = 0.6f
            holder.dueTime.alpha = 0.6f
        } else {
            holder.taskName.paintFlags = holder.taskName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.dueTime.paintFlags = holder.dueTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.taskName.alpha = 1.0f
            holder.dueTime.alpha = 1.0f
        }
    }

    private fun updateCompleteButton(holder: TaskViewHolder, task: TaskItem) {
        val isCompleted = task.isCompleted()
        
        val drawableRes = if (isCompleted) {
            R.drawable.check_circle
        } else {
            R.drawable.radio_button_unchecked
        }
        
        holder.completeButton.setImageResource(drawableRes)
        
        val colorRes = if (isCompleted) {
            android.R.color.holo_green_dark
        } else {
            android.R.color.darker_gray
        }
        
        holder.completeButton.setColorFilter(
            holder.context.getColor(colorRes),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }
}
