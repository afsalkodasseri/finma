package app.bicast.finma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.ExpensesRecyAdapter
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.DaySummaryBalanceModel
import app.bicast.finma.db.models.DaySummaryItem
import app.bicast.finma.db.models.Expense
import app.bicast.finma.utils.DateUtils
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DaySummaryActivity : AppCompatActivity() {
    lateinit var chartCombined :CombinedChart
    lateinit var etReserve: EditText
    lateinit var tvAverage: TextView
    lateinit var tvBalance: TextView
    lateinit var tvExcess: TextView
    lateinit var tvNeed: TextView
    lateinit var tvMonth: TextView
    lateinit var ivBackMonth: ImageView
    lateinit var ivNextMonth: ImageView
    lateinit var tvDay: TextView
    lateinit var tvTotal: TextView
    lateinit var recyExpenses: RecyclerView
    val db: dbSql = dbSql(this)
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    val sdfDm = SimpleDateFormat("dd MMM", Locale.ENGLISH)
    val calendarMonth : Calendar = Calendar.getInstance()
    var startTime:Long = 0
    var endTime:Long = 0
    var totalAmount = 0
    var expenseTotal = 0
    var reserveAmount = 0
    var dayAmount = 0
    var dayMaxCount = 0
    var summaryModel = DaySummaryBalanceModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_summary)

        chartCombined = findViewById(R.id.chart_summary)
        etReserve = findViewById(R.id.et_reserve)
        tvAverage = findViewById(R.id.tv_average)
        tvBalance = findViewById(R.id.tv_balance)
        tvExcess = findViewById(R.id.tv_excess)
        tvNeed = findViewById(R.id.tv_need)
        tvMonth = findViewById(R.id.tv_month)
        ivBackMonth = findViewById(R.id.iv_prev_month)
        ivNextMonth = findViewById(R.id.iv_next_month)
        tvDay = findViewById(R.id.tv_day)
        tvTotal = findViewById(R.id.tv_total)
        recyExpenses = findViewById(R.id.recy_entries)
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

        etReserve.doAfterTextChanged {
            reserveAmount = it.toString().toIntOrNull()?:0
            setSummaryAmounts()
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
        startTime = timeMonth.timeInMillis
        timeMonth.set(Calendar.DAY_OF_MONTH,timeMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
        timeMonth.add(Calendar.DAY_OF_MONTH,1)
        timeMonth.add(Calendar.SECOND,-1)
        endTime = timeMonth.timeInMillis
        dayMaxCount = timeMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        val summaryList = db.getExpenseMonthDayGrouped(startTime, endTime)
        summaryModel = db.getMonthAmountDaySummary(startTime, endTime)
        totalAmount = summaryModel.balance
        expenseTotal = summaryList.sumOf { it.amount?:0 }
        fillSummaryDays(summaryList,startTime, endTime)
        setBardata(summaryList)
        onDaySelected(0)
        setSummaryAmounts()
        tvMonth.setText(sdf.format(timeMonth.time))
    }

    fun setSummaryAmounts(){
        val actualAmount = totalAmount - reserveAmount
        tvNeed.setText(actualAmount.toString())
        dayAmount = actualAmount/dayMaxCount
        tvAverage.setText(dayAmount.toString())
        val dayValue = calendarMonth.get(Calendar.DAY_OF_MONTH)
        val currentBalance = dayAmount * dayValue
        tvBalance.text = currentBalance.toString()
        val difference = currentBalance - expenseTotal
        if(difference>0) {
            tvExcess.setTextColor(getColor(R.color.green))
            tvExcess.text = "+"+difference.toString()
        }else{
            tvExcess.setTextColor(getColor(R.color.red))
            tvExcess.text = difference.toString()
        }

        val currentData = chartCombined.data
        val lineEntries :ArrayList<Entry> = ArrayList()
        lineEntries.add(Entry(1f,dayAmount.toFloat()))
        lineEntries.add(Entry(dayMaxCount.toFloat(),dayAmount.toFloat()))
        val lineData = LineDataSet(lineEntries,"dayBalance")
        lineData.setColor(getColor(R.color.green))
        lineData.lineWidth = 1f
        lineData.setDrawCircles(false)
        lineData.setDrawValues(false)
        lineData.setDrawHighlightIndicators(false)
        currentData.setData(LineData(lineData))
        chartCombined.data = currentData
        chartCombined.invalidate()
    }

    fun setBardata(listExpenses :List<DaySummaryItem>){
        val amountList = listExpenses.map { it.amount }
        val barEntries = amountList.mapIndexed { i,amount-> BarEntry(i+1.toFloat(),amount.toFloat()) }
        val barDataSet = BarDataSet(barEntries,"daySummary")
        barDataSet.setDrawValues(true)
        barDataSet.valueTypeface = ResourcesCompat.getFont(this,R.font.monteser_bold)
        barDataSet.valueTextSize = 5.5f
        barDataSet.color = getColor(R.color.blue)
        barDataSet.highLightColor = getColor(R.color.black)
        barDataSet.highLightAlpha = 255
        barDataSet.valueFormatter = object : ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                if(value==0f)
                    return ""
                return value.toInt().toString()
            }
        }

        val combinedData = CombinedData()
        combinedData.setData(BarData(barDataSet))
        chartCombined.data = combinedData
        chartCombined.legend.isEnabled = false
        chartCombined.axisLeft.isEnabled = false
        chartCombined.description.isEnabled = false
        chartCombined.setScaleEnabled(false)
        chartCombined.setPinchZoom(false)
        val xAxis = chartCombined.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0.5f
        xAxis.axisMaximum = Math.max(listExpenses.size,28) + 0.5f
        xAxis.typeface =ResourcesCompat.getFont(this,R.font.monteser_bold)
        xAxis.axisLineColor = getColor(R.color.black)
        xAxis.setDrawGridLines(false)
        xAxis.axisLineWidth = 0.8f
        val yAxis = chartCombined.axisRight
        chartCombined.axisLeft.axisMinimum = 0f
        yAxis.axisMinimum = 0f
        yAxis.typeface =ResourcesCompat.getFont(this,R.font.monteser_bold)
        yAxis.axisLineColor = getColor(R.color.black)
        yAxis.axisLineWidth = 0.8f
        yAxis.setDrawGridLines(false)
        chartCombined.setOnChartValueSelectedListener(object :OnChartValueSelectedListener{
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                onDaySelected(e!!.x.toInt())
            }

            override fun onNothingSelected() {
                onDaySelected(0)
            }

        })
        chartCombined.invalidate()
    }

    fun onDaySelected(day :Int){
        val expenseItems : List<Expense>
        val txtDay :String
        if(day>0) {
            val tempCal = Calendar.getInstance()
            tempCal.timeInMillis = startTime
            tempCal.set(Calendar.DAY_OF_MONTH, day)
            val dayStartTime = DateUtils.startTime(tempCal.timeInMillis)
            val dayEndTime = DateUtils.endTime(tempCal.timeInMillis)
            expenseItems = db.getSummaryExpense(dayStartTime,dayEndTime)
            txtDay = sdfDm.format(Date(dayStartTime))
        }else{
            expenseItems = db.getSummaryExpense(startTime,endTime)
            txtDay = sdfDm.format(Date(startTime)) + " - " + sdfDm.format(Date(endTime))
        }
        val adapterEntries = ExpensesRecyAdapter(expenseItems)
        recyExpenses.adapter = adapterEntries
        recyExpenses.layoutManager = LinearLayoutManager(this)
        tvDay.setText(txtDay)
        tvTotal.text = expenseItems.sumOf { it.amount?:0 }.toString()
    }

    fun fillSummaryDays(listSummary :ArrayList<DaySummaryItem>,startTime :Long, endTime :Long){
        val daySeconds = 86400000
        if(listSummary.isEmpty())   return
        val lastTime = listSummary.last().date
        val daysCountAvailable = ((lastTime - startTime)/daySeconds).toInt()
        for(i in 0 ..  daysCountAvailable){
            val item = listSummary.get(i)
            val secondsDifference = item.date - startTime
            val dayNumberActual = (secondsDifference/daySeconds).toInt()
            val missedDays = dayNumberActual - i
            for(j in 0 until missedDays)
                listSummary.add(i+j,DaySummaryItem(startTime+(daySeconds*i)+(daySeconds*j),0,0))
        }
    }
}