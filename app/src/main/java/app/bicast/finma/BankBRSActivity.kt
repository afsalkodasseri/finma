package app.bicast.finma

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.BrsRecyAdapter
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.BankBrs
import app.bicast.finma.R
import app.bicast.finma.utils.DateUtils
import app.bicast.finma.utils.RangePicker.PickerDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BankBRSActivity : AppCompatActivity() {
    lateinit var recyEntries: RecyclerView
    lateinit var tvTotal: TextView
    lateinit var tvCash: TextView
    lateinit var tvBank: TextView
    lateinit var tvMonth: TextView
    lateinit var ivBackMonth: ImageView
    lateinit var ivNextMonth: ImageView
    lateinit var fabAdd: FloatingActionButton
    val db: dbSql = dbSql(this)
    lateinit var cashArray :List<BankBrs>
    lateinit var bankArray :List<BankBrs>
    var totalCash :Int = 0
    var totalBank :Int = 0
    var accountType = 0
    val calendarMonth : Calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    var adapterEntries : BrsRecyAdapter? = null
    var startTime = 0L
    var endTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank_brsactivity)

        tvTotal = findViewById(R.id.tv_total)
        tvCash = findViewById(R.id.tv_cash)
        tvBank = findViewById(R.id.tv_bank)
        tvMonth = findViewById(R.id.tv_month)
        ivBackMonth = findViewById(R.id.iv_prev_month)
        ivNextMonth = findViewById(R.id.iv_next_month)
        fabAdd = findViewById(R.id.fb_add)
        recyEntries = findViewById(R.id.recy_entries)
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        fabAdd.setOnClickListener {
            startActivity(Intent(this, NewBankBrsActivity::class.java))
        }

        tvCash.setOnClickListener {
            accountType = 1
            filterEntries()
        }

        tvBank.setOnClickListener {
            accountType = 2
            filterEntries()
        }
        tvTotal.setOnClickListener {
            accountType = 0
            loadEntries()
        }

        ivBackMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH,-1)
            loadMonthTimes()
            loadEntries()
        }
        ivNextMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH,1)
            loadMonthTimes()
            loadEntries()
        }
        tvMonth.setOnClickListener {
            val startDate = Date(startTime)
            val endDate = Date(endTime)
            val dialog = PickerDialog(this, startDate, endDate)
            dialog.showPicker()
            dialog.setOnRangeSelection { StartDate, EndDate ->
                val timeMonth = Calendar.getInstance()
                timeMonth.time = StartDate
                timeMonth.set(Calendar.HOUR_OF_DAY,0)
                timeMonth.set(Calendar.MINUTE,0)
                timeMonth.set(Calendar.SECOND,0)
                timeMonth.set(Calendar.MILLISECOND,0)
                startTime = timeMonth.timeInMillis
                endTime = EndDate.time
                loadEntries()
            }
        }

        loadMonthTimes()
    }

    private fun loadEntries(){
        val brsArray = db.getBrsMonth(calendarMonth,startTime,endTime)
        adapterEntries = BrsRecyAdapter(brsArray)
        recyEntries.adapter = adapterEntries
        recyEntries.layoutManager = LinearLayoutManager(this)

        if(accountType == 0) {
            cashArray =
                brsArray.filter { it.type == BankBrs.Typ.CASH.toString() }
            bankArray =
                brsArray.filter { it.type == BankBrs.Typ.BANK.toString() }

            totalCash = cashArray.sumOf { it.amount }
            totalBank = bankArray.sumOf { it.amount }

            tvCash.setText(totalCash.toString())
            tvBank.setText(totalBank.toString())
            tvTotal.setText((totalCash + totalBank).toString())
        }

        tvTotal.setTextColor(getColor(R.color.black))
        tvCash.setTextColor(getColor(R.color.black))
        tvBank.setTextColor(getColor(R.color.black))

        when(accountType){
            0-> tvTotal.setTextColor(getColor(R.color.blue))
            1-> tvCash.setTextColor(getColor(R.color.blue))
            2-> tvBank.setTextColor(getColor(R.color.blue))
        }

        tvMonth.setText(sdf.format(calendarMonth.time))
    }

    private fun filterEntries(){
        if(accountType == 1) {
            adapterEntries?.items = cashArray
        }else if(accountType == 2){
            adapterEntries?.items = bankArray
        }else{
            loadEntries()
        }
        adapterEntries?.notifyDataSetChanged()


        tvTotal.setTextColor(getColor(R.color.black))
        tvCash.setTextColor(getColor(R.color.black))
        tvBank.setTextColor(getColor(R.color.black))

        when(accountType){
            0-> tvTotal.setTextColor(getColor(R.color.blue))
            1-> tvCash.setTextColor(getColor(R.color.blue))
            2-> tvBank.setTextColor(getColor(R.color.blue))
        }
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }

    private fun loadMonthTimes(){
        val timeMonth = Calendar.getInstance()
        timeMonth.time = calendarMonth.time
        timeMonth.set(Calendar.DAY_OF_MONTH,1)
        timeMonth.set(Calendar.HOUR_OF_DAY,0)
        timeMonth.set(Calendar.MINUTE,0)
        timeMonth.set(Calendar.SECOND,0)
        timeMonth.set(Calendar.MILLISECOND,0)
        startTime = timeMonth.timeInMillis
        timeMonth.set(Calendar.DAY_OF_MONTH,timeMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        timeMonth.add(Calendar.DAY_OF_MONTH,1)
        timeMonth.add(Calendar.SECOND,-1)
        endTime = timeMonth.timeInMillis
    }
}