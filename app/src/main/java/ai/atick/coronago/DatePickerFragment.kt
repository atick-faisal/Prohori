package ai.atick.coronago

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class DatePickerFragment : DialogFragment(), OnDateSetListener {
    //////////////////////////////////////////////////////////////////////////////////
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c: Calendar = Calendar.getInstance()
        val year: Int = c.get(Calendar.YEAR)
        val month: Int = c.get(Calendar.MONTH)
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(this.requireContext(), this, year, month, day)
    }
    ///////////////////////////////////////////////////////////////////////////////////
    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val date = "$day-${month+1}-$year"
        val mainActivity = this.requireContext() as Activity
        mainActivity.birthdayText.setText(date)
    }
}