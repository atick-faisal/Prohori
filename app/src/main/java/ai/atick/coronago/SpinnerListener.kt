package ai.atick.coronago

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.AdapterView

class SpinnerListener(context: Context) : AdapterView.OnItemSelectedListener {
    private val database = AppDatabase(context)
    override fun onNothingSelected(parent: AdapterView<*>?) {
        Log.d("corona", "Nothing Selected")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d("corona", "Selection: ${parent?.getItemAtPosition(position)}")
        database.putString("gender", parent?.getItemAtPosition(position).toString())
    }
}