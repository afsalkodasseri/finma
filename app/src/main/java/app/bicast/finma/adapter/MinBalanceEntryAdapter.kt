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
import app.bicast.finma.db.models.EntryRowItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MinBalanceEntryAdapter (var items :List<EntryRowItem>) : RecyclerView.Adapter<MinBalanceEntryAdapter.MyHolder>() {

    val monthDate = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_min_balance_entry,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvDate.setText(monthDate.format(Date(items[position].date)))
        holder.tvType.setText(items[position].type.lowercase(Locale.ROOT)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
        holder.tvAmount.setText("₹"+ Math.abs(items[position].amount))
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val tvDate :TextView = itemView.findViewById(R.id.tv_date)
        val tvType :TextView = itemView.findViewById(R.id.tv_type)
        val tvAmount :TextView = itemView.findViewById(R.id.tv_amount)
    }
}