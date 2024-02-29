package app.bicast.finma.utils.RangePicker

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.bicast.finma.R
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DatePickerView : LinearLayout {

    val TAG = "DATE-RANGER"
    val MAX_CALENDAR_COLUMN = 42
    lateinit var prevButton:ImageView
    lateinit var nextButton:ImageView
    lateinit var currentDate:TextView
    lateinit var calendarGridView:GridView
    val formatter = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    val cal = Calendar.getInstance()
    lateinit var mContext: Context
    lateinit var mAdapter : GridAdapterDatePicker
    var prev = -1
    var pos=-1
    var cr_pos = -2
    lateinit var dayValueInCells: ArrayList<Date>
    val today_date = Calendar.getInstance()
    lateinit var color_date:Date
    lateinit var startDate:Date
    lateinit var endDate:Date
    var mDateSelector :DateSelector? = null
    var maxDate :Date? = null
    var minDate :Date? = null
    var isMinDisable = false
    var isMaxDisable = false
    var todayDrw = R.drawable.widget_rangepicker_date_square_color_out
    var startDrw = R.drawable.widget_rangepicker_custom_date_start
    var endDrw = R.drawable.widget_rangepicker_custom_date_end
    var midDrw = R.drawable.widget_rangepicker_custom_date_range_p
    var singleDrw = R.drawable.widget_rangepicker_date_square_color_blue
    var todayTxt = Color.parseColor("#000000")
    var startTxt = Color.parseColor("#ffffff")
    var singleTxt = Color.parseColor("#ffffff")
    var endTxt = Color.parseColor("#ffffff")
    var midTxt = Color.parseColor("#000000")
    constructor(context: Context) : super(context){
        mContext = context;
        color_date = today_date.time
        startDate = Date()
        endDate = Date()
        initUi()
        setupAdapter()
        setButtonClicks()
        Log.d(TAG,"construct the DatePickerView")
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context,attributeSet){}
    constructor(context: Context, attributeSet: AttributeSet,style :Int) : super(context,attributeSet,style){}
    fun initUi(){
        val view = inflate(mContext,R.layout.widget_rangepicker_calendar_view,this)
        prevButton = view.findViewById(R.id.previous_month)
        nextButton = view.findViewById(R.id.next_month)
        currentDate = view.findViewById(R.id.display_current_date)
        calendarGridView = view.findViewById(R.id.calendar_grid)
    }

    fun setupAdapter(){
        dayValueInCells = ArrayList()
        val mCal :Calendar = cal.clone() as Calendar
        mCal.set(Calendar.DAY_OF_MONTH,1)
        val firstDayMonth = mCal.get(Calendar.DAY_OF_WEEK) -1
        mCal.add(Calendar.DAY_OF_MONTH,-firstDayMonth)
        while (dayValueInCells.size<MAX_CALENDAR_COLUMN){
            dayValueInCells.add(mCal.time)
            mCal.add(Calendar.DAY_OF_MONTH,1)
        }
        val sDate = formatter.format(cal.time)
        currentDate.setText(sDate)
        prev = dayValueInCells.indexOf(color_date)
        cr_pos = dayValueInCells.indexOf(today_date.time)
        mAdapter = GridAdapterDatePicker(
            mContext,
            dayValueInCells,
            cal,
            color_date,
            startDate,
            endDate,
            todayDrw,
            todayTxt,
            startDrw,
            startTxt,
            midDrw,
            midTxt,
            endDrw,
            endTxt,
            singleDrw,
            singleTxt,
            minDate,
            isMinDisable,
            maxDate,
            isMaxDisable
        )
        calendarGridView.adapter = mAdapter
    }
    fun setButtonClicks(){
        prevButton.setOnClickListener {
            cal.add(Calendar.MONTH,-1)
            setupAdapter()
        }
        nextButton.setOnClickListener {
            cal.add(Calendar.MONTH,1)
            setupAdapter()
        }
        calendarGridView.setOnItemClickListener { adapterView, view, i, l ->
            pos = view.tag as Int
            val txt :TextView = view.findViewById(R.id.calendar_date_id)
            color_date = dayValueInCells.get(pos)
            prev = pos
            val monthId = txt.tag as Int
            if(monthId==-1) cr_pos = pos
            if(monthId==1){
                cal.add(Calendar.MONTH,1)
                prev = -1
                cr_pos = -2
            }
            if(monthId==2){
                cal.add(Calendar.MONTH,-1)
                prev = -1
                cr_pos = -2
            }
            setupAdapter()
            if(maxDate!=null && minDate!=null){
                val compareMax = compareDate(color_date,maxDate!!)
                val compareMin = compareDate(color_date,minDate!!)
                if(compareMin!=-1 && compareMax!=-1 && mDateSelector!=null)
                    mDateSelector!!.onDateSelected()
            }else if(maxDate!=null){
                val compart = compareDate(color_date,maxDate!!)
                if(compart!=1 && mDateSelector!=null)
                    mDateSelector!!.onDateSelected()
            }else if(minDate!=null){
                val compart = compareDate(color_date,minDate!!)
                if(compart!=-1 && mDateSelector!=null)
                    mDateSelector!!.onDateSelected()
            }else
                mDateSelector!!.onDateSelected()
        }
    }

    fun compareDate(first :Date, second :Date) :Int{
        //reset time in startDate
        val startCal = Calendar.getInstance()
        startCal.time = first
        startCal.set(Calendar.HOUR_OF_DAY,0)
        startCal.set(Calendar.MINUTE,0)
        startCal.set(Calendar.SECOND,0)
        startCal.set(Calendar.MILLISECOND,0)
        //reset time in endDate
        val endCal = Calendar.getInstance()
        endCal.time = first
        endCal.set(Calendar.HOUR_OF_DAY,0)
        endCal.set(Calendar.MINUTE,0)
        endCal.set(Calendar.SECOND,0)
        endCal.set(Calendar.MILLISECOND,0)
        return startCal.compareTo(endCal)
//        var result = -2;
//        val firstDay = first.date
//        val firstMonth = first.month
//        val firstYear = first.year
//        val secondDay = second.date
//        val secondMonth = second.month
//        val secondYear = second.year
//        if(firstYear<secondYear)
//            result = -1
//        else if(firstYear==secondYear){
//            if(firstMonth<secondMonth)
//                result = -1
//            else if(firstMonth==secondMonth) {
//                if (firstDay < secondDay)
//                    result = -1
//                else if (firstDay == secondDay)
//                    result = 0
//                else
//                    result = 1
//            }else
//                result = 1
//        }else
//            result = 1
//
//        return result;
    }

    interface DateSelector{
        fun onDateSelected()
    }

    fun setRangeDate(start :Date, end :Date){
        startDate = start
        endDate = end
        setupAdapter()
    }
    fun setMinDate(start :Date, isDisableColor :Boolean){
        minDate = start
        isMinDisable = isDisableColor
        setupAdapter()
    }

    fun setMaxDate(start :Date, isDisableColor :Boolean){
        maxDate = start
        isMaxDisable = isDisableColor
        setupAdapter()
    }
    fun setStartDrawable(drawable :Int){
        startDrw = drawable
    }
    fun setEndDrawable(drawable :Int){
        endDrw = drawable
    }
    fun setMidDrawable(drawable :Int){
        midDrw = drawable
    }
    fun setStartTextColor(color :Int){
        startTxt = color
    }
    fun setEndTextColor(color :Int){
        endTxt = color
    }
    fun setMidTextColor(color :Int){
        midTxt = color
    }
    fun setSingleRangeDrawable(drawable :Int){
        singleDrw = drawable
    }
    fun setSingleRangeTextColor(color :Int){
        singleTxt = color
    }
    fun setTodayDrawable(drawable :Int){
        todayDrw = drawable
    }
    fun setTodayTextColor(color :Int){
        todayTxt = color
    }
    fun applyChanges(){
        setupAdapter()
    }
}

