package app.bicast.finma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.DaySummaryItem
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DaySummaryActivity : AppCompatActivity() {
    lateinit var chartCombined :CombinedChart
    lateinit var tvTotal: TextView
    lateinit var tvAverage: TextView
    lateinit var tvNeed: TextView
    lateinit var tvMonth: TextView
    lateinit var ivBackMonth: ImageView
    lateinit var ivNextMonth: ImageView
    val db: dbSql = dbSql(this)
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    val calendarMonth : Calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_summary)

        chartCombined = findViewById(R.id.chart_summary)
        tvTotal = findViewById(R.id.tv_total)
        tvAverage = findViewById(R.id.tv_average)
        tvNeed = findViewById(R.id.tv_need)
        tvMonth = findViewById(R.id.tv_month)
        ivBackMonth = findViewById(R.id.iv_prev_month)
        ivNextMonth = findViewById(R.id.iv_next_month)
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }

        loadData()

        ivBackMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH,-1)
            loadData()
        }
        ivNextMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH,1)
            loadData()
        }
    }

    fun loadData(){
        val timeMonth = Calendar.getInstance()
        timeMonth.time = calendarMonth.time
        timeMonth.set(Calendar.DAY_OF_MONTH,1)
        timeMonth.set(Calendar.HOUR_OF_DAY,0)
        timeMonth.set(Calendar.MINUTE,0)
        timeMonth.set(Calendar.SECOND,0)
        timeMonth.set(Calendar.MILLISECOND,0)
        val startTime = timeMonth.timeInMillis
        timeMonth.set(Calendar.DAY_OF_MONTH,timeMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        timeMonth.add(Calendar.DAY_OF_MONTH,1)
        timeMonth.add(Calendar.SECOND,-1)
        val endTime = timeMonth.timeInMillis

        val summaryList = db.getExpenseMonthDayGrouped(0,startTime, endTime)
        setBardata(summaryList)

        val totalAmount = summaryList.sumOf { it.amount }
//        val highest = summaryList.maxOf { it.amount }

        tvNeed.setText(totalAmount.toString())
        tvAverage.setText(0.toString())
        tvMonth.setText(sdf.format(calendarMonth.time))
    }

    fun setBardata(listExpenses :List<DaySummaryItem>){
        val amountList = listExpenses.map { it.amount }
        val barEntries = amountList.mapIndexed { i,amount-> BarEntry(i.toFloat(),amount.toFloat()) }
        val barDataSet = BarDataSet(barEntries,"daySummary")
        barDataSet.setDrawValues(true)
        barDataSet.color = getColor(R.color.blue)

        val combinedData = CombinedData()
        combinedData.setData(BarData(barDataSet))
        chartCombined.data = combinedData
        chartCombined.legend.isEnabled = false
        chartCombined.axisLeft.isEnabled = false
        chartCombined.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chartCombined.invalidate()
    }
}