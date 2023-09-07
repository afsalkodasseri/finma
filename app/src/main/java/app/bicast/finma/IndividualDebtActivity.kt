package app.bicast.finma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.EntryRecyAdapter
import app.bicast.finma.R
import app.bicast.finma.db.dbSql
import com.google.android.material.floatingactionbutton.FloatingActionButton

class IndividualDebtActivity : AppCompatActivity() {
    val db: dbSql = dbSql(this)
    lateinit var recyItems : RecyclerView
    var userId = 0
    var userName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_debt)

        userId = intent.getIntExtra("user_id",0)
        userName = intent.getStringExtra("user_name").toString()

        val fabAdd : FloatingActionButton = findViewById(R.id.fb_add)
        recyItems = findViewById(R.id.recy_peoples)
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        fabAdd.setOnClickListener {
            startActivity(Intent(this, EntryActivity::class.java)
                .putExtra("user_name",userName))
        }


    }

    private fun loadItems(){
        val namesArray = db.getPersonEntries(userId)
        val adapterItems = EntryRecyAdapter(namesArray)
        recyItems.adapter = adapterItems
        recyItems.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        loadItems()
    }
}