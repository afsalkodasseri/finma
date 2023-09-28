package app.bicast.finma

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.MinBalanceEntryAdapter
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.EntryRowItem
import app.bicast.finma.db.models.RowItem
import app.bicast.finma.utils.DateUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MinBalActivity : AppCompatActivity() {
    lateinit var recyEntries: RecyclerView
    lateinit var tvTotal: TextView
    lateinit var tvAverage: TextView
    lateinit var tvNeed: TextView
    lateinit var tvMonth: TextView
    lateinit var ivBackMonth: ImageView
    lateinit var ivNextMonth: ImageView
    lateinit var fabAdd: FloatingActionButton
    val db: dbSql = dbSql(this)

    var PDF_LINE_SEPERATOR = ""
    var MIN_BALANCE = 0
    val calendarMonth : Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_min_bal)

        tvTotal = findViewById(R.id.tv_total)
        tvAverage = findViewById(R.id.tv_average)
        tvNeed = findViewById(R.id.tv_need)
        tvMonth = findViewById(R.id.tv_month)
        ivBackMonth = findViewById(R.id.iv_prev_month)
        ivNextMonth = findViewById(R.id.iv_next_month)
        fabAdd = findViewById(R.id.fb_add)
        recyEntries = findViewById(R.id.recy_entries)
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        fabAdd.setOnClickListener {
            if (checkReadExternalStoragePermission())
                getFile()
            else
                askPermission()
        }

        PDF_LINE_SEPERATOR = getSharedPreferences("min_bal",0).getString("line_seperator","1192")?:"1192"
        MIN_BALANCE = getSharedPreferences("min_bal",0).getInt("minimum_balance",5000)

        loadData(calendarMonth.time)

        ivBackMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH,-1)
            loadData(calendarMonth.time)
        }
        ivNextMonth.setOnClickListener {
            calendarMonth.add(Calendar.MONTH,1)
            loadData(calendarMonth.time)
        }
    }

    private fun loadData(dataDate: Date) {
        val monthName = SimpleDateFormat("MMMM, yy", Locale.ENGLISH).format(dataDate)
        val startTime = DateUtils.monthStartTime(dataDate)
        val endTime = DateUtils.monthEndTime(dataDate)
        val totalSum: Int = db.getBalanceForMonth(startTime,endTime)
        val tempCal = Calendar.getInstance()
        tempCal.time = dataDate
        val totalDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val averageBalance = totalSum / totalDay
        val needBalance = (totalDay*MIN_BALANCE) - totalSum
        tvMonth.text = monthName
        tvTotal.text = "" + totalSum
        tvAverage.setText("₹$averageBalance")
        tvNeed.setText("$needBalance")
        if(averageBalance>=MIN_BALANCE)
            tvAverage.setTextColor(getColor(R.color.balance_green))
        else
            tvAverage.setTextColor(getColor(R.color.balance_red))

        //load entries
        val entries: List<EntryRowItem> = db.getMinBalEntries(startTime,endTime)
        val adapterSummary = MinBalanceEntryAdapter(entries)
        recyEntries.setAdapter(adapterSummary)
        recyEntries.setLayoutManager(LinearLayoutManager(applicationContext))
    }

    fun checkReadExternalStoragePermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),100)
        }
    }

    private fun getFile() {
        var chooseIntent = Intent(Intent.ACTION_GET_CONTENT)
        chooseIntent.type = "*/*"
        chooseIntent = Intent.createChooser(chooseIntent, "Choose File")
        resultLauncher.launch(chooseIntent)
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val uri = data!!.data
            if(uri!=null) {
                val ioStreamReader = contentResolver.openInputStream(uri)
                if (ioStreamReader != null) {
                    parsePdf(ioStreamReader)
                }
            }
        }
    }

    fun parsePdf(fileStream :InputStream){
        var extracted = ""
        val reader = PdfReader(fileStream)
        val pageCount = reader.numberOfPages
        for(i in 0 until pageCount){
            extracted = extracted + PdfTextExtractor.getTextFromPage(reader,i+1).trim()+"\n"
        }
        val parts = extracted.split("for the period")
        val rowParts = extracted.split("Init.", limit = 2)
        val toDate = parts.get(1).split("To : ")[1].substring(0,10)
        val toMonth = toDate.substring(3)
        val rowStart = toMonth
        val entryRowsOnly = rowParts.get(1).substring(0,rowParts.get(1).indexOf("CLOSING BALANCE"))
        val rows = entryRowsOnly.split(Regex(" "+PDF_LINE_SEPERATOR+" | "+PDF_LINE_SEPERATOR+"\n"))

        val monthDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(toDate)
        val monthName = SimpleDateFormat("MMMM, yy",Locale.ENGLISH).format(monthDate)

        val bankItems = ArrayList<RowItem>()
        val singleItems = ArrayList<RowItem>()
        for(i in 0 until rows.size){
            val tempRow = rows[i]
            val startPos = tempRow.indexOf(rowStart)-3
            if(!(startPos<0)){
                val actualRow = tempRow.substring(startPos)
                val rowDate = actualRow.substring(0,10)
                val startPosAmount = getLastPos(actualRow,' ')
                val amountString = actualRow.substring(startPosAmount)
                bankItems.add(RowItem(rowDate,amountString))
            }
        }

        var date=""
        var tempItem = RowItem(bankItems.get(0).date,bankItems.get(0).amount)
        date=tempItem.date
        for(i in 0 until bankItems.size) {
            if(date.equals(bankItems.get(i).date, true))
                tempItem.amount=bankItems.get(i).amount
            else{
                singleItems.add(tempItem)
                tempItem = RowItem(bankItems.get(i).date,bankItems.get(i).amount)
                date=tempItem.date
            }
        }
        //add last item
        singleItems.add(tempItem)
        reader.close()
        //calculate output
        val entryFinal = generateAiEntries(singleItems,monthDate)
        val totalAmount=calculateAmount(singleItems,monthDate)

        //insert into database
        db.upsertEntries(entryFinal)
        db.insertBalance(DateUtils.startTime(monthDate.time),totalAmount.toString())

        loadData(monthDate);
    }

    fun getLastPos(source :String,founder :Char) :Int{
        for(i in source.length-1 downTo 0) {
            if(source[i]==founder) {
                return i
            }
        }
        return -1
    }

    private fun calculateAmount(items: ArrayList<RowItem>, monthDate: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = monthDate
        calendar[Calendar.DAY_OF_MONTH] = 1
        val sdfDmy = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var amountTemp = 0
        var sum = 0
        var tempRow = items[0]
        items.removeAt(0)
        for (i in 0 until maxDay) {
            calendar[Calendar.DAY_OF_MONTH] = i + 1
            if (sdfDmy.format(calendar.time) == tempRow.date) {
                amountTemp = tempRow.amount.toFloat().toInt()
                sum = sum + amountTemp
                if (items.size > 0) {
                    tempRow = items[0]
                    items.removeAt(0)
                    if (items.isEmpty()) amountTemp = 0
                } else {
                    amountTemp = 0
                }
            } else {
                if (i == 0) amountTemp = 0
                sum = sum + amountTemp
            }
        }
        return sum
    }

    private fun generateAiEntries(source: List<RowItem>, monthDate: Date): List<EntryRowItem> {
        val calendar = Calendar.getInstance()
        calendar.time = monthDate
        calendar[Calendar.DAY_OF_MONTH] = 1
        val result: MutableList<EntryRowItem> = java.util.ArrayList<EntryRowItem>()
        val sdfDmy = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var amountTemp = 0
        var sum = 0
        val items: ArrayList<RowItem> = ArrayList(source)
        var tempRow: RowItem?
        for (i in 0 until maxDay) {
            calendar[Calendar.DAY_OF_MONTH] = i + 1
            if (items.size > 0) {
                tempRow = items[0]
                if (sdfDmy.format(calendar.time) == tempRow!!.date) {
                    amountTemp = tempRow.amount.toFloat().toInt()
                    sum = sum + amountTemp
                    result.add(EntryRowItem(DateUtils.startTime(calendar.timeInMillis), amountTemp, "From Statement"))
                    items.removeAt(0)
                    if (items.isEmpty())
                        amountTemp = 0

                } else {
                    sum = sum + amountTemp
                    result.add(EntryRowItem(DateUtils.startTime(calendar.timeInMillis),amountTemp,"AI Generated"))
                }
            }
        }
        return result
    }
}