package app.bicast.finma.utils

import android.R.attr.mimeType
import android.os.AsyncTask
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.concurrent.Executors


class GDriveHelper(var mDrive: Drive) {
    val TAG = "GDriveHelper"
    val folderName = "Finma"
    val executor = Executors.newSingleThreadExecutor()

    fun getDBFile(downloadFile: java.io.File): Task<File?> {
        return Tasks.call<File?>(executor) {
            //creating file object for output
            var file: File? = null
            //getting all folders this app created
            var pageToken: String? = null
            var list: FileList
            do {
                list = mDrive.files().list()
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute()
                pageToken = list.nextPageToken
            } while (pageToken != null)

            val files = list.files
            Log.d(TAG,"::"+files[0].name.toString())
            val folder = files.firstOrNull { it.name == folderName }
            if (folder != null) {
                Log.d(TAG,"folder backup found")
                val sb = StringBuilder()
                sb.append("'")
                sb.append(folder.id)
                sb.append("'")
                sb.append(" in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                val childList = mDrive.files().list()
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, createdTime)")
                    .setQ(sb.toString())
                    .setOrderBy("modifiedTime desc")
                    .execute()
                val childFiles = childList.files
                file = childFiles.firstOrNull()
                if(file!=null) {
                    Log.d(TAG,"backup files found")
                    val outStream = FileOutputStream(downloadFile)
                    mDrive.files().get(file.id).executeMediaAndDownloadTo(outStream)
                    Log.d(TAG,"backup files downloaded")
                }else{
                    Log.d(TAG,"no backup files found")
                }
            }
            file
        }
    }

    fun putDBFile(file: java.io.File): Task<File?> {
        return Tasks.call<File?>(executor) {
            var fileMeta : File? = null
            //getting all folders this app created
            var pageToken: String? = null
            var list: FileList
            do {
                list = mDrive.files().list()
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute()
                pageToken = list.nextPageToken
            } while (pageToken != null)

            val files = list.files
            var folder = files.firstOrNull { it.name == folderName }
            if (folder == null) {
                Log.d(TAG,"folder backup not found")

                val metadata: File = File()
                    .setParents(listOf("root"))
                    .setMimeType("application/vnd.google-apps.folder")
                    .setName(folderName)

                val newFolder: File = mDrive.files().create(metadata).execute()

                Log.d(TAG,"folder backup created")
                folder = newFolder
            }
            val metadata = File()
                .setParents(listOf(folder.id))
                .setMimeType("text/plain")
                .setName(file.getName())

            val fileContent = FileContent("text/plain", file)

             fileMeta = mDrive.files().create(
                metadata,
                fileContent
            ).execute()

            Log.d(TAG,"backup file created")
            fileMeta
        }
    }

    fun clearDBFileBackups(): Task<Int> {
        return Tasks.call<Int>(executor) {
            //creating file object for output
            var deleteCount = 0
            //getting all folders this app created
            var pageToken: String? = null
            var list: FileList
            do {
                list = mDrive.files().list()
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute()
                pageToken = list.nextPageToken
            } while (pageToken != null)

            val files = list.files
            Log.d(TAG,"::"+files[0].name.toString())
            val folder = files.firstOrNull { it.name == folderName }
            if (folder != null) {
                Log.d(TAG,"folder backup found")
                val sb = StringBuilder()
                sb.append("'")
                sb.append(folder.id)
                sb.append("'")
                sb.append(" in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false")
                val childList = mDrive.files().list()
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setQ(sb.toString())
                    .setOrderBy("modifiedTime")
                    .execute()
                val childFiles = childList.files
                if(childFiles!=null && childFiles.size>5){
                    Log.d(TAG,"older backup files found morethan 5")
                    for (i in 5..childFiles.size-1){
                        mDrive.files().delete(childFiles.get(i).id).execute()
                        deleteCount++
                    }
                }else{
                    Log.d(TAG,"older backup files not found or less than 5")
                }
            }
            deleteCount
        }
    }
}