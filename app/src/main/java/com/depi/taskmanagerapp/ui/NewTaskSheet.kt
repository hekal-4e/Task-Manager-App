package com.depi.taskmanagerapp.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.depi.taskmanagerapp.dataModel.TaskItem
import com.depi.taskmanagerapp.databinding.FragmentNewTaskSheetBinding
import com.depi.taskmanagerapp.viewmodel.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Calendar
import java.util.Date

class NewTaskSheet(var taskItem: TaskItem?) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentNewTaskSheetBinding
    private val viewModel: TaskViewModel by activityViewModels()
    private var dueTime: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewTaskSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.saveBtn.setOnClickListener {
            saveTask()
        }
        binding.timePickerButton.setOnClickListener {
            openTimePicker()
        }

        if (taskItem != null) {
            binding.taskTitle.text = "Edit Task"
            binding.name.setText(taskItem!!.name)
            binding.desc.setText(taskItem!!.desc)

            if (taskItem!!.dueTime() != null) {
                dueTime = taskItem!!.dueTime()!!
                updateTimeButtonText()
            }
        }
    }

    private fun getCalenderFromDueTime(): Calendar {
        val calender = Calendar.getInstance()
        dueTime?.let { calender.time = it }
        return calender
    }

    private fun updateTimeButtonText() {
        val calender = getCalenderFromDueTime()
        binding.timePickerButton.text = String.format(
            "%02d:%02d",
            calender.get(Calendar.HOUR_OF_DAY),
            calender.get(Calendar.MINUTE)
        )
    }

    private fun openTimePicker() {
        val dialogCalendar = getCalenderFromDueTime()

        val listener = TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
            val newCal = getCalenderFromDueTime()
            newCal.set(Calendar.HOUR_OF_DAY, selectedHour)
            newCal.set(Calendar.MINUTE, selectedMinute)
            newCal.set(Calendar.SECOND, 0)
            newCal.set(Calendar.MILLISECOND, 0)

            if (newCal.timeInMillis <= System.currentTimeMillis()) {
                newCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            dueTime = newCal.time
            updateTimeButtonText()
        }
        val is24Hour = DateFormat.is24HourFormat(activity) // Automatically uses 12/24hr setting
        val dialog = TimePickerDialog(
            activity,
            listener,
            dialogCalendar.get(Calendar.HOUR_OF_DAY),
            dialogCalendar.get(Calendar.MINUTE),
            is24Hour // This makes it respect the user's phone settings
        )
        dialog.setTitle("Task Time")
        dialog.show()
    }

    private fun saveTask() {
        val taskName = binding.name.text.toString()
        val taskDescription = binding.desc.text.toString()

        if (taskName.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a task name", Toast.LENGTH_SHORT).show()
            return
        }

        val dueTimeString = if (dueTime == null) {
            Toast.makeText(requireContext(), "Please select a time!", Toast.LENGTH_SHORT).show()
            return
        } else {
            TaskItem.timeFormatter.format(dueTime)
        }

        if (taskItem == null) {
            val newTask = TaskItem(
                name = taskName,
                desc = taskDescription,
                dueTimeString = dueTimeString,
                completedDateString = null
            )
            viewModel.addTaskItem(newTask)
            (activity as? MainActivity)?.scheduleNotification(newTask)
        } else {
            val updatedTask = taskItem!!.copy(
                name = taskName,
                desc = taskDescription,
                dueTimeString = dueTimeString
            )
            viewModel.updateTaskItem(updatedTask)
            (activity as? MainActivity)?.scheduleNotification(updatedTask)
        }

        binding.apply {
            name.text?.clear()
            desc.text?.clear()
        }
        dismiss()
    }
}