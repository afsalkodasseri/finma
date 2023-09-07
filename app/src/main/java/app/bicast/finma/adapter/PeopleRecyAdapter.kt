package app.bicast.finma.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.IndividualDebtActivity
import app.bicast.finma.R
import app.bicast.finma.db.models.User

class PeopleRecyAdapter (val items :List<User>) : RecyclerView.Adapter<PeopleRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_peoples,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvName.setText(items[position].name)
        holder.tvPhone.setText(items[position].phone.ifEmpty { "(no number)" })
        holder.ivUser.setImageBitmap(items[position].getBitmap())
        val tempBal = items[position].balance
        if(tempBal<0){
            holder.tvBalance.setTextColor(holder.itemView.context.getColor(R.color.red))
        }else{
            holder.tvBalance.setTextColor(holder.itemView.context.getColor(R.color.green))
        }
        holder.tvBalance.setText("₹"+ Math.abs(tempBal).toString())

        holder.itemView.setOnClickListener {
            holder.itemView.context.startActivity(
                Intent(holder.itemView.context, IndividualDebtActivity::class.java)
                    .putExtra("user_id",items[position].id)
                    .putExtra("user_name",items[position].name)
            )
        }
    }
    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val ivUser : ImageView = itemView.findViewById(R.id.iv_user)
        val tvName :TextView = itemView.findViewById(R.id.tv_name)
        val tvPhone :TextView = itemView.findViewById(R.id.tv_mobile)
        val tvBalance :TextView = itemView.findViewById(R.id.tv_amount)
    }
}