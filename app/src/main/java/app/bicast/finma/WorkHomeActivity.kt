package app.bicast.finma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.BrsRecyAdapter
import app.bicast.finma.adapter.EventRecyAdapter
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.WorkEvent
import app.bicast.finma.utils.DateUtils
import com.ak.ColoredDate
import com.ak.EventObjects
import com.ak.KalendarView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkHomeActivity : AppCompatActivity() {
    lateinit var mKalendarView :KalendarView
    lateinit var fabAdd: FloatingActionButton
    lateinit var rvEvents: RecyclerView
    lateinit var tvNoEvents: TextView
    lateinit var tvSickLeave: TextView
    lateinit var tvSickLeaveTotal: TextView
    lateinit var tvCasualLeave: TextView
    lateinit var tvCasualLeaveTotal: TextView
    lateinit var tvHoliday: TextView
    lateinit var tvHolidayTotal: TextView
    lateinit var tvWfh: TextView
    lateinit var tvWfhTotal: TextView
    val db: dbSql = dbSql(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_home)

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        mKalendarView = findViewById(R.id.calendarView)
        rvEvents = findViewById(R.id.rv_events)
        tvNoEvents = findViewById(R.id.tv_no_events)
        tvSickLeave = findViewById(R.id.tv_sick_leave)
        tvSickLeaveTotal = findViewById(R.id.tv_sick_leave_total)
        tvCasualLeave = findViewById(R.id.tv_casual_leave)
        tvCasualLeaveTotal = findViewById(R.id.tv_casual_leave_total)
        tvHoliday = findViewById(R.id.tv_holidays)
        tvHolidayTotal = findViewById(R.id.tv_holidays_total)
        tvWfh = findViewById(R.id.tv_wfh)
        tvWfhTotal = findViewById(R.id.tv_wfh_total)
        fabAdd = findViewById(R.id.fb_add)

        fabAdd.setOnClickListener {
            startActivity(
                Intent(applicationContext, NewWorkEventActivity::class.java)
                    .putExtra("date",mKalendarView.selectedDate.time))
        }

        mKalendarView.setMonthChanger {
            selectedMonth->
            run {
                mKalendarView.setColoredDates(getWeekendsForMonth(selectedMonth))
                loadEventsMonthly(selectedMonth)
                Log.d("changed",selectedMonth.toString())
                Log.d("weekends",getWeekendsForMonth(selectedMonth).toString())
            }
        }
        mKalendarView.setDateSelector { selectedDate ->
            run {
                Log.d("asd", selectedDate.toString())
                loadEvents(selectedDate)
            }
        }
        mKalendarView.setInitialSelectedDate(Date())
    }

    fun refreshSummary(){
        val tempCal = Calendar.getInstance()
        val monthCount = tempCal.get(Calendar.MONTH) + 1
        val totalCasual = monthCount
        val totalSick = monthCount
        val sickTook = db.getWorkEvent(DateUtils.yearStartTime(),DateUtils.yearEndTime(),WorkEvent.Typ.SICK_LEAVE.toString())
        val casualTook = db.getWorkEvent(DateUtils.yearStartTime(),DateUtils.yearEndTime(),WorkEvent.Typ.CASUAL_LEAVE.toString())
        val holidays = db.getWorkEvent(DateUtils.yearStartTime(),DateUtils.yearEndTime(),WorkEvent.Typ.HOLIDAY.toString())
        val holidaysLeft = db.getWorkEvent(Date().time,DateUtils.yearEndTime(),WorkEvent.Typ.HOLIDAY.toString())
        val wfhs = db.getWorkEvent(DateUtils.yearStartTime(),DateUtils.yearEndTime(),WorkEvent.Typ.WFH.toString())

        tvSickLeave.setText(sickTook.size.toString())
        tvSickLeaveTotal.setText("out of "+totalSick.toString())
        tvCasualLeave.setText(casualTook.size.toString())
        tvCasualLeaveTotal.setText("out of "+totalCasual.toString())
        tvHoliday.setText(holidaysLeft.size.toString())
        tvHolidayTotal.setText("out of "+holidays.size.toString())
        tvWfh.setText(wfhs.size.toString())
        tvWfhTotal.setText("no limits")
    }

    override fun onResume() {
        super.onResume()
        mKalendarView.setColoredDates(getWeekendsForMonth(mKalendarView.showingMonth))
        loadEvents(mKalendarView.selectedDate)
        loadEventsMonthly(mKalendarView.showingMonth)
        mKalendarView.setInitialSelectedDate(mKalendarView.selectedDate)
        refreshSummary()
    }

    private fun loadEvents(date :Date){
        val startTime = DateUtils.startTime(date.time)
        val endTime = DateUtils.endTime(date.time)
        val events = db.getWorkEvent(startTime,endTime)
        val adapterItems = EventRecyAdapter(events)
        rvEvents.adapter = adapterItems
        rvEvents.layoutManager = LinearLayoutManager(this)
        if(events.isEmpty())
            tvNoEvents.visibility = View.VISIBLE
        else
            tvNoEvents.visibility = View.GONE
    }
    private fun loadEventsMonthly(date :Date){
        val startTime = DateUtils.monthStartTime(date)
        val endTime = DateUtils.monthEndTime(date)
        val events = db.getWorkEvent(startTime,endTime)

        val eventsCal = ArrayList<ColoredDate>()
        var color: Int
        for(event in events){
            if (event.type.equals(WorkEvent.Typ.HOLIDAY.toString()))
                color = R.color.work_holiday
            else if (event.type.equals(WorkEvent.Typ.WFH.toString()))
                color = R.color.work_wfh
            else
                color = R.color.work_leave
            eventsCal.add(ColoredDate(Date(event.date),getColor(color)))
        }
        mKalendarView.addColoredDates(eventsCal)
    }
    private fun getWeekendsForMonth(monthDate :Date) :List<ColoredDate>{
        val result = ArrayList<ColoredDate>()
        val sdfWeek = SimpleDateFormat("EEE", Locale.ENGLISH)
        val tempCal :Calendar = Calendar.getInstance()
        tempCal.time = monthDate
        tempCal.set(Calendar.DAY_OF_MONTH,1)
        val maxDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        var interVal = 1
        for (i in 1..maxDay step interVal){
            tempCal.set(Calendar.DAY_OF_MONTH,i)
            if(sdfWeek.format(tempCal.time).equals("sat",true)){
                result.add(ColoredDate(tempCal.time,getColor(R.color.progres_red)))
                interVal = 1
            }else if(sdfWeek.format(tempCal.time).equals("sun",true)){
                result.add(ColoredDate(tempCal.time,getColor(R.color.progres_red)))
                interVal = 6
            }
        }
        return result
    }
}