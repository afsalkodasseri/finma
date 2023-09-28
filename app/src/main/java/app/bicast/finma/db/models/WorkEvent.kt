package app.bicast.finma.db.models

import android.os.Parcel
import android.os.Parcelable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkEvent (
    var id: Int?,
    var type: String,
    var description: String,
    var date: Long)
    : Parcelable
{
    private val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    enum class Typ{SICK_LEAVE,CASUAL_LEAVE,HALF_LEAVE,HOLIDAY,WFH}

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readLong()
    )

    fun getDate() :String{
        return sdf.format(Date(date))
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(type)
        parcel.writeString(description)
        parcel.writeLong(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WorkEvent> {
        override fun createFromParcel(parcel: Parcel): WorkEvent {
            return WorkEvent(parcel)
        }

        override fun newArray(size: Int): Array<WorkEvent?> {
            return arrayOfNulls(size)
        }
        val EVENT_TYPES =
            listOf("Sick Leave", "Casual Leave", "Half Leave", "Holiday", "Wfh")
    }
}