package app.bicast.finma.utils.RangePicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import app.bicast.finma.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PickerDialog {
    var mContext: Context
    var selector = 0
    var pickerStart : Date? = null
    var pickerEnd : Date? = null
    val mdy = SimpleDateFormat("MMMM dd,yy", Locale.ENGLISH)
    var dateRanger :DatePickerView
    var rangeSelector: OnRangeSelect? = null
    var currentPickerDrw = R.drawable.widget_rangepicker_selected_type
    var prevPickerDrw = R.drawable.widget_rangepicker_selected_type_grey
    var crTextColor = Color.WHITE
    var prTextColor = Color.BLACK
    var todayDrw = R.drawable.widget_rangepicker_date_square_color_out
    var startDrw = R.drawable.widget_rangepicker_custom_date_start
    var endDrw = R.drawable.widget_rangepicker_custom_date_end
    var midDrw = R.drawable.widget_rangepicker_custom_date_range_p
    var singleDrw = R.drawable.widget_rangepicker_date_square_color_blue
    var todayTxt = Color.parseColor("#000000")
    var startTxt = Color.parseColor("#FFFFFF")
    var singleTxt = Color.parseColor("#FFFFFF")
    var endTxt = Color.parseColor("#FFFFFF")
    var midTxt = Color.parseColor("#000000")
    var datePickerDialog: Dialog
    var txtStartDate :TextView
    var txtEndDate :TextView

    constructor(context: Context){
        this.mContext = context
        pickerStart = Date()
        pickerEnd = Date()
        selector = 0
        datePickerDialog = Dialog(context)
        datePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        datePickerDialog.setContentView(R.layout.widget_rangepicker_dialog)
        datePickerDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        datePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.setCancelable(true)
        val btnCancel :LinearLayout = datePickerDialog.findViewById(R.id.btnCancel)
        val btnApply :LinearLayout = datePickerDialog.findViewById(R.id.btnChange)
        txtStartDate = datePickerDialog.findViewById(R.id.txtStartDate)
        val txtStartTitle :TextView = datePickerDialog.findViewById(R.id.txtStartTitle)
        txtEndDate = datePickerDialog.findViewById(R.id.txtEndDate)
        val txtEndTitle :TextView = datePickerDialog.findViewById(R.id.txtEndTitle)
        val lyStartDate :LinearLayout = datePickerDialog.findViewById(R.id.lyStartDate)
        val lyEndDate :LinearLayout = datePickerDialog.findViewById(R.id.lyEndDate)

        if(selector==0){
            lyStartDate.setBackgroundResource(currentPickerDrw)
            txtStartTitle.setTextColor(crTextColor)
            txtStartDate.text = mdy.format(pickerStart!!)
            txtStartDate.setTextColor(crTextColor)
            lyEndDate.setBackgroundResource(prevPickerDrw)
            txtEndTitle.setTextColor(prTextColor)
            txtEndDate.text = mdy.format(pickerEnd!!)
            txtEndDate.setTextColor(prTextColor)
        }else{

            lyStartDate.setBackgroundResource(prevPickerDrw)
            txtStartTitle.setTextColor(prTextColor)
            txtStartDate.text = mdy.format(pickerEnd!!)
            txtStartDate.setTextColor(prTextColor)
            lyEndDate.setBackgroundResource(currentPickerDrw)
            txtEndTitle.setTextColor(crTextColor)
            txtEndDate.text = mdy.format(pickerStart!!)
            txtEndDate.setTextColor(crTextColor)
        }

        dateRanger = datePickerDialog.findViewById(R.id.dateRangerView)
        dateRanger.mDateSelector = object : DatePickerView.DateSelector {
            override fun onDateSelected() {
                if(selector==0){
                    pickerStart = dateRanger.color_date
                    txtStartDate.setText(mdy.format(pickerStart!!))
                    if(pickerStart!!.after(pickerEnd)){
                        pickerEnd = pickerStart
                        txtEndDate.text = mdy.format(pickerEnd!!)
                    }
                    selector=1
                    lyEndDate.setBackgroundResource(currentPickerDrw)
                    txtEndTitle.setTextColor(crTextColor)
                    txtEndDate.text = mdy.format(pickerEnd!!)
                    txtEndDate.setTextColor(crTextColor)
                    lyStartDate.setBackgroundResource(prevPickerDrw)
                    txtStartTitle.setTextColor(prTextColor)
                    txtStartDate.text = mdy.format(pickerStart!!)
                    txtStartDate.setTextColor(prTextColor)
                }else{
                    pickerEnd = dateRanger.color_date
                    if(pickerEnd!!.before(pickerStart!!)){
                        pickerStart = pickerEnd
                        txtStartDate.text = mdy.format(pickerStart)
                    }
                    txtEndDate.text = mdy.format(pickerEnd!!)
                }
                dateRanger.setRangeDate(pickerStart!!,pickerEnd!!)
            }
        }

        lyStartDate.setOnClickListener {
            selector = 0
            lyStartDate.setBackgroundResource(currentPickerDrw)
            txtStartTitle.setTextColor(crTextColor)
            txtStartDate.text = mdy.format(pickerStart!!)
            txtStartDate.setTextColor(crTextColor)
            lyEndDate.setBackgroundResource(prevPickerDrw)
            txtEndTitle.setTextColor(prTextColor)
            txtEndDate.text = mdy.format(pickerEnd!!)
            txtEndDate.setTextColor(prTextColor)
        }

        lyEndDate.setOnClickListener {
            selector = 1
            lyStartDate.setBackgroundResource(prevPickerDrw)
            txtStartTitle.setTextColor(prTextColor)
            txtStartDate.text = mdy.format(pickerEnd!!)
            txtStartDate.setTextColor(prTextColor)
            lyEndDate.setBackgroundResource(currentPickerDrw)
            txtEndTitle.setTextColor(crTextColor)
            txtEndDate.text = mdy.format(pickerStart!!)
            txtEndDate.setTextColor(crTextColor)
        }

        btnCancel.setOnClickListener {
            datePickerDialog.dismiss()
        }

        btnApply.setOnClickListener {
            if(rangeSelector!=null)
                rangeSelector!!.onSelect(pickerStart!!,pickerEnd!!)
            datePickerDialog.dismiss()
        }
    }
    constructor(context: Context,start :Date, end :Date){
        this.mContext = context
        pickerStart = start
        pickerEnd = end
        selector = 0
        datePickerDialog = Dialog(context)
        datePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        datePickerDialog.setContentView(R.layout.widget_rangepicker_dialog)
        datePickerDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        datePickerDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        datePickerDialog.setCancelable(true)
        val btnCancel :LinearLayout = datePickerDialog.findViewById(R.id.btnCancel)
        val btnApply :LinearLayout = datePickerDialog.findViewById(R.id.btnChange)
        txtStartDate = datePickerDialog.findViewById(R.id.txtStartDate)
        val txtStartTitle :TextView = datePickerDialog.findViewById(R.id.txtStartTitle)
        txtEndDate = datePickerDialog.findViewById(R.id.txtEndDate)
        val txtEndTitle :TextView = datePickerDialog.findViewById(R.id.txtEndTitle)
        val lyStartDate :LinearLayout = datePickerDialog.findViewById(R.id.lyStartDate)
        val lyEndDate :LinearLayout = datePickerDialog.findViewById(R.id.lyEndDate)

        if(selector==0){
            lyStartDate.setBackgroundResource(currentPickerDrw)
            txtStartTitle.setTextColor(crTextColor)
            txtStartDate.text = mdy.format(pickerStart!!)
            txtStartDate.setTextColor(crTextColor)
            lyEndDate.setBackgroundResource(prevPickerDrw)
            txtEndTitle.setTextColor(prTextColor)
            txtEndDate.text = mdy.format(pickerEnd!!)
            txtEndDate.setTextColor(prTextColor)
        }else{

            lyStartDate.setBackgroundResource(prevPickerDrw)
            txtStartTitle.setTextColor(prTextColor)
            txtStartDate.text = mdy.format(pickerEnd!!)
            txtStartDate.setTextColor(prTextColor)
            lyEndDate.setBackgroundResource(currentPickerDrw)
            txtEndTitle.setTextColor(crTextColor)
            txtEndDate.text = mdy.format(pickerStart!!)
            txtEndDate.setTextColor(crTextColor)
        }

        dateRanger = datePickerDialog.findViewById(R.id.dateRangerView)
        dateRanger.mDateSelector = object : DatePickerView.DateSelector {
            override fun onDateSelected() {
                if(selector==0){
                    pickerStart = dateRanger.color_date
                    txtStartDate.setText(mdy.format(pickerStart!!))
                    if(pickerStart!!.after(pickerEnd)){
                        pickerEnd = pickerStart
                        txtEndDate.text = mdy.format(pickerEnd!!)
                    }
                    selector=1
                    lyEndDate.setBackgroundResource(currentPickerDrw)
                    txtEndTitle.setTextColor(crTextColor)
                    txtEndDate.text = mdy.format(pickerEnd!!)
                    txtEndDate.setTextColor(crTextColor)
                    lyStartDate.setBackgroundResource(prevPickerDrw)
                    txtStartTitle.setTextColor(prTextColor)
                    txtStartDate.text = mdy.format(pickerStart!!)
                    txtStartDate.setTextColor(prTextColor)
                }else{
                    pickerEnd = dateRanger.color_date
                    if(pickerEnd!!.before(pickerStart!!)){
                        pickerStart = pickerEnd
                        txtStartDate.text = mdy.format(pickerStart)
                    }
                    txtEndDate.text = mdy.format(pickerEnd!!)
                }
                dateRanger.setRangeDate(pickerStart!!,pickerEnd!!)
            }
        }

        lyStartDate.setOnClickListener {
            selector = 0
            lyStartDate.setBackgroundResource(currentPickerDrw)
            txtStartTitle.setTextColor(crTextColor)
            txtStartDate.text = mdy.format(pickerStart!!)
            txtStartDate.setTextColor(crTextColor)
            lyEndDate.setBackgroundResource(prevPickerDrw)
            txtEndTitle.setTextColor(prTextColor)
            txtEndDate.text = mdy.format(pickerEnd!!)
            txtEndDate.setTextColor(prTextColor)
        }

        lyEndDate.setOnClickListener {
            selector = 1
            lyStartDate.setBackgroundResource(prevPickerDrw)
            txtStartTitle.setTextColor(prTextColor)
            txtStartDate.text = mdy.format(pickerEnd!!)
            txtStartDate.setTextColor(prTextColor)
            lyEndDate.setBackgroundResource(currentPickerDrw)
            txtEndTitle.setTextColor(crTextColor)
            txtEndDate.text = mdy.format(pickerStart!!)
            txtEndDate.setTextColor(crTextColor)
        }

        btnCancel.setOnClickListener {
            datePickerDialog.dismiss()
        }

        btnApply.setOnClickListener {
            if(rangeSelector!=null)
                rangeSelector!!.onSelect(pickerStart!!,pickerEnd!!)
            datePickerDialog.dismiss()
        }
    }
    fun showPicker(){
        datePickerDialog.show()
    }
    fun setStartDrawable(drawable :Int){
        startDrw = drawable
        dateRanger.setStartDrawable(startDrw)
        dateRanger.applyChanges()
    }
    fun setEndDrawable(drawable :Int){
        endDrw = drawable
        dateRanger.setEndDrawable(endDrw)
        dateRanger.applyChanges()
    }
    fun setStartTextColor(color :Int){
        startTxt = color
        dateRanger.setStartTextColor(startTxt)
        dateRanger.applyChanges()
    }
    fun setEndTextColor(color :Int){
        endTxt = color
        dateRanger.setEndTextColor(endTxt)
        dateRanger.applyChanges()
    }
    fun setMidDrawable(drawable :Int){
        midDrw = drawable
        dateRanger.setMidDrawable(midDrw)
        dateRanger.applyChanges()
    }
    fun setMidTextColor(color :Int){
        midTxt = color
        dateRanger.setMidTextColor(midTxt)
        dateRanger.applyChanges()
    }
    fun setSingleRangeDrawable(drawable :Int){
        singleDrw = drawable
        dateRanger.setSingleRangeDrawable(singleDrw)
        dateRanger.applyChanges()
    }
    fun setSingleRangeTextColor(color :Int){
        singleTxt = color
        dateRanger.setSingleRangeTextColor(singleTxt)
        dateRanger.applyChanges()
    }
    fun setTodayDrawable(drawable :Int){
        todayDrw = drawable
        dateRanger.setTodayDrawable(todayDrw)
        dateRanger.applyChanges()
    }
    fun setTodayTextColor(color :Int){
        todayTxt = color
        dateRanger.setTodayTextColor(todayTxt)
        dateRanger.applyChanges()
    }
    fun setDateRanges(start :Date, end :Date){
        dateRanger.setRangeDate(start,end)
        pickerStart = start
        pickerEnd = end
        txtStartDate.text = mdy.format(pickerStart!!)
        txtEndDate.text = mdy.format(pickerEnd!!)
    }
    fun setMaxDate(maxRange :Date, isDisableDateColor :Boolean){
        dateRanger.setMaxDate(maxRange,isDisableDateColor)
    }
    fun setMinDate(minRange :Date, isDisableDateColor :Boolean){
        dateRanger.setMinDate(minRange,isDisableDateColor)
    }
    fun setOnRangeSelection(rangeSelector :OnRangeSelect){
        this.rangeSelector = rangeSelector
    }
    interface OnRangeSelect{
        fun onSelect(start :Date,end :Date)
    }
}