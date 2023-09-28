package app.bicast.finma.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.bicast.finma.NewExpenseActivity
import app.bicast.finma.NewWorkEventActivity
import app.bicast.finma.R
import app.bicast.finma.db.models.Expense
import app.bicast.finma.db.models.WorkEvent

class EventRecyAdapter (val items :List<WorkEvent>) : RecyclerView.Adapter<EventRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_event,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvDate.setText(items[position].getDate())
        holder.tvDescription.setText(items[position].description)
        holder.tvDescription.visibility = if (items[position].description.isEmpty()) View.GONE else View.VISIBLE
        holder.tvType.setText(WorkEvent.EVENT_TYPES.get(WorkEvent.Typ.valueOf(items[position].type).ordinal))

        holder.itemView.setOnClickListener {
            holder.itemView.context.startActivity(
                Intent(holder.itemView.context, NewWorkEventActivity::class.java)
                    .putExtra("event",items[position])
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val tvDate :TextView = itemView.findViewById(R.id.tv_date)
        val tvType :TextView = itemView.findViewById(R.id.tv_type)
        val tvDescription :TextView = itemView.findViewById(R.id.tv_description)
    }
}