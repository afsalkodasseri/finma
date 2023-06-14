package app.bicast.finma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.R
import app.bicast.finma.adapter.EntryRecyAdapter
import app.bicast.finma.db.dbSql
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class DebtsActivity : AppCompatActivity() {
    val db: dbSql = dbSql(this)
    lateinit var recyEntries : RecyclerView
    lateinit var tlType : TabLayout
    var debtsType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debts)

        val fabAdd : FloatingActionButton = findViewById(R.id.fb_add)
        recyEntries = findViewById(R.id.recy_entries)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, EntryActivity::class.java))
        }
        tlType = findViewById(R.id.tl_type)
        tlType.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                debtsType = tab!!.position
                loadEntries()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
    }


    private fun loadEntries(){
        val entriesArray = db.getEntries(debtsType)
        val adapterEntries = EntryRecyAdapter(entriesArray)
        recyEntries.adapter = adapterEntries
        recyEntries.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }
}