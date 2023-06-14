package app.bicast.finma.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.NewBankBrsActivity
import app.bicast.finma.NewExpenseActivity
import app.bicast.finma.R
import app.bicast.finma.db.models.BankBrs

class BrsRecyAdapter (var items :List<BankBrs>) : RecyclerView.Adapter<BrsRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_brs,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvName.setText(items[position].name)
        holder.tvDate.setText(items[position].getDate())
        holder.tvType.setText(items[position].type.toLowerCase().capitalize())
        holder.tvAmount.setTextColor(holder.itemView.context.getColor(if(items[position].amount > 0) R.color.green else R.color.red))
        holder.tvAmount.setText("₹"+ Math.abs(items[position].amount))

        holder.itemView.setOnClickListener {
            holder.itemView.context.startActivity(
                Intent(holder.itemView.context, NewBankBrsActivity::class.java)
                    .putExtra("brs",items[position])
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val tvDate :TextView = itemView.findViewById(R.id.tv_date)
        val tvName :TextView = itemView.findViewById(R.id.tv_name)
        val tvType :TextView = itemView.findViewById(R.id.tv_type)
        val tvAmount :TextView = itemView.findViewById(R.id.tv_amount)
    }
}