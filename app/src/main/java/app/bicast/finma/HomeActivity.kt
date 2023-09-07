package app.bicast.finma

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import app.bicast.finma.R
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.HomeSummaryModel
import app.bicast.finma.utils.DateUtils
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection


class HomeActivity : AppCompatActivity() {
    val requiredPermission = Manifest.permission.READ_CONTACTS
    lateinit var ivWarning :ImageView
    val db = dbSql(this)
    lateinit var tvSummaryDebt :TextView
    lateinit var tvSummaryPeople :TextView
    lateinit var tvSummaryExpense :TextView
    lateinit var tvSummaryAccounts :TextView
    lateinit var tvTotalCash :TextView
    lateinit var tvTotalExpense :TextView
    lateinit var tvTotalBalance :TextView
    lateinit var tvTotalDebts :TextView
    lateinit var tvPercentage :TextView
    lateinit var donutProgressbar :DonutProgressView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val cardDebts = findViewById<CardView>(R.id.card_debts)
        val cardAccounts = findViewById<CardView>(R.id.card_accounts)
        val cardIndividuals = findViewById<CardView>(R.id.card_individuals)
        val cardExpenses = findViewById<CardView>(R.id.card_expense)
        val cardWork = findViewById<CardView>(R.id.card_work)
        val cardMinBal = findViewById<CardView>(R.id.card_minimum_balances)
        ivWarning = findViewById(R.id.iv_warning)

        tvSummaryDebt = findViewById(R.id.tv_debts_summary)
        tvSummaryPeople = findViewById(R.id.tv_individual_summary)
        tvSummaryExpense = findViewById(R.id.tv_expense_summary)
        tvSummaryAccounts = findViewById(R.id.tv_accounts_summary)
        tvTotalCash = findViewById(R.id.tv_total_cash)
        tvTotalExpense = findViewById(R.id.tv_total_expense)
        tvTotalDebts = findViewById(R.id.tv_total_debts)
        tvTotalBalance = findViewById(R.id.tv_total_balance)
        tvPercentage = findViewById(R.id.tv_percentage)
        donutProgressbar = findViewById(R.id.donut_view)

        if (checkPermission())
            ivWarning.visibility = View.GONE
        else
            ivWarning.visibility = View.VISIBLE

        ivWarning.setOnClickListener {
            if(checkPermission())
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show()
            else
                askPermission()
        }

        cardDebts.setOnClickListener {
            startActivity(Intent(this, DebtsActivity::class.java))
        }

        cardAccounts.setOnClickListener {
            startActivity(Intent(this, BankBRSActivity::class.java))
        }

        cardIndividuals.setOnClickListener {
            startActivity(Intent(this, IndividualsActivity::class.java))
        }

        cardExpenses.setOnClickListener {
            startActivity(Intent(this, ExpensesActivity::class.java))
        }

        cardWork.setOnClickListener{
            startActivity(Intent(this, WorkHomeActivity::class.java))
        }

        cardMinBal.setOnClickListener{
            startActivity(Intent(this, MinBalActivity::class.java))
        }
    }

    fun checkPermission() :Boolean{
        val checkVal: Int = ContextCompat.checkSelfPermission(this,requiredPermission)
        return (checkVal == PackageManager.PERMISSION_GRANTED)
    }

    fun askPermission(){
        requestPermissions(arrayOf(requiredPermission), 101)
    }

    override fun onRequestPermissionsResult(requestCode: Int,permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101){
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                ivWarning.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSummary()
    }

    fun loadSummary(){
        val summary = db.getHomeSummary(DateUtils.monthStartTime(),DateUtils.monthEndTime())
        tvSummaryDebt.setText(summary.debtCount.toString() +" "+ if (summary.debtType == "PAID") "Debt" else "Credit")
        tvSummaryExpense.setText(summary.expenseCount.toString() + " expenses")
        tvSummaryPeople.setText(summary.peopleCount.toString() + " people")
        tvSummaryAccounts.setText(summary.cashAmount.toString() +" "+ if (summary.cashType == "CASH") "Cash" else "Bank")
        tvTotalCash.setText(summary.totalCash.toString())
        tvTotalExpense.setText(summary.expenseAmount.toString())
        tvTotalDebts.setText(summary.pureDebtAmount.toString())
        tvTotalBalance.setText(summary.balance.toString())

        donutProgressbar.masterProgress = 0.6f
        if(summary.totalCash>0) {
            var percentage =
                (summary.balance.toFloat() / summary.totalCash.toFloat()) * 100
            percentage = 100 - percentage   //convert balance percentage to used percentage
            tvPercentage.setText(String.format("%.1f", percentage) + "%")

            val section1 = DonutSection(
                "expense",
                getColor(R.color.progres_red),
                summary.expenseAmount.toFloat()
            )
            val section2 = DonutSection(
                "debts",
                getColor(R.color.progres_orange),
                summary.pureDebtAmount.toFloat()
            )
            donutProgressbar.cap = summary.totalCash.toFloat()
            donutProgressbar.submitData(listOf(section1, section2))
        }else{
            tvPercentage.setText("0%")
        }
    }
}