package app.bicast.finma.db.models

class ExpenseGroup (
    val id: Int?,
    val name: String,
    val icon: Int?,
    val color: Int?){

    override fun toString(): String {
        return name
    }
}