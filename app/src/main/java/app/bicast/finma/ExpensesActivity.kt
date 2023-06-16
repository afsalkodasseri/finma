package app.bicast.finma

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.ExpensesRecyAdapter
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.Expense
import app.bicast.finma.R
import app.bicast.finma.utils.RangePicker.PickerDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpensesActivity : AppCompatActivity() {
    lateinit var recyEntries: RecyclerView
    lateinit var tvBalance: TextView
    lateinit var tvExpense: TextView
    lateinit var tvIncome: TextView
    lateinit var tvMonth: TextView
    lateinit var ivBackMonth: ImageView
    lateinit var ivNextMonth: ImageView
    lateinit var fabAdd: FloatingActionButton
    val db: dbSql = dbSql(this)
    lateinit var expArray :List<Expense>
    lateinit var incArray :List<Expense>
    var totalExpense :Int = 0
    var totalIncome :Int = 0
    var expenseType = 0
    val calendarMonth :Calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    var startTime = 0L
    var endTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses)

        tvBalance = findViewById(R.id.tv_balance)
        tvExpense = findViewById(R.id.tv_expenses)
        tvIncome = findViewById(R.id.tv_incomes)
        tvMonth = findViewById(R.id.tv_month)
        ivBackMonth = findViewById(R.id.iv_prev_month)
        ivNextMonth = findViewById(R.id.iv_next_month)
        fabAdd = findViewById(R.id.fb_add)
        recyEntries = findViewById(R.id.recy_entries)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, NewExpenseActivity::class.java))
        }
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }

        findViewById<ImageView>(R.id.iv_groups).setOnClickListener {
            startActivity(Intent(this,ExpenseGroupsActivity::class.java))
        }
        tvExpense.setOnClickListener {
            expenseType = 1
            loadEntries()
        }

        tvIncome.setOnClickListener {
            expenseType = 2
            loadEntries()
        }
        tvBalance.setOnClickListener {
            expenseType = 0
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
        val expensesArray = db.getExpenseMonth(expenseType, startTime, endTime)
        val adapterEntries = ExpensesRecyAdapter(expensesArray)
        recyEntries.adapter = adapterEntries
        recyEntries.layoutManager = LinearLayoutManager(this)

        if(expenseType == 0) {
            expArray =
                expensesArray.filter { it.type == NewExpenseActivity.ExpenseType.EXPENSE.toString() }
            incArray =
                expensesArray.filter { it.type == NewExpenseActivity.ExpenseType.INCOME.toString() }

            totalExpense = expArray.sumOf { it.amount }
            totalIncome = incArray.sumOf { it.amount }

            tvExpense.setText(totalExpense.toString())
            tvIncome.setText(totalIncome.toString())
            tvBalance.setText((totalIncome - totalExpense).toString())
        }

        tvBalance.setTextColor(getColor(R.color.black))
        tvExpense.setTextColor(getColor(R.color.black))
        tvIncome.setTextColor(getColor(R.color.black))

        when(expenseType){
            0-> tvBalance.setTextColor(getColor(R.color.blue))
            1-> tvExpense.setTextColor(getColor(R.color.blue))
            2-> tvIncome.setTextColor(getColor(R.color.blue))
        }

        tvMonth.setText(sdf.format(calendarMonth.time))
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