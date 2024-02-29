package app.bicast.finma.utils.RangePicker

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.persistableBundleOf
import app.bicast.finma.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class GridAdapterDatePicker : BaseAdapter {
    val TAG = "DATE-RANGER-A";
    var mInflater :LayoutInflater? = null
    lateinit var monthlyDates :List<Date>
    lateinit var currentDate :Calendar
    lateinit var color_date:Date
    lateinit var start_date :Date
    lateinit var end_date :Date
    lateinit var mContext :Context
    var cell_bg :ConstraintLayout? = null
    var isTodayDate = false
    val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    var TodayIndicatorDrawable = R.drawable.widget_rangepicker_date_square_color_out
    var StartIndicatorDrawable = R.drawable.widget_rangepicker_custom_date_start
    var EndIndicatorDrawable = R.drawable.widget_rangepicker_custom_date_end
    var MidIndicatorDrawable = R.drawable.widget_rangepicker_custom_date_range_p
    var SingleDayIndicatorDrawable = R.drawable.widget_rangepicker_date_square_color_blue
    var TodayTextColor = Color.parseColor("#000000")
    var RangeTextColor = Color.parseColor("#FFFFFF")
    var RangeTextSingleColor = Color.parseColor("#FFFFFF")
    var RangeTextEndColor = Color.parseColor("#FFFFFF")
    var RangeMidTextColor = Color.parseColor("#000000")
    var minDateRange :Date? = null
    var maxDateRange :Date? = null
    var isMinDisable = false
    var isMaxDisable = false

    constructor(
        context: Context,
        monthlyDates: List<Date>,
        currentDate :Calendar,
        color :Date,
        start :Date,
        end :Date,
        todayDrw :Int,
        todayTxt :Int,
        startDrw :Int,
        startTxt :Int,
        midDrw :Int,
        midTxt :Int,
        endDrw :Int,
        endTxt :Int,
        singleDrw :Int,
        singleTxt :Int,
        min :Date?,
        isMinDis :Boolean,
        max :Date?,
        isMaxDis :Boolean
    ){
        mContext = context
        this.monthlyDates = monthlyDates
        this.currentDate = currentDate
        this.mInflater = LayoutInflater.from(context)
        color_date = color
        start_date = start
        end_date = end
        this.TodayIndicatorDrawable = todayDrw
        this.TodayTextColor = todayTxt
        this.StartIndicatorDrawable = startDrw
        this.RangeTextColor = startTxt
        this.EndIndicatorDrawable = endDrw
        this.RangeTextEndColor = endTxt
        this.MidIndicatorDrawable = midDrw
        this.RangeMidTextColor = midTxt
        this.SingleDayIndicatorDrawable = singleDrw
        this.RangeTextSingleColor = singleTxt
        this.minDateRange = min
        this.maxDateRange = max
        this.isMinDisable = isMinDis
        this.isMaxDisable = isMaxDis
    }
    override fun getCount(): Int {
        return monthlyDates.size
    }

    override fun getItem(p0: Int): Any {
        return monthlyDates.get(p0)
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var mDate = monthlyDates.get(p0)
        val dateCal = Calendar.getInstance()
        dateCal.time = mDate
        val dayValue = dateCal.get(Calendar.DAY_OF_MONTH)
        val displayMonth = dateCal.get(Calendar.MONTH)+1
        val displayYear = dateCal.get(Calendar.YEAR)
        val currentMonth = currentDate.get(Calendar.MONTH)+1
        val currentYear = currentDate.get(Calendar.YEAR)
        val today_date = Calendar.getInstance()
        val toYear = today_date.get(Calendar.YEAR)
        val toMonth = today_date.get(Calendar.MONTH)+1
        val toDay = today_date.get(Calendar.DATE)

        var view = p1
        isTodayDate = false
        if(view==null)
            view = mInflater!!.inflate(R.layout.widget_rangepicker_single_cell,p2,false)
        val cellNumber :TextView = view!!.findViewById(R.id.calendar_date_id)
        cell_bg = view.findViewById(R.id.cell)
        if(displayMonth==toMonth && displayYear==toYear && dayValue==toDay){
            cell_bg!!.setBackgroundResource(TodayIndicatorDrawable)
            cellNumber.setTextColor(TodayTextColor)
            cellNumber.tag = -1
            isTodayDate = true
        }

        if(displayMonth==currentMonth && displayYear == currentYear){
            cellNumber.tag = 0
            cellNumber.setTextColor(TodayTextColor)
            if(minDateRange!=null){
                val comparer = compareDate(mDate,minDateRange!!)
                if(isMinDisable && comparer==-1)
                    cellNumber.setTextColor(Color.parseColor("#a9a9a9"))
            }
            if(maxDateRange!=null){
                val comparer = compareDate(mDate,maxDateRange!!)
                if(isMaxDisable && comparer==1)
                    cellNumber.setTextColor(Color.parseColor("#a9a9a9"))
            }
        }else{
            if(displayMonth > currentMonth && displayYear==currentYear || (displayMonth<currentMonth && displayYear>currentYear))
                cellNumber.tag = 1
            else
                cellNumber.tag = 2
        }

        val tempItemDate = dateFormatter.format(mDate)
        val tempStartDate = dateFormatter.format(start_date)
        val tempEndDate = dateFormatter.format(end_date)

        if(tempItemDate.equals(tempStartDate) && tempItemDate.equals(tempEndDate)){
            cell_bg!!.setBackgroundResource(SingleDayIndicatorDrawable)
            cellNumber.setTextColor(RangeTextSingleColor)
        }else if(tempItemDate.equals(tempStartDate)){
            view.setBackgroundResource(StartIndicatorDrawable)
            cell_bg!!.setBackgroundResource(StartIndicatorDrawable)
            cellNumber.setTextColor(RangeTextColor)
        }else if(tempItemDate.equals(tempEndDate)){
            view.setBackgroundResource(EndIndicatorDrawable)
            cell_bg!!.setBackgroundResource(EndIndicatorDrawable)
            cellNumber.setTextColor(RangeTextEndColor)
        }else if(mDate.after(start_date) && mDate.before(end_date)){
            view.setBackgroundResource(MidIndicatorDrawable)
            cellNumber.setTextColor(RangeMidTextColor)
        }
        cellNumber.setText(dayValue.toString())
        view.tag = p0

        return view
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
    }
}