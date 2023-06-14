package app.bicast.finma

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.BankBrs
import app.bicast.finma.db.models.Expense
import app.bicast.finma.R
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewExpenseActivity : AppCompatActivity() {
    enum class ExpenseType{EXPENSE,INCOME}
    var expenseType: ExpenseType = ExpenseType.EXPENSE
    val calTime: Calendar = Calendar.getInstance()
    val sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    val db : dbSql = dbSql(this)

    lateinit var etName : AutoCompleteTextView
    lateinit var etDescription : EditText
    lateinit var tvDate : TextView
    lateinit var ivDelete : ImageView
    lateinit var etAmount : EditText
    lateinit var tlType : TabLayout
    lateinit var brsType : LinearLayout
    lateinit var tvBrsType : TextView
    lateinit var btAdd : Button
    var expense: Expense? = null
    var multiMode = false
    var BrsType = BankBrs.Typ.BANK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_expense)

        btAdd = findViewById<Button>(R.id.bt_add)
        etName = findViewById(R.id.edt_name)
        etAmount = findViewById(R.id.edt_amount)
        etDescription = findViewById(R.id.edt_description)
        tvDate = findViewById(R.id.tv_date)
        ivDelete = findViewById(R.id.iv_delete)
        brsType = findViewById(R.id.ll_brs_type)
        tvBrsType = findViewById(R.id.tv_brs_type)

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        expense = intent.getParcelableExtra("expense")

        tlType = findViewById(R.id.tl_type)
        tlType.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                expenseType = if(tab?.position==0)
                    ExpenseType.EXPENSE
                else
                    ExpenseType.INCOME
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        tvBrsType.setText(BrsType.toString())
        brsType.setOnClickListener {
            BrsType = if(BrsType== BankBrs.Typ.CASH) BankBrs.Typ.BANK else BankBrs.Typ.CASH
            tvBrsType.setText(BrsType.toString())
        }

        findViewById<CheckBox>(R.id.check_mutli).setOnCheckedChangeListener { buttonView, isChecked ->
            multiMode = isChecked
        }

        calTime.set(Calendar.HOUR,0)
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


        btAdd.setOnClickListener {
            val name = etName.text.toString()
            val amount = etAmount.text.toString()
            val description = etDescription.text.toString()
            val brsType = BrsType

            if(amount.isEmpty() || name.isEmpty()){
                Toast.makeText(this,"Enter valid details",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(expense==null) {
                    db.upsertExpense(
                        Expense(
                            name,
                            amount.toInt(),
                            description,
                            expenseType.toString(),
                            calTime.timeInMillis,
                            brsType
                        )
                    )

                Toast.makeText(this, "Inserted successfully", Toast.LENGTH_SHORT).show()
                clearEntry(etName, etAmount, etDescription, tvDate)
                if(!multiMode)
                    finish()
            }else{
                expense!!.amount = amount.toInt()
                expense!!.description = description
                expense!!.name = name
                expense!!.type = expenseType.toString()
                expense!!.dateTime = calTime.timeInMillis
                val tempBrs = expense!!.brs ?: BankBrs(null,"From $expenseType Expense",if(expenseType== ExpenseType.INCOME)amount.toInt() else -1+amount.toInt(),brsType.toString(),calTime.timeInMillis,0)
                tempBrs.type = brsType.toString()
                expense!!.brs = tempBrs
                db.upsertExpense(expense!!)
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }


        ivDelete.setOnClickListener {
            if(expense!=null){
                db.deleteExpense(expense!!)
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        checkForUpdate()
    }

    private fun checkForUpdate(){
        if (expense!=null){
            etName.setText(expense!!.name)
            calTime.timeInMillis = expense!!.dateTime
            tvDate.setText(sdf.format(calTime.time))
            ivDelete.visibility = View.VISIBLE

            etDescription.setText(expense!!.description)
            etAmount.setText(expense!!.amount.toString())
            if(expense!!.type == ExpenseType.EXPENSE.toString())
                tlType.selectTab(tlType.getTabAt(0))
            else
                tlType.selectTab(tlType.getTabAt(1))

            BrsType = BankBrs.Typ.valueOf(expense!!.brs?.type?:BrsType.toString())
            tvBrsType.setText(BrsType.toString())
            btAdd.setText("Save")
            expense!!.brs?:let{
                brsType.visibility = View.GONE
            }
        }
    }


    private fun clearEntry(etName :EditText,etAmount :EditText,etDescription :EditText,tvDate :TextView){
        etName.setText("")
        etAmount.setText("")
        etDescription.setText("")
    }

}