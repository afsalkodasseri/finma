package app.bicast.finma

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Orientation
import app.bicast.finma.adapter.ColorsRecyAdapter
import app.bicast.finma.adapter.ExpenseGroupsRecyAdapter
import app.bicast.finma.adapter.ExpensesRecyAdapter
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.Expense
import app.bicast.finma.db.models.ExpenseGroup
import app.bicast.finma.utils.GroupUtils
import app.bicast.finma.utils.RangePicker.PickerDialog
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseGroupsActivity : AppCompatActivity() {
    lateinit var recyGroups: RecyclerView
    lateinit var recyExpenses: RecyclerView
    lateinit var tvExpense: TextView
    lateinit var tvMonth: TextView
    lateinit var tvSortBy: TextView
    lateinit var ivBackMonth: ImageView
    lateinit var ivNextMonth: ImageView
    var totalExpense :Int = 0
    val calendarMonth :Calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    var startTime = 0L
    var endTime = 0L
    lateinit var donutProgressbar : DonutProgressView
    lateinit var fabAdd: FloatingActionButton
    var isDialogShow = false
    val db = dbSql(this)
    var filterItems :ArrayList<Int?> = ArrayList()
    var expenseItems :ArrayList<ExpenseGroup> = ArrayList()
    var expenseEntryItems :ArrayList<Expense> = ArrayList()
    var sortMode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_groups)

        fabAdd = findViewById(R.id.fb_add)
        fabAdd.setOnClickListener {
            showAddGroupDialog(null)
        }

        tvExpense = findViewById(R.id.tv_total_expense)
        tvMonth = findViewById(R.id.tv_month)
        tvSortBy = findViewById(R.id.tv_sort)
        ivBackMonth = findViewById(R.id.iv_prev_month)
        ivNextMonth = findViewById(R.id.iv_next_month)
        recyGroups = findViewById(R.id.recy_groups)
        recyExpenses = findViewById(R.id.recy_entries)
        donutProgressbar = findViewById(R.id.donut_view)
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
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

        tvSortBy.setOnClickListener {
            if (sortMode==4)
                sortMode = 1
            else
                sortMode++
            filterExpense()
        }

        loadMonthTimes()
    }

    private fun loadEntries(){
        expenseItems = db.getExpenseMonthGrouped(startTime, endTime)
        totalExpense = expenseItems.sumOf { it.amount?:0 }
        tvExpense.setText(totalExpense.toString())
        tvMonth.setText(sdf.format(calendarMonth.time))

        if(totalExpense>0) {
            val sectionList :ArrayList<DonutSection> = ArrayList()
            for(itemGroup:ExpenseGroup in expenseItems){
                sectionList.add(
                    DonutSection(
                    itemGroup.name?:"Other",
                    Color.parseColor(itemGroup.color?:GroupUtils.otherColorGrey),
                    itemGroup.amount?.toFloat() ?:0f)
                )
            }
            donutProgressbar.cap = totalExpense.toFloat()
            donutProgressbar.submitData(sectionList)
        }

        loadExpenses()
        loadGroups(expenseItems)
    }

    private fun loadExpenses(){
        expenseEntryItems = db.getExpenseAllMonthGrouped(startTime, endTime)
        val adapterEntries = ExpensesRecyAdapter(expenseEntryItems)
        recyExpenses.adapter = adapterEntries
        recyExpenses.layoutManager = LinearLayoutManager(this)
    }

    private fun loadGroups(groupList: List<ExpenseGroup>){
        val groupsArray = db.getExpenseGroups()
        for(groupItem :ExpenseGroup in groupsArray){
            groupItem.amount = groupList.find { groupItem.id ==it.id }?.amount?:0
        }
        val adapterEntries = ExpenseGroupsRecyAdapter(groupsArray,filterItems,object :ExpenseGroupsRecyAdapter.GroupClick{
            override fun onClick(item: ExpenseGroup) {
                if (filterItems.contains(item.id)) filterItems.remove(item.id) else filterItems.add(item.id)
                filterExpense()
            }

            override fun onLongClick(item: ExpenseGroup) {
                showAddGroupDialog(item)
            }
        })
        recyGroups.adapter = adapterEntries
        recyGroups.layoutManager = GridLayoutManager(this,3)
    }

    private fun filterExpense(){
        val filteredExpenses = ArrayList<ExpenseGroup>()
        for(item :ExpenseGroup in expenseItems){
            if(!filterItems.contains(item.id))
                filteredExpenses.add(item)
        }
//        val filteredExpenses = expenseItems.filter { allItem-> filterItems.any { allItem.id != it.id} }
        totalExpense = filteredExpenses.sumOf { it.amount?:0 }
        tvExpense.setText(totalExpense.toString())
        tvMonth.setText(sdf.format(calendarMonth.time))

        if(totalExpense>0) {
            val sectionList :ArrayList<DonutSection> = ArrayList()
            for(itemGroup:ExpenseGroup in filteredExpenses){
                sectionList.add(
                    DonutSection(
                        itemGroup.name?:"Other",
                        Color.parseColor(itemGroup.color?:GroupUtils.otherColorGrey),
                        itemGroup.amount?.toFloat() ?:0f)
                )
            }
            donutProgressbar.cap = totalExpense.toFloat()
            donutProgressbar.submitData(sectionList)
        }

        //for expense entries
        val filteredExpenseEntries = ArrayList<Expense>()
        for(item :Expense in expenseEntryItems){
            if(!filterItems.contains(item.group_id?.toInt()))
                filteredExpenseEntries.add(item)
        }
        when (sortMode){
            1-> {
                tvSortBy.setText("Sort By Date -D")
                filteredExpenseEntries.sortByDescending { it.dateTime }
            }
            2-> {
                tvSortBy.setText("Sort By Amount -D")
                filteredExpenseEntries.sortByDescending { it.amount }
            }
            3-> {
                tvSortBy.setText("Sort By Date -A")
                filteredExpenseEntries.sortBy { it.dateTime }
            }
            4-> {
                tvSortBy.setText("Sort By Amount -A")
                filteredExpenseEntries.sortBy { it.amount }
            }
        }
        val adapterEntries = ExpensesRecyAdapter(filteredExpenseEntries)
        recyExpenses.adapter = adapterEntries
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
    private fun showAddGroupDialog(item :ExpenseGroup?){
        var selectedPos = GroupUtils.Colors.indexOf(item?.color)
        selectedPos = if (selectedPos==-1) 0 else selectedPos
        val addDialog = Dialog(this)
        addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        addDialog.setContentView(R.layout.dialog_add_group)
        addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        addDialog.window?.setGravity(Gravity.BOTTOM)
        addDialog.setOnShowListener{isDialogShow=true}
        addDialog.setOnDismissListener{isDialogShow=false}

        val edtName :EditText = addDialog.findViewById(R.id.edt_name)
        val recyColors: RecyclerView = addDialog.findViewById(R.id.recy_colors)
        val adapterColors = ColorsRecyAdapter(GroupUtils.Colors,object :ColorsRecyAdapter.ColorClick{
            override fun Click(pos: Int, color: String) {
                selectedPos = pos
            }
        },selectedPos)
        recyColors.layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        recyColors.adapter = adapterColors

        if(item!=null){
            edtName.setText(item.name)
            addDialog.findViewById<Button>(R.id.bt_add).setText("Save")
        }

        addDialog.findViewById<Button>(R.id.bt_add).setOnClickListener {
//            val rbId = rbColors.checkedRadioButtonId
//            val radButton = addDialog.findViewById<RadioButton>(rbId)
            val color = GroupUtils.Colors.get(selectedPos)
            val name = edtName.text.toString().trim()

            if(name.isEmpty()||name.length<2){
                Toast.makeText(this,"Enter a valid name",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(item==null)
                db.addExpenseGroup(ExpenseGroup(null,edtName.text.toString(),color,null,null))
            else{
                item.name = name
                item.color = color
                db.updateExpenseGroup(item)
            }
            loadEntries()
            addDialog.dismiss()
        }

        addDialog.findViewById<Button>(R.id.bt_cancel).setOnClickListener {
            addDialog.dismiss()
        }

        addDialog.show()
    }

    private fun getColorFromButton(button: RadioButton) :String {
        val colorsList = button.buttonTintList
        val color = colorsList?.defaultColor
        val hexColor = String.format("#%06X", 0xFFFFFF and color!!)
        return hexColor
    }
}