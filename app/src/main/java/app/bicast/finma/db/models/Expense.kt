package app.bicast.finma.db.models

import android.os.Parcel
import android.os.Parcelable
import app.bicast.finma.NewExpenseActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Expense (
    val id: Int?,
    var name: String,
    var amount: Int,
    var description: String,
    var type: String,
    var dateTime: Long,
    var brs: BankBrs?,
    var group_id :String?)
    : Parcelable
{
    private val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    constructor(name: String,amount: Int,description: String,type: String,dateTime: Long,brsType : BankBrs.Typ,group_id: String?):
            this(null,name,amount, description, type, dateTime, BankBrs(null,"From $type Expense",if(type== NewExpenseActivity.ExpenseType.INCOME.toString())amount else -1*amount, brsType.toString(), dateTime,0),group_id)

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()?:"",
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readLong(),
        parcel.readParcelable(BankBrs::class.java.classLoader),
        parcel.readString()?:"",
    )

    fun getDate() :String{
        return sdf.format(Date(dateTime))
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeInt(amount)
        parcel.writeString(description)
        parcel.writeString(type)
        parcel.writeLong(dateTime)
        parcel.writeParcelable(brs, flags)
        parcel.writeString(group_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }
}