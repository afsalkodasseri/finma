package app.bicast.finma

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.BankBrs
import app.bicast.finma.db.models.WorkEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewWorkEventActivity : AppCompatActivity() {
    val calTime: Calendar = Calendar.getInstance()
    val sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    val db : dbSql = dbSql(this)

    lateinit var spType : Spinner
    lateinit var tvDate : TextView
    lateinit var ivDelete : ImageView
    lateinit var etDescription : EditText
    lateinit var btAdd : Button
    var workEvent: WorkEvent? = null
    var chooseDate :Long = 0
    var multiMode = false
    var eventType = WorkEvent.Typ.HOLIDAY
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_work_event)

        btAdd = findViewById(R.id.bt_add)
        spType = findViewById(R.id.sp_type)
        etDescription = findViewById(R.id.edt_description)
        tvDate = findViewById(R.id.tv_date)
        ivDelete = findViewById(R.id.iv_delete)

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        workEvent = intent.getParcelableExtra("event")
        chooseDate = intent.getLongExtra("date",0)
        if(chooseDate!=0L)
            calTime.timeInMillis = chooseDate

        findViewById<CheckBox>(R.id.check_mutli).setOnCheckedChangeListener { buttonView, isChecked ->
            multiMode = isChecked
        }

        calTime.set(Calendar.HOUR_OF_DAY,0)
        calTime.set(Calendar.MINUTE,0)
        calTime.set(Calendar.SECOND,0)
        calTime.set(Calendar.MILLISECOND,0)

        tvDate.setText(sdf.format(calTime.time))
        tvDate.setOnClickListener {
            val year = calTime.get(Calendar.YEAR)
            val month = calTime.get(Calendar.MONTH)
            val day = calTime.get(Calendar.DAY_OF_MONTH)

            val dateDialog = DatePickerDialog(this,
                R.style.datePickerBlue, { view, year, month, dayOfMonth ->
                    calTime.set(Calendar.DAY_OF_MONTH,dayOfMonth)
                    calTime.set(Calendar.MONTH,month)
                    calTime.set(Calendar.YEAR,year)
                    tvDate.setText(sdf.format(calTime.time))
                },year,month,day)
            dateDialog.show()
        }

        val spTypeAdapter = ArrayAdapter(this,R.layout.list_item,WorkEvent.EVENT_TYPES)
        spTypeAdapter.setDropDownViewResource(R.layout.list_item_dropped)
        spType.adapter = spTypeAdapter
        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?,view: View?,position: Int,id: Long) {
                eventType = WorkEvent.Typ.values().get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("Selector","nothing")
            }
        }

        btAdd.setOnClickListener {
            val description = etDescription.text.toString()

            if(workEvent==null) {
                db.addWorkEvent(
                    WorkEvent(
                        null,
                        eventType.toString(),
                        description,
                        calTime.timeInMillis
                    )
                )

                Toast.makeText(this, "Inserted successfully", Toast.LENGTH_SHORT).show()
                clearEntry(etDescription)
                if(!multiMode)
                    finish()
            }else{
                workEvent!!.description = description
                workEvent!!.type = eventType.toString()
                workEvent!!.date = calTime.timeInMillis
                db.updateWorkEvent(workEvent!!)
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        ivDelete.setOnClickListener {
            if(workEvent!=null){
                db.deleteWorkEvent(workEvent!!)
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        checkForUpdate()
    }

    private fun checkForUpdate(){
        if (workEvent!=null){
            etDescription.setText(workEvent!!.description)
            calTime.timeInMillis = workEvent!!.date
            tvDate.setText(sdf.format(calTime.time))
            ivDelete.visibility = View.VISIBLE
            eventType = WorkEvent.Typ.valueOf(workEvent!!.type)
            spType.setSelection(eventType.ordinal)
            btAdd.setText("Save")
        }
    }


    private fun clearEntry(etName : EditText){
        etName.setText("")
    }
}