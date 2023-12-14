package app.bicast.finma

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import app.bicast.finma.db.dbSql
import app.bicast.finma.utils.GDriveHelper
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Collections

class SettingsActivity : AppCompatActivity() {
    val TAG = this.javaClass.simpleName
    lateinit var tvAccount : TextView
    lateinit var btBackup : Button
    lateinit var btRestore : Button
    lateinit var btExport : Button
    lateinit var btImport : Button

    val db = dbSql(this)

    //for google account
    var mGoogleAccount : GoogleSignInAccount? = null
    var mGoogleDrive :Drive? = null
    var helperDrive : GDriveHelper? = null
    var gClient : GoogleApiClient? = null
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data!!)
                if(googleSignInResult!!.isSuccess) {
                    Log.d(TAG, "Successfully signed in")
                    mGoogleAccount = googleSignInResult.signInAccount
                    tvAccount.setText(mGoogleAccount!!.email.toString())
                    checkGDrivePermission()
                }else
                    Log.d(TAG,"Sign in failed")
            }else
                Log.d(TAG,"Sign in failed from google api")
    }

    //for file picker result
    val filePickerResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedFile = result.data?.data // The URI with the location of the file
            if (selectedFile != null) {
                restoreFile(selectedFile)
            }else{
                Log.d(TAG,"file is null")
            }
        }else{
            Log.d(TAG,"file result is not ok")
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }
        tvAccount = findViewById(R.id.tv_google_account)
        btBackup = findViewById(R.id.bt_backup)
        btRestore = findViewById(R.id.bt_restore)
        btExport = findViewById(R.id.bt_export)
        btImport = findViewById(R.id.bt_import)

        val go = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        gClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this) {
                Log.d(TAG, "Google api on connection failed " + it.errorMessage)
            }
            .addApi(Auth.GOOGLE_SIGN_IN_API,go)
            .build()

        mGoogleAccount = GoogleSignIn.getLastSignedInAccount(this)
        var googleEmail = "Not signed in"
        if(mGoogleAccount!=null){
            googleEmail = mGoogleAccount!!.email.toString()
            initGDriveIfPermission()
        }
        tvAccount.setText(googleEmail)

        btBackup.setOnClickListener {
            if(mGoogleAccount == null){
                val intent = Auth.GoogleSignInApi.getSignInIntent(gClient!!)
                startForResult.launch(intent)
            }else if(mGoogleDrive == null){
                checkGDrivePermission()
            }else{
                backupGDrive()
            }
        }

        btRestore.setOnClickListener {
            if(mGoogleAccount == null){
                val intent = Auth.GoogleSignInApi.getSignInIntent(gClient!!)
                startForResult.launch(intent)
            }else if(mGoogleDrive == null){
                checkGDrivePermission()
            }else{
                restoreGDrive()
            }
        }

        btExport.setOnClickListener {
            shareBackup()
        }

        btImport.setOnClickListener {
            importBackup()
        }
    }
    fun importBackup(){
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)
        filePickerResult.launch(Intent.createChooser(intent, "Select a file"))
    }
    fun restoreGDrive(){
        val cachePath = File(getCacheDir(), "restores")
        cachePath.mkdirs()
        val restoreFile = File(cachePath,  "Records.txt")
        val contentUri = FileProvider.getUriForFile(this, packageName+".fileprovider", restoreFile)
        helperDrive!!.getDBFile(restoreFile).addOnSuccessListener {
            Toast.makeText(this,"back up files found",Toast.LENGTH_SHORT).show()
            restoreFile(contentUri)
        }
    }

    fun restoreFile(uri: Uri){
        try {
            val `in`: InputStream? = contentResolver.openInputStream(uri)
            val r = BufferedReader(InputStreamReader(`in`))
            val total = StringBuilder()
            var line: String?
            while (r.readLine().also { line = it } != null) {
                total.append(line).append('\n')
            }
            val inputAsString = total.toString()
            val jbData = JSONObject(inputAsString)
            val dataVersion = jbData.getInt("version")
            Toast.makeText(this,"imported db version "+dataVersion,Toast.LENGTH_SHORT).show()
            db.putMetadata(jbData)
        } catch (e: Exception) {
            Toast.makeText(this,"e $e",Toast.LENGTH_SHORT).show()
        }
    }
    fun shareBackup(){
        val newFile = getBackupFile()
        val contentUri = FileProvider.getUriForFile(this, packageName+".fileprovider", newFile)
        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(Intent.createChooser(shareIntent, "Choose an app"))
        }
    }
    fun backupGDrive(){
        val file = getBackupFile()
        //check data folder has permission to upload drive
        helperDrive!!.putDBFile(file).addOnSuccessListener {
            Toast.makeText(this,"Backed up successfully",Toast.LENGTH_SHORT).show()
        }
    }

    fun getBackupFile() :File {
        val jaTables = db.getMetadata()
        //creating files inside data folder root
        val path = getFilesDir()
        val letDirectory = File(path, "Backup")
        letDirectory.mkdirs()
        val file = File(letDirectory, "Records.txt")
        file.writeText(jaTables.toString())
        //creating files inside data folder storage
        val cachePath = File(getCacheDir(), "backups")
        val newFile = File(cachePath,  "Records.txt")
        if(newFile.exists())
            newFile.delete()
        file.copyTo(newFile)
        //return file from storage
        return newFile
    }
    fun initDrive(){
        val creds = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(Scopes.DRIVE_FILE))
        creds.setSelectedAccount(mGoogleAccount?.account)
        mGoogleDrive = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            creds)
            .setApplicationName("Drive integr 3")
            .build()

        helperDrive = GDriveHelper(mGoogleDrive!!)
    }
    fun checkGDrivePermission() :Boolean {
        if(!GoogleSignIn.hasPermissions(mGoogleAccount, Scope(Scopes.DRIVE_FILE), Scope(Scopes.EMAIL))){
            GoogleSignIn.requestPermissions(this,1, mGoogleAccount, Scope(Scopes.DRIVE_FILE), Scope(Scopes.EMAIL))
            return false
        }else{
            if(mGoogleDrive==null)
                initDrive()
            return true
        }
    }

    fun initGDriveIfPermission() {
        if(GoogleSignIn.hasPermissions(mGoogleAccount, Scope(Scopes.DRIVE_FILE), Scope(Scopes.EMAIL))){
            if(mGoogleDrive==null)
                initDrive()
        }
    }

}