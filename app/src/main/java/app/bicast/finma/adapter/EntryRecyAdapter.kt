package app.bicast.finma.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.EntryActivity
import app.bicast.finma.R
import app.bicast.finma.db.models.Entry

class EntryRecyAdapter (val items :List<Entry>) : RecyclerView.Adapter<EntryRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_entry,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.ivUser.setImageBitmap(items[position].getBitmap())
        holder.tvName.setText(items[position].userName)
        holder.tvMobile.setText(items[position].userMob?:"no number")
        holder.tvAmount.setTextColor(holder.itemView.context.getColor(if(items[position].type == EntryActivity.PaymentType.RECEIVED.toString()) R.color.green else R.color.red))
        holder.tvAmount.setText("₹"+ items[position].amount)
        holder.tvDate.setText(items[position].getDate())
        holder.tvDescription.setText(if (items[position].description.isEmpty()) "(none)" else items[position].description)
        holder.tvType.setText(items[position].type.toLowerCase().capitalize())

        holder.itemView.setOnClickListener {
            holder.itemView.context.startActivity(
                Intent(holder.itemView.context, EntryActivity::class.java)
                    .putExtra("entry_id",items[position])
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val ivUser :ImageView = itemView.findViewById(R.id.iv_user)
        val tvName :TextView = itemView.findViewById(R.id.tv_name)
        val tvMobile :TextView = itemView.findViewById(R.id.tv_mobile)
        val tvAmount :TextView = itemView.findViewById(R.id.tv_amount)
        val tvDate :TextView = itemView.findViewById(R.id.tv_date)
        val tvDescription :TextView = itemView.findViewById(R.id.tv_description)
        val tvType :TextView = itemView.findViewById(R.id.tv_type)
    }
}