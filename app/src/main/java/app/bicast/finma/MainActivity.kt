package app.bicast.finma

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import app.bicast.finma.utils.GDriveHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.FileInputStream
import java.io.IOException
import java.util.Collections


class MainActivity : AppCompatActivity() {
    lateinit var btnLogin:Button
    lateinit var etUserName: EditText
    lateinit var etPassword: EditText
    var gClient :GoogleApiClient? = null
    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
//        if (result.resultCode == Activity.RESULT_OK) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data!!)
            if(result!!.isSuccess) {
                Log.d("Result", "Success")
                checkGDrive()
            }else
                Log.d("Result","failed")
//        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogin = findViewById(R.id.bt_login)
        etUserName = findViewById(R.id.edt_user_name)
        etPassword = findViewById(R.id.edt_password)

        val go = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        gClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, GoogleApiClient.OnConnectionFailedListener {

            })
            .addApi(Auth.GOOGLE_SIGN_IN_API,go)
            .build()

        btnLogin.setOnClickListener {
//            val intent = Auth.GoogleSignInApi.getSignInIntent(gClient!!)
//            startForResult.launch(intent)
            startActivity(Intent(this,SettingsActivity::class.java))
        }
    }

    fun checkGDrive(){
        if(!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this),
                Scope(Scopes.DRIVE_FILE),Scope(Scopes.EMAIL))){
            GoogleSignIn.requestPermissions(this,1,
                GoogleSignIn.getLastSignedInAccount(this),
                Scope(Scopes.DRIVE_FILE),Scope(Scopes.EMAIL))
        }else{
            Toast.makeText(this,"Permission und", Toast.LENGTH_SHORT).show()
            initDrive()
        }
    }

    fun initDrive(){
        val mAccount = GoogleSignIn.getLastSignedInAccount(this)
        val creds = GoogleAccountCredential.usingOAuth2(this,Collections.singleton(Scopes.DRIVE_FILE))
        creds.setSelectedAccount(mAccount?.account)
        val driveObj = Drive.Builder(AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            creds)
            .setApplicationName("Drive integr 3")
            .build()
//        getOldFiles(driveObj)
        handleFile(driveObj)
    }

    fun handleFile(drive: Drive){
        val pathRestore = getFilesDir()
        val letDirectoryRestore = java.io.File(pathRestore, "BackupRestore")
        letDirectoryRestore.mkdirs()
        val fileRestore = java.io.File(letDirectoryRestore, "Records.txt")

        val helperDrive = GDriveHelper(drive)
        helperDrive.getDBFile(fileRestore).addOnSuccessListener {
            Log.d("Tav",it!!.createdTime.toString())
        }
        val path = getFilesDir()
        val letDirectory = java.io.File(path, "BackupTemp")
        letDirectory.mkdirs()
        val file = java.io.File(letDirectory, "Records.txt")
        file.writeText("this is my boss")
        helperDrive.putDBFile(file)
    }

    fun getOldFiles(drive: Drive){
        AsyncTask.execute(Runnable {
            var pageToke:String? = null
            var list:FileList
            do{
                list = drive.files().list()
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToke)
                    .execute()
                pageToke = list.nextPageToken
            }while (pageToke!=null)

            val files = list.files
            Log.d("REs",files.size.toString()+" : items -- "+files.toString())
        })
    }

    fun operateFiles(drive :Drive){

        val root: List<String>
        root = listOf("root")

        val metadata: File = File()
            .setParents(root)
            .setMimeType("application/vnd.google-apps.folder")
            .setName("MyFinma")

        AsyncTask.execute(Runnable {
            val googleFile: File = drive.files().create(metadata).execute()
                ?: throw IOException("Null result when requesting file creation.")
            Log.d("Filed","file id "+googleFile.id)
        })

    }
}