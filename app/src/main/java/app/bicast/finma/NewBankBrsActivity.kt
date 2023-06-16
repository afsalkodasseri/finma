package app.bicast.finma

import android.app.DatePickerDialog
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
import androidx.appcompat.app.AppCompatActivity
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.BankBrs
import app.bicast.finma.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewBankBrsActivity : AppCompatActivity() {
    val calTime: Calendar = Calendar.getInstance()
    val sdf: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    val db : dbSql = dbSql(this)

    lateinit var etName : AutoCompleteTextView
    lateinit var tvDate : TextView
    lateinit var ivDelete : ImageView
    lateinit var etAmount : EditText
    lateinit var brsType : LinearLayout
    lateinit var tvBrsType : TextView
    lateinit var btAdd : Button
    lateinit var checkMonthly : CheckBox
    var bankBrs: BankBrs? = null
    var multiMode = false
    var monthlyIncome = false
    var BrsType = BankBrs.Typ.BANK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_bank_brs)

        btAdd = findViewById(R.id.bt_add)
        etName = findViewById(R.id.edt_name)
        etAmount = findViewById(R.id.edt_amount)
        tvDate = findViewById(R.id.tv_date)
        ivDelete = findViewById(R.id.iv_delete)
        brsType = findViewById(R.id.ll_brs_type)
        tvBrsType = findViewById(R.id.tv_brs_type)
        checkMonthly = findViewById(R.id.check_monthly)

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        bankBrs = intent.getParcelableExtra("brs")

        tvBrsType.setText(BrsType.toString())
        brsType.setOnClickListener {
            BrsType = if(BrsType== BankBrs.Typ.CASH) BankBrs.Typ.BANK else BankBrs.Typ.CASH
            tvBrsType.setText(BrsType.toString())
        }

        checkMonthly.setOnCheckedChangeListener { buttonView, isChecked ->
            multiMode = isChecked
        }

        findViewById<CheckBox>(R.id.check_monthly).setOnCheckedChangeListener { buttonView, isChecked ->
            monthlyIncome = isChecked
        }

        calTime.set(Calendar.HOUR_OF_DAY,0)
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
            val brsType = BrsType

            if(amount.isEmpty() || name.isEmpty()){
                Toast.makeText(this,"Enter valid details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(bankBrs==null) {
                db.addBrs(
                    BankBrs(
                        null,
                        name,
                        amount.toInt(),
                        brsType.toString(),
                        calTime.timeInMillis,
                        if (monthlyIncome) 1 else 0
                    )
                )

                Toast.makeText(this, "Inserted successfully", Toast.LENGTH_SHORT).show()
                clearEntry(etName, etAmount)
                if(!multiMode)
                    finish()
            }else{
                bankBrs!!.amount = amount.toInt()
                bankBrs!!.type = brsType.toString()
                bankBrs!!.name = name
                bankBrs!!.monthlyIncome = if (monthlyIncome) 1 else 0
                bankBrs!!.dateTime = calTime.timeInMillis
                db.updateBrs(bankBrs!!)
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        ivDelete.setOnClickListener {
            if(bankBrs!=null){
                db.deleteBrs(bankBrs!!)
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        checkForUpdate()
    }

    private fun checkForUpdate(){
        if (bankBrs!=null){
            etName.setText(bankBrs!!.name)
            calTime.timeInMillis = bankBrs!!.dateTime
            tvDate.setText(sdf.format(calTime.time))
            ivDelete.visibility = View.VISIBLE
            etAmount.setText(bankBrs!!.amount.toString())
            BrsType = BankBrs.Typ.valueOf(bankBrs!!.type)
            checkMonthly.isChecked = bankBrs!!.monthlyIncome==1
            tvBrsType.setText(BrsType.toString())
            btAdd.setText("Save")
        }
    }


    private fun clearEntry(etName : EditText, etAmount : EditText){
        etName.setText("")
        etAmount.setText("")
    }
}