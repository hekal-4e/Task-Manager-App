package com.depi.taskmanagerapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.provider.Settings
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.depi.taskmanagerapp.R
import com.depi.taskmanagerapp.dataModel.TaskItem
import com.depi.taskmanagerapp.databinding.ActivityMainBinding
import com.depi.taskmanagerapp.notification.Notifications
import com.depi.taskmanagerapp.notification.channelId
import com.depi.taskmanagerapp.notification.messageExtra
import com.depi.taskmanagerapp.notification.notificationId
import com.depi.taskmanagerapp.notification.titleExtra
import com.depi.taskmanagerapp.viewmodel.TaskApplication
import com.depi.taskmanagerapp.viewmodel.TaskViewModel
import com.depi.taskmanagerapp.viewmodel.TaskViewModel.TaskItemModelFactory
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), TaskItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TaskViewModel by viewModels {
        TaskItemModelFactory(
            (application as TaskApplication).repository
        )
    }
    private lateinit var rv: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rv = findViewById(R.id.todoRecyclerView)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(this)
        rv.adapter = adapter

        viewModel.taskItems.observe(this) { list ->
            adapter.submitList(list.toMutableList())
        }

        binding.fab.setOnClickListener {
            NewTaskSheet(null).show(supportFragmentManager, "newTaskTag")
        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.todoRecyclerView)
        }

        createNotificationChannel()
        checkAndRequestPermissions()
    }

    override fun editTaskItem(taskItem: TaskItem) {
        NewTaskSheet(taskItem).show(supportFragmentManager, "newTaskTag")
    }

    override fun completeTaskItem(taskItem: TaskItem) {
        viewModel.setCompleted(taskItem)
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(
            viewHolder: RecyclerView.ViewHolder,
            direction: Int
        ) {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val task = adapter.differ.currentList[position]
                viewModel.deleteTaskItem(task)
                Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo") {
                        viewModel.addTaskItem(task)
                    }
                    show()
                }
            }
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleNotification(taskItem: TaskItem) {
        val intent = Intent(applicationContext, Notifications::class.java)
        intent.putExtra(titleExtra, taskItem.name)
        intent.putExtra(messageExtra, "Time to start: ${taskItem.desc}")
        val requestCode = if (taskItem.id == 0) System.currentTimeMillis().toInt()
        else taskItem.id
        intent.putExtra("notification_id", requestCode)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            requestCode,
            intent,
            flags
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = taskItem.dueTime()

        if (time != null) {
            val notificationTime = time.time - (15 * 60 * 1000)
            val currentTime = System.currentTimeMillis()

            Log.i("NotificationDebug", "Due time: ${time.time}," +
                    " notificationTime: $notificationTime, currentTime: $currentTime")

            if (notificationTime > currentTime) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        Toast.makeText(
                            this,
                            "Cannot schedule notification. 'Alarms & reminders' permission not granted.",
                            Toast.LENGTH_LONG
                        ).show()
                        checkAndRequestPermissions()
                        return
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime,
                        pendingIntent
                    )
                }
                Toast.makeText(this, "Notification scheduled.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    this,
                    "Task time is too soon or has passed. Notification not scheduled.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intent ->
                    intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    intent.data = Uri.fromParts("package", packageName, null)
                    startActivity(intent)
                    Toast.makeText(this, "Please grant 'Alarms & reminders' permission", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminders"
            val descriptionText = "Notifications for upcoming tasks"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}