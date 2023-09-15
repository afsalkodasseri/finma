package app.bicast.finma

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class WorkHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_home)

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
    }
}