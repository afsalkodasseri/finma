package app.bicast.finma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.EntryRecyAdapter
import app.bicast.finma.R
import app.bicast.finma.db.dbSql
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ReceivedActivity : AppCompatActivity() {
    val db: dbSql = dbSql(this)
    lateinit var recyEntries : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_received)

        val fabAdd : FloatingActionButton = findViewById(R.id.fb_add)
        recyEntries = findViewById(R.id.recy_entries)
        fabAdd.setOnClickListener {
            startActivity(Intent(this, EntryActivity::class.java))
        }
    }

    private fun loadEntries(){
        val entriesArray = db.getEntries(2)
        val adapterEntries = EntryRecyAdapter(entriesArray)
        recyEntries.adapter = adapterEntries
        recyEntries.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        loadEntries()
    }
}