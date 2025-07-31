package app.bicast.finma.utils

import android.content.Context
import android.provider.Settings.Secure
import android.util.Log
import android.widget.Toast
import app.futured.donut.BuildConfig
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Date
import java.util.TimeZone

class FirebaseLogUtils {
    companion object{
        fun logEvent(context :Context, event :String){
            val id = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
            val log = JSONObject()
            log.put("loggedAt", Date().toString())
            log.put("time_zone", TimeZone.getDefault().id)
            log.put("event",event)
            val reqQue = Volley.newRequestQueue(context)
            val reqUrl = "https://sendnoti-7ftcoksyjq-uc.a.run.app/finma/log"
            val stringReq = object : StringRequest(Method.POST,reqUrl,{
                    response->
                try{
                    Log.d("HOME","resp ${response.toString()}")
                    val jbResp = JSONObject(response)
                    val stat = jbResp.getString("status")
                }catch (e :Exception){
                    Log.d("HOME","error ${e.toString()}")
//                        Toast.makeText(applicationContext,"exc: "+e.toString(),Toast.LENGTH_SHORT).show()
                }

            },{
                    error->
                Log.d("HOME","error ${error.toString()}")
//                Toast.makeText(applicationContext,"error : "+error.toString(),Toast.LENGTH_SHORT).show()
            }){
                override fun getBody(): ByteArray {
                    val params = HashMap<String, String>()
                    params["log_data"] = log.toString()
                    params["id"] = id
                    return JSONObject(params as Map<String,String>).toString().toByteArray()
                }

                override fun getBodyContentType(): String {
                    return "application/json"
                }
            }
            reqQue.add(stringReq)
        }
    }
}