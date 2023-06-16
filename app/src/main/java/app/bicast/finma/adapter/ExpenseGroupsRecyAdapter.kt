package app.bicast.finma.adapter

import android.content.Intent
import android.graphics.Color
import android.provider.CalendarContract.Colors
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.EntryActivity
import app.bicast.finma.NewExpenseActivity
import app.bicast.finma.R
import app.bicast.finma.db.models.Expense
import app.bicast.finma.db.models.ExpenseGroup

class ExpenseGroupsRecyAdapter (val items :List<ExpenseGroup>,val filteredIds :List<Int?>,val listener :GroupClick?) : RecyclerView.Adapter<ExpenseGroupsRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_expense_group,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvName.setText(items[position].name)
        holder.cardColor.setCardBackgroundColor(Color.parseColor(items[position].color))
        holder.tvAmount.setText("₹"+items[position].amount.toString())
        holder.ivFilter.visibility = if (filteredIds.contains(items[position].id)) View.VISIBLE else View.GONE
        holder.itemView.setOnClickListener {
            listener?.onClick(items[position])
            notifyItemChanged(position)
        }
        holder.itemView.setOnLongClickListener {
            listener?.onLongClick(items[position])
            true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val tvName :TextView = itemView.findViewById(R.id.tv_name)
        val tvAmount :TextView = itemView.findViewById(R.id.tv_amount)
        val cardColor :CardView = itemView.findViewById(R.id.card_color)
        val ivFilter :ImageView = itemView.findViewById(R.id.iv_filter)
    }

    interface GroupClick{
        fun onClick(item :ExpenseGroup)
        fun onLongClick(item :ExpenseGroup)
    }
}