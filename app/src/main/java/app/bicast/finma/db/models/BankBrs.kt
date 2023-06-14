package app.bicast.finma.db.models

import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BankBrs (
    var id: Int?,
    var name: String,
    var amount: Int,
    var type: String,
    var dateTime: Long,
    var monthlyIncome: Int)
    : Parcelable
{
    private val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    enum class Typ{CASH,BANK}

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readLong(),
        parcel.readInt()
    )

    fun getDate() :String{
        return sdf.format(Date(dateTime))
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeInt(amount)
        parcel.writeString(type)
        parcel.writeLong(dateTime)
        parcel.writeInt(monthlyIncome)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BankBrs> {
        override fun createFromParcel(parcel: Parcel): BankBrs {
            return BankBrs(parcel)
        }

        override fun newArray(size: Int): Array<BankBrs?> {
            return arrayOfNulls(size)
        }
    }
}