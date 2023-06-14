package app.bicast.finma

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.PeopleRecyAdapter
import app.bicast.finma.db.dbSql
import app.bicast.finma.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class IndividualsActivity : AppCompatActivity() {
    val db: dbSql = dbSql(this)
    lateinit var recyPeoples :RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individuals)

        val fabAdd : FloatingActionButton = findViewById(R.id.fb_add)
        recyPeoples = findViewById(R.id.recy_peoples)
        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        fabAdd.setOnClickListener {
            startActivity(Intent(this, EntryActivity::class.java))
        }


    }

    private fun loadNames(){
        val namesArray = db.getUserBalances()
        val adapterNames = PeopleRecyAdapter(namesArray)
        recyPeoples.adapter = adapterNames
        recyPeoples.layoutManager = LinearLayoutManager(this)
//        Toast.makeText(this,namesArray.size.toString()+" items",Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadNames()
    }
}