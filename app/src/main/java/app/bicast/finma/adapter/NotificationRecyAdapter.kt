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
import app.bicast.finma.db.models.AppNotificationItem
import app.bicast.finma.db.models.Entry

class NotificationRecyAdapter (val items :List<AppNotificationItem>) : RecyclerView.Adapter<NotificationRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_notification,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvName.setText(items[position].title)
        holder.tvDesc.setText(items[position].desc)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val tvName :TextView = itemView.findViewById(R.id.tv_title)
        val tvDesc :TextView = itemView.findViewById(R.id.tv_desc)
    }
}