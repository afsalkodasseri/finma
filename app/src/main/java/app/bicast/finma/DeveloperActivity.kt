package app.bicast.finma

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.adapter.AppUsersRecyAdapter
import app.bicast.finma.adapter.NotificationRecyAdapter
import app.bicast.finma.databinding.ActivityDeveloperBinding
import app.bicast.finma.db.models.AppLogItem
import app.bicast.finma.db.models.AppNotificationItem
import app.bicast.finma.db.models.AppUserItem
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

class DeveloperActivity : AppCompatActivity() {
    lateinit var recyUsers : RecyclerView
    lateinit var recyNotifications : RecyclerView
    lateinit var tvUserCount : TextView
    lateinit var tvNotifications : TextView
    lateinit var tvDAU : TextView
    lateinit var binding : ActivityDeveloperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_developer)
        recyUsers = findViewById(R.id.recy_users)
        recyNotifications = findViewById(R.id.recy_notifications)
        tvUserCount = findViewById(R.id.tv_users)
        tvNotifications= findViewById(R.id.tv_notifications)
        tvDAU = findViewById(R.id.tv_dau)

        loadData()

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        findViewById<NestedScrollView>(R.id.scroll_parent).isNestedScrollingEnabled = false
    }

    fun loadData(){
        binding.llLoading.visibility = View.VISIBLE
        val reqQue = Volley.newRequestQueue(applicationContext)
        val reqUrl = "https://sendnoti-7ftcoksyjq-uc.a.run.app/finma/app_summary"
        val stringReq = object : StringRequest(Method.POST,reqUrl,{
                response->
            try{
                Log.d("HOME","resp ${response.toString()}")
                val jbResp = JSONObject(response)
                val stat = jbResp.getString("status")
                val notificationList = ArrayList<AppNotificationItem>()
                val appUsersList = ArrayList<AppUserItem>()
                val logsNewUserList = ArrayList<AppLogItem>()
                if(stat=="success"){
                    val notificationArray = jbResp.getJSONArray("notifications")
                    val logArray = jbResp.getJSONArray("logs")
                    val deviceArray = jbResp.getJSONArray("device")
                    var timeSeconds = 0L
                    for (i in 0 until notificationArray.length()){
                        val itemObj = notificationArray.getJSONObject(i)
                        val title = itemObj.getString("title")
                        val body = itemObj.getString("body")
                        timeSeconds = itemObj.getJSONObject("createdAt").getLong("_seconds")
                        notificationList.add(AppNotificationItem(title,body,timeSeconds))
                    }
                    for (i in 0 until logArray.length()){
                        val itemObj = logArray.getJSONObject(i)
                        val id = itemObj.getString("device_id")
                        val log = itemObj.getString("device_log")
                        val loggedAtSeconds = itemObj.getJSONObject("loggedAt").getLong("_seconds")
                        var event = ""
                        try{
                            event = JSONObject(log).getString("event")
                        }catch (e :JSONException){
                            event = log
                        }
                        logsNewUserList.add(AppLogItem(log,id,loggedAtSeconds,event))
                    }
                    for (i in 0 until deviceArray.length()){
                        val itemObj = deviceArray.getJSONObject(i)
                        val device_name = itemObj.getString("device_name")
                        var device_id = ""
                        try{
                            device_id = itemObj.getString("device_id")
                        }catch (e :JSONException){
                            device_id = itemObj.getString("id")
                        }
                        var time_zone = ""
                        try{
                            time_zone = itemObj.getString("time_zone")
                        }catch (e :JSONException){
                            time_zone = "old_api"
                        }
                        var dateStr = ""
                        var timeSecondsTemp = 0L
                        try{
                            val createdAt = itemObj.getJSONObject("createdAt").getLong("_seconds")
                            timeSecondsTemp = createdAt
                            dateStr = SimpleDateFormat("hh:mm aa, dd MMM yy", Locale.ENGLISH).format(
                                Date(createdAt*1000))
                        }catch (e :JSONException){
                            dateStr = "old_api"
                        }
                        appUsersList.add(AppUserItem(device_name,device_id,dateStr,time_zone,timeSecondsTemp))
                    }

                    val uniqueAppUsers = appUsersList.distinctBy { it.id }
                    val sortedAppUsers = uniqueAppUsers.sortedByDescending { it.timeSeconds }
                    val newUserLogs = logsNewUserList.filter { it.event == "open" }

                    val adapterNotification = NotificationRecyAdapter(notificationList)
                    recyNotifications.adapter =  adapterNotification
                    recyNotifications.layoutManager = LinearLayoutManager(applicationContext)

                    val adapterUsers = AppUsersRecyAdapter(sortedAppUsers)
                    recyUsers.adapter =  adapterUsers
                    recyUsers.layoutManager = LinearLayoutManager(applicationContext)

                    tvUserCount.text = sortedAppUsers.size.toString()
                    tvNotifications.text = notificationList.size.toString()
                    tvDAU.text = newUserLogs.size.toString()


                    binding.llLoading.visibility = View.GONE
                }
            }catch (e :Exception){
                Log.d("HOME","error ${e.toString()}")
//                        Toast.makeText(applicationContext,"exc: "+e.toString(),Toast.LENGTH_SHORT).show()
            }

        },{
                error->
            Log.d("HOME","error ${error.toString()}")
//                Toast.makeText(applicationContext,"error : "+error.toString(),Toast.LENGTH_SHORT).show()
        }){
            override fun getBodyContentType(): String {
                return "application/json"
            }
        }
        reqQue.add(stringReq)
    }
}