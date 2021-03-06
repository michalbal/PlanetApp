package net.planner.planetapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD
import com.google.android.material.timepicker.TimeFormat
import net.planner.planetapp.*
import net.planner.planetapp.databinding.PreferenceTimeItemBinding
import java.util.HashMap


/**
 * View adapter for Time settings of a preference.
 * Holds values of the structure: ArrayList<PreferenceTimeRep>
 */
class PreferenceTimeViewAdapter(
    private var values: ArrayList<PreferenceTimeRep>,
    private val fm: FragmentManager,
    private var isEditable: Boolean = true
) : RecyclerView.Adapter<PreferenceTimeViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreferenceTimeViewAdapter.ViewHolder {

        return ViewHolder(
            PreferenceTimeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    fun updateIsEditable(isEditableNew: Boolean) {
        isEditable = isEditableNew
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: PreferenceTimeViewAdapter.ViewHolder, position: Int) {
        val item = values[position]

        initView(holder, item)

        holder.startTimeHour.setOnClickListener { view ->
            // Show time picker dialog
            var hour = 9
            var minutes = 0
            try {
                val splitted = item.endHour.split(":")
                hour = splitted[0].trim().toInt()
                minutes = splitted[1].trim().toInt()
            } catch (e: Exception) {
                minutes = 0
            }

            val picker =
                MaterialTimePicker.Builder()
                    .setInputMode(INPUT_MODE_KEYBOARD)
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(hour)
                    .setMinute(minutes)
                    .build()

            picker.addOnPositiveButtonClickListener {
                item.startHour = createTimeString(picker.hour, picker.minute)
                holder.startTimeHour.text = item.startHour
            }

            picker.show(fm, "Choose start time:")
        }

        holder.endTimeHour.setOnClickListener { view ->
            // Show time picker dialog
            var hour = 12
            var minutes = 0
            try {
                val splitted = item.endHour.split(":")
                hour = splitted[0].trim().toInt()
                minutes = splitted[1].trim().toInt()
            } catch (e: Exception) {
                minutes = 0
            }

            val picker =
                MaterialTimePicker.Builder()
                    .setInputMode(INPUT_MODE_KEYBOARD)
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(hour)
                    .setMinute(minutes)
                    .build()

            picker.addOnPositiveButtonClickListener {
                item.endHour = createTimeString(picker.hour, picker.minute)
                holder.endTimeHour.setText(item.endHour)
            }

            picker.show(fm, "Choose end time:")
        }

        holder.removeButton.setOnClickListener { view ->
            removeTime(item)
        }

        holder.sunCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!item.daysAppliesTo.contains(SUNDAY)) {
                    item.daysAppliesTo.add(SUNDAY)
                }
            } else {
                if (item.daysAppliesTo.contains(SUNDAY)) {
                    item.daysAppliesTo.remove(SUNDAY)
                }
            }
        }

        holder.monCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!item.daysAppliesTo.contains(MONDAY)) {
                    item.daysAppliesTo.add(MONDAY)
                }
            } else {
                if (item.daysAppliesTo.contains(MONDAY)) {
                    item.daysAppliesTo.remove(MONDAY)
                }
            }
        }

        holder.tuCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!item.daysAppliesTo.contains(TUESDAY)) {
                    item.daysAppliesTo.add(TUESDAY)
                }
            } else {
                if (item.daysAppliesTo.contains(TUESDAY)) {
                    item.daysAppliesTo.remove(TUESDAY)
                }
            }
        }

        holder.weCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!item.daysAppliesTo.contains(WEDNESDAY)) {
                    item.daysAppliesTo.add(WEDNESDAY)
                }
            } else {
                if (item.daysAppliesTo.contains(WEDNESDAY)) {
                    item.daysAppliesTo.remove(WEDNESDAY)
                }
            }
        }

        holder.thCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!item.daysAppliesTo.contains(THURSDAY)) {
                    item.daysAppliesTo.add(THURSDAY)
                }
            } else {
                if (item.daysAppliesTo.contains(THURSDAY)) {
                    item.daysAppliesTo.remove(THURSDAY)
                }
            }
        }

        holder.friCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!item.daysAppliesTo.contains(FRIDAY)) {
                    item.daysAppliesTo.add(FRIDAY)
                }
            } else {
                if (item.daysAppliesTo.contains(FRIDAY)) {
                    item.daysAppliesTo.remove(FRIDAY)
                }
            }
        }

        holder.satCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (!item.daysAppliesTo.contains(SATURDAY)) {
                    item.daysAppliesTo.add(SATURDAY)
                }
            } else {
                if (item.daysAppliesTo.contains(SATURDAY)) {
                    item.daysAppliesTo.remove(SATURDAY)
                }
            }
        }
    }

    private fun createPickStartHourDialog(item: PreferenceTimeRep, holder: ViewHolder ) {
        val splitted = item.startHour.split(":")
        val hour = splitted[0].toInt()
        val minutes = splitted[1].toInt()

        val picker =
            MaterialTimePicker.Builder()
                .setInputMode(INPUT_MODE_KEYBOARD)
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minutes)
                .build()

        picker.addOnPositiveButtonClickListener {
            item.startHour = "${picker.hour}:${picker.minute}"
            holder.startTimeHour.setText(item.startHour)
        }

        picker.show(fm, "Choose start time:")
    }

    private fun initView(holder: ViewHolder, item: PreferenceTimeRep) {
        holder.startTimeHour.text = item.startHour
        holder.endTimeHour.text = item.endHour

        holder.startTimeHour.isClickable = isEditable
        holder.endTimeHour.isClickable = isEditable

        holder.removeButton.isClickable = isEditable
        if (isEditable) {
            holder.removeButton.visibility = View.VISIBLE
        } else {
            holder.removeButton.visibility = View.INVISIBLE
        }

        holder.sunCheck.isClickable = isEditable
        holder.sunCheck.isChecked = item.daysAppliesTo.contains(SUNDAY)

        holder.monCheck.isClickable = isEditable
        holder.monCheck.isChecked = item.daysAppliesTo.contains(MONDAY)

        holder.tuCheck.isClickable = isEditable
        holder.tuCheck.isChecked = item.daysAppliesTo.contains(TUESDAY)

        holder.weCheck.isClickable = isEditable
        holder.weCheck.isChecked = item.daysAppliesTo.contains(WEDNESDAY)

        holder.thCheck.isClickable = isEditable
        holder.thCheck.isChecked = item.daysAppliesTo.contains(THURSDAY)

        holder.friCheck.isClickable = isEditable
        holder.friCheck.isChecked = item.daysAppliesTo.contains(FRIDAY)

        holder.satCheck.isClickable = isEditable
        holder.satCheck.isChecked = item.daysAppliesTo.contains(SATURDAY)
    }


    override fun getItemCount(): Int = values.size


    private fun removeTime(time: PreferenceTimeRep) {
        values.remove(time)
        notifyDataSetChanged()
    }

    fun addTime(time: PreferenceTimeRep) {
        values.add(time)
        notifyDataSetChanged()
    }

    fun getTimesPreferenceFormat(): HashMap<String, ArrayList<String>> {
        val timesMap: HashMap<String, ArrayList<String>>  = HashMap()
        for(timePref in values) {
            timesMap.put("${timePref.startHour}-${timePref.endHour}", timePref.daysAppliesTo)
        }
        return timesMap
    }


    inner class ViewHolder(binding: PreferenceTimeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var startTimeHour: TextView = binding.editStartTime
        var endTimeHour: TextView = binding.editEndTime
        var removeButton: ImageButton = binding.removeTimeButton
        var sunCheck: CheckBox = binding.checkSun
        var monCheck: CheckBox = binding.checkMon
        var tuCheck: CheckBox = binding.checkTu
        var weCheck: CheckBox = binding.checkWed
        var thCheck: CheckBox = binding.checkTh
        var friCheck: CheckBox = binding.checkFr
        var satCheck: CheckBox = binding.checkSat

    }

}


data class PreferenceTimeRep (
    var startHour: String = "09:00",
    var endHour: String = "12:00",
    var daysAppliesTo: ArrayList<String> = ArrayList()

    )