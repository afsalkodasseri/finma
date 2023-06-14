package app.bicast.finma

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import app.bicast.finma.db.dbSql
import app.bicast.finma.db.models.BankBrs
import app.bicast.finma.db.models.Entry
import app.bicast.finma.db.models.User
import app.bicast.finma.R
import com.google.android.material.tabs.TabLayout
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EntryActivity : AppCompatActivity() {
    enum class PaymentType{PAID,RECEIVED}
    var entryType: PaymentType = PaymentType.PAID
    val calTime:Calendar = Calendar.getInstance()
    val sdf:SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    val db : dbSql = dbSql(this)
    var namesArray :ArrayList<User> = ArrayList()
    var userMob: String = ""
    lateinit var userPic: ByteArray
    lateinit var etName :AutoCompleteTextView
    lateinit var llNameContainer :LinearLayout
    lateinit var tvDate :TextView
    lateinit var ivDelete :ImageView
    lateinit var etDescription :EditText
    lateinit var etAmount :EditText
    lateinit var tlType :TabLayout
    lateinit var brsType : LinearLayout
    lateinit var tvBrsType : TextView
    lateinit var btAdd : Button
    var entry: Entry? = null
    var BrsType = BankBrs.Typ.BANK

    @SuppressLint("Range")
    val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK){
            // There are no request codes
            val data = result.data?.data

            data?.let { val cursor = contentResolver.query(data, null, null, null, null)

                cursor?.let {
                    if (it.moveToFirst()) {
                        val name =it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                        if (Integer.parseInt( it.getString( it.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0 // Check if the contact has phone numbers
                        ) {
                            val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))

                            val phonesCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                null,
                                null
                            )

                            val numbers = mutableSetOf<String>()
                            phonesCursor?.let {
                                while (phonesCursor.moveToNext()) {
                                    val phoneNumber =
                                        phonesCursor.getString(
                                            phonesCursor.getColumnIndex(
                                                ContactsContract.CommonDataKinds.Phone.NUMBER
                                            )
                                        ).replace("-", "").replace(" ", "")
                                    numbers.add(phoneNumber)
                                }

                                Log.d("TAG", "$name $numbers")
                            }
                            phonesCursor?.close()
                            setPhoto(id)
                            userMob = numbers.first()

                        } else {

                            Log.d("TAG", "$name - No numbers")
                        }

                        etName.setText(name)
                    }

                    cursor.close()
                }

            }
        }
        // Handle the returned Uri
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        btAdd = findViewById<Button>(R.id.bt_add)
        etName = findViewById(R.id.edt_name)
        llNameContainer = findViewById(R.id.ll_name_container)
        etAmount = findViewById(R.id.edt_amount)
        etDescription = findViewById(R.id.edt_description)
        tvDate = findViewById(R.id.tv_date)
        ivDelete = findViewById(R.id.iv_delete)
        val ivContact = findViewById<ImageView>(R.id.iv_contact)
        brsType = findViewById(R.id.ll_brs_type)
        tvBrsType = findViewById(R.id.tv_brs_type)

        findViewById<ImageView>(R.id.iv_toolbar_back).setOnClickListener {
            onBackPressed()
        }

        entry = intent.getParcelableExtra<Entry>("entry_id")

        tlType = findViewById(R.id.tl_type)
        tlType.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                entryType = if(tab?.position==0)
                    PaymentType.PAID
                else
                    PaymentType.RECEIVED
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        tvBrsType.setText(BrsType.toString())
        brsType.setOnClickListener {
            BrsType = if(BrsType== BankBrs.Typ.CASH) BankBrs.Typ.BANK else BankBrs.Typ.CASH
            tvBrsType.setText(BrsType.toString())
        }

        calTime.set(Calendar.HOUR,0)
        calTime.set(Calendar.MINUTE,0)
        calTime.set(Calendar.SECOND,0)
        calTime.set(Calendar.MILLISECOND,0)

        tvDate.setText(sdf.format(calTime.time))
        tvDate.setOnClickListener {
            val year = calTime.get(Calendar.YEAR)
            val month = calTime.get(Calendar.MONTH)
            val day = calTime.get(Calendar.DAY_OF_MONTH)
            
            val dateDialog = DatePickerDialog(this,
                R.style.datePickerBlue, { view, year, month, dayOfMonth ->
                calTime.set(Calendar.DAY_OF_MONTH,dayOfMonth)
                calTime.set(Calendar.MONTH,month)
                calTime.set(Calendar.YEAR,year)
                tvDate.setText(sdf.format(calTime.time))
            },year,month,day)
            dateDialog.show()
        }

        loadNames(etName)

        btAdd.setOnClickListener {
            val name = etName.text.toString()
            val amount = etAmount.text.toString()
            val description = etDescription.text.toString()
            val brsType = BrsType

            if(entry==null) {
                val index = namesArray.indexOfFirst { it.name == name } // -1 if not found
                if (index >= 0) {
                    val user = namesArray[index]
                    db.addNewEntry(
                        Entry(
                            user.id ?: 0,
                            amount.toInt(),
                            description,
                            entryType.toString(),
                            calTime.timeInMillis,
                            brsType
                        )
                    )
                } else {
                    db.addNewEntry(
                        Entry(
                            name,
                            userMob,
                            userPic,
                            amount.toInt(),
                            description,
                            entryType.toString(),
                            calTime.timeInMillis,
                            brsType
                        )
                    )
                }
                loadNames(etName)
                Toast.makeText(this, "Inserted successfully", Toast.LENGTH_SHORT).show()
                clearEntry(etName, etAmount, etDescription, tvDate)
            }else{
                entry!!.amount = amount.toInt()
                entry!!.description = description
                entry!!.type = entryType.toString()
                entry!!.dateTime = calTime.timeInMillis
                val tempBrs = entry!!.brs ?: BankBrs(null,"From $entryType Entry",if(entryType== PaymentType.RECEIVED)amount.toInt() else -1+amount.toInt(),brsType.toString(),calTime.timeInMillis,0)
                tempBrs.type = brsType.toString()
                entry!!.brs = tempBrs
                db.addNewEntry(entry!!)
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        ivContact.setOnClickListener {
            if(entry==null)
                getContent.launch(Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI))
            else
                Toast.makeText(this, "Couldn't update name", Toast.LENGTH_SHORT).show()
        }

        ivDelete.setOnClickListener {
            if(entry!=null){
                db.deleteEntry(entry!!)
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        checkForUpdate()
    }

    private fun checkForUpdate(){
        if (entry!=null){
            etName.setText(entry!!.userName)
            etName.isEnabled = false
            llNameContainer.setBackgroundResource(R.drawable.text_box_disabled)
            etName.setTextColor(getColor(R.color.grey_text))

            calTime.timeInMillis = entry!!.dateTime
            tvDate.setText(sdf.format(calTime.time))
            ivDelete.visibility = View.VISIBLE

            etDescription.setText(entry!!.description)
            etAmount.setText(entry!!.amount.toString())
            if(entry!!.type == PaymentType.PAID.toString())
                tlType.selectTab(tlType.getTabAt(0))
            else
                tlType.selectTab(tlType.getTabAt(1))

            BrsType = BankBrs.Typ.valueOf(entry!!.brs?.type?:BrsType.toString())
            tvBrsType.setText(BrsType.toString())
            btAdd.setText("Save")
            entry!!.brs?:let{
                brsType.visibility = View.GONE
            }
        }else{
            // for default photo
            val photo = BitmapFactory.decodeResource(getResources(), R.drawable.image_user)
            val bos = ByteArrayOutputStream()
            photo.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            userPic = bos.toByteArray()
        }

    }

    private fun loadNames(textBox :AutoCompleteTextView){
        namesArray = db.getUsers()
        val adapterNames = ArrayAdapter(this,android.R.layout.simple_list_item_1,namesArray)
        textBox.setAdapter(adapterNames)
        textBox.threshold = 1
    }

    private fun clearEntry(etName :EditText,etAmount :EditText,etDescription :EditText,tvDate :TextView){
        etName.setText("")
        etAmount.setText("")
        etDescription.setText("")
    }

    fun setPhoto(contactId :String){
        var photo = BitmapFactory.decodeResource(getResources(), R.drawable.image_user)

        try {
            if (contactId != null) {
                val inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId.toLong()))
                if (inputStream != null) {
                    photo = BitmapFactory.decodeStream(inputStream)
                }
                if(inputStream != null)
                    inputStream!!.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val bos = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        userPic = bos.toByteArray()
    }
}