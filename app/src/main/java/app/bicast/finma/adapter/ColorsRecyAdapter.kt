package app.bicast.finma.adapter

import android.content.Intent
import android.graphics.Color
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

class ColorsRecyAdapter (val items :List<String>,val listener :ColorClick?, var selectedPos :Int = 0) : RecyclerView.Adapter<ColorsRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_color,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.cardColor.setCardBackgroundColor(Color.parseColor(items.get(position)))
        holder.ivTick.visibility = if (selectedPos==position) View.VISIBLE else View.GONE
        holder.cardColor.setOnClickListener {
            val oldPos = selectedPos
            selectedPos = position
            if(selectedPos!=oldPos) {
                notifyItemChanged(oldPos)
                notifyItemChanged(selectedPos)
                listener?.Click(position, items.get(position))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val cardColor :CardView = itemView.findViewById(R.id.card_color)
        val ivTick :ImageView = itemView.findViewById(R.id.iv_tick)
    }

    interface ColorClick{
        fun Click(pos :Int,color :String)
    }
}