package app.bicast.finma

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    lateinit var btnLogin:Button
    lateinit var etUserName: EditText
    lateinit var etPassword: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.bt_login)
        etUserName = findViewById(R.id.edt_user_name)
        etPassword = findViewById(R.id.edt_password)

        btnLogin.setOnClickListener {
            val name = etUserName.text.toString()
            val password = etPassword.text.toString()
            if(name == "admin" && password == "123"){
                Toast.makeText(this,"Success login",Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
        }
    }
}