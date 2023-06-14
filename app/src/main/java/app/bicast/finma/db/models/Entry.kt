package app.bicast.finma.db.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import app.bicast.finma.EntryActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Entry (
    val id: Int?,
    var userId: Int?,
    val userName: String?,
    val userMob: String?,
    val userPic: ByteArray?,
    var amount: Int,
    var description: String,
    var type: String,
    var dateTime: Long,
    var brs: BankBrs?)
    : Parcelable
{
    constructor(userId: Int,amount: Int,description :String,type: String,dateTime: Long,bankType : BankBrs.Typ) : this(
        null,userId,null,null,null,amount, description,type,dateTime,
        BankBrs(null,"From $type Entry",if(type==EntryActivity.PaymentType.RECEIVED.toString())amount else -1*amount,bankType.toString(),dateTime,0)
    )
    constructor(name :String,mob :String,photo :ByteArray,amount: Int,description :String,type: String,dateTime: Long,bankType : BankBrs.Typ) : this(
        null,null,name,mob,photo,amount, description,type,dateTime,
        BankBrs(null,"From $type Entry",if(type==EntryActivity.PaymentType.RECEIVED.toString())amount else -1*amount,bankType.toString(),dateTime,0)
    )

    fun getUserCopy() : User {
        return User(userName?:"",userMob?:"",userPic)
    }

    private val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.createByteArray(),
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readLong(),
        parcel.readParcelable(BankBrs::class.java.classLoader)
    ) {
    }


    fun getDate() :String{
        return sdf.format(Date(dateTime))
    }

    fun getBitmap() : Bitmap? {
        return userPic?.let { BitmapFactory.decodeByteArray(userPic, 0 , it.size) }
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(userId)
        parcel.writeString(userName)
        parcel.writeString(userMob)
        parcel.writeByteArray(userPic)
        parcel.writeInt(amount)
        parcel.writeString(description)
        parcel.writeString(type)
        parcel.writeLong(dateTime)
        parcel.writeParcelable(brs, flags)
    }

    companion object CREATOR : Parcelable.Creator<Entry> {
        override fun createFromParcel(parcel: Parcel): Entry {
            return Entry(parcel)
        }

        override fun newArray(size: Int): Array<Entry?> {
            return arrayOfNulls(size)
        }
    }

}