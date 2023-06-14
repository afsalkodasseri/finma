package app.bicast.finma.db.models

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class User (
    val id: Int?,
    val name: String,
    val phone: String,
    val photo: ByteArray? ){
    constructor(name :String,phone :String) : this(null,name,phone, null)
    constructor(name :String,phone :String,photo: ByteArray?) : this(null,name,phone, photo)

    var balance: Int = 0

    fun setBalanceUser(user : User, balance :Int) : User {
        user.balance = balance
        return user
    }

    override fun toString(): String {
        return name
    }

    fun getBitmap() : Bitmap? {
        return photo?.let { BitmapFactory.decodeByteArray(photo, 0 , it.size) }
    }
}