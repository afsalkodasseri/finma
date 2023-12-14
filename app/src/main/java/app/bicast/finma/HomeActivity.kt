package app.bicast.finma

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.WorkEvent
import app.bicast.finma.utils.DateUtils
import app.bicast.finma.utils.OnSwipeTouchListener
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HomeActivity : AppCompatActivity() {
    val requiredPermission = Manifest.permission.READ_CONTACTS
    lateinit var ivWarning :ImageView
    lateinit var ivBackup :ImageView
    lateinit var ivSettings :ImageView
    val db = dbSql(this)
    lateinit var tvSummaryMonth :TextView
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
    lateinit var tvHolidayLeft :TextView
    lateinit var tvMinBalance :TextView
    val summaryDate = Calendar.getInstance()
    val sdfMonth = SimpleDateFormat("MMMM", Locale.ENGLISH)
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
        ivBackup = findViewById(R.id.iv_backup)
        ivSettings = findViewById(R.id.iv_settings)

        tvSummaryMonth = findViewById(R.id.tv_month)
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
        tvHolidayLeft = findViewById(R.id.tv_work_summary)
        tvMinBalance = findViewById(R.id.tv_min_bal_summary)

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

        tvSummaryMonth.setOnTouchListener(
            object :OnSwipeTouchListener(this@HomeActivity){
                override fun onSwipeLeft(){
                    Log.d("Swipe","left")
                    summaryDate.add(Calendar.MONTH,1)
                    loadSummary()
                }
                override fun onSwipeRight(){
                    Log.d("Swipe","right")
                    summaryDate.add(Calendar.MONTH,-1)
                    loadSummary()
                }
            }
        )

        ivBackup.setOnClickListener {
//            view->showPopup(view)
        }

        ivSettings.setOnClickListener {
            startActivity(Intent(this,SettingsActivity::class.java))
        }
    }

    fun showPopup(v : View){
        val popup = PopupMenu(this, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.backup_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.backup-> {
                    backup()
                }
                R.id.restore-> {
                    restoreFile()
                }
            }
            true
        }
        popup.show()
    }

    fun backup() {
        val jaTables = db.getMetadata()
        val path = getFilesDir()
        val letDirectory = File(path, "Backup")
        letDirectory.mkdirs()
        val file = File(letDirectory, "Records.txt")
        file.writeText(jaTables.toString())
        saveBackup(file)
    }

    fun restore(){
        val path = getExternalFilesDir(null)
        val letDirectory = File(path, "restore")
        letDirectory.mkdirs()
        val file = File(letDirectory, "Records.txt")
        val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
        val jbData = JSONObject(inputAsString)
        val dataVersion = jbData.getInt("version")
        db.putMetadata(jbData)
        loadSummary()
    }

    fun restore(uri: Uri){
        try {
            val `in`: InputStream? = contentResolver.openInputStream(uri)
            val r = BufferedReader(InputStreamReader(`in`))
            val total = StringBuilder()
            var line: String?
            while (r.readLine().also { line = it } != null) {
                total.append(line).append('\n')
            }
            val inputAsString = total.toString()
            val jbData = JSONObject(inputAsString)
            val dataVersion = jbData.getInt("version")
            Toast.makeText(this,"version",Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this,"e $e",Toast.LENGTH_SHORT).show()
        }

//        db.putMetadata(jbData)
//        loadSummary()
    }

    fun saveBackup(file :File){
        val cachePath = File(getCacheDir(), "backups")
        val newFile = File(cachePath,  "Records.txt")
        if(newFile.exists())
            newFile.delete()
        file.copyTo(newFile)
        val contentUri =
            FileProvider.getUriForFile(this, packageName+".fileprovider", newFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }

    fun restoreFile(){
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 111 && resultCode == RESULT_OK) {
            val selectedFile = data?.data // The URI with the location of the file
            if (selectedFile != null) {
                restore(selectedFile)
            }
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
        val summary = db.getHomeSummary(DateUtils.monthStartTime(summaryDate.time),DateUtils.monthEndTime(summaryDate.time))
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

        val holidaysLeft = db.getWorkEvent(Date().time,DateUtils.yearEndTime(), WorkEvent.Typ.HOLIDAY.toString())
        tvHolidayLeft.setText(holidaysLeft.size.toString()+" holidays")

        val totalSum: Int = db.getBalanceForMonth(DateUtils.monthStartTime(summaryDate.time), DateUtils.monthEndTime(summaryDate.time))
        val tempCal = Calendar.getInstance()
        val totalDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val averageBalance = totalSum / totalDay
        tvMinBalance.setText("$averageBalance")

        tvSummaryMonth.setText(sdfMonth.format(summaryDate.time))
    }
}