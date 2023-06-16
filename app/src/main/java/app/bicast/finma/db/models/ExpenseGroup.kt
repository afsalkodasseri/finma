package app.bicast.finma.db.models

class ExpenseGroup (
    val id: Int?,
    var name: String?,
    var color: String?,
    val icon: ByteArray?,
    var amount: Int?){

    override fun toString(): String {
        return name?:""
    }
}