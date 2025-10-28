package com.depi.taskmanagerapp.dataModel

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.depi.taskmanagerapp.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "task_item_table")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "desc") var desc: String,
    @ColumnInfo(name = "dueTimeString") var dueTimeString: String?,
    @ColumnInfo(name = "completedDateString") var completedDateString: String?
) {

    fun completedDate(): Date? {
        return try {
            completedDateString?.let {
                dateFormatter.parse(it)
            }
        } catch (e: ParseException) {
            null
        }
    }

    fun dueTime(): Date? {
        return try {
            dueTimeString?.let {
                timeFormatter.parse(it)
            }
        } catch (e: ParseException) {
            null
        }
    }

    fun isCompleted(): Boolean = completedDate() != null

    fun imageResource(): Int =
        if (isCompleted()) R.drawable.check_circle else R.drawable.radio_button_unchecked

    fun imageColor(context: Context): Int =
        if (isCompleted()) purple(context) else black(context)

    private fun purple(context: Context) =
        ContextCompat.getColor(context, R.color.purple)

    private fun black(context: Context) =
        ContextCompat.getColor(context, R.color.black)

    companion object {
        @SuppressLint("ConstantLocale")
        val timeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}
