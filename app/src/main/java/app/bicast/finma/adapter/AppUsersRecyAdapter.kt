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
import app.bicast.finma.db.models.AppUserItem
import app.bicast.finma.db.models.Entry

class AppUsersRecyAdapter (val items :List<AppUserItem>) : RecyclerView.Adapter<AppUsersRecyAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_user,parent,false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.tvName.setText(items[position].name)
        holder.tvId.setText(items[position].id)
        holder.tvTimeZone.setText(items[position].timeZone)
        holder.tvDate.setText(items[position].regDate)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MyHolder (itemView :View) :RecyclerView.ViewHolder(itemView){
        val tvName :TextView = itemView.findViewById(R.id.tv_name)
        val tvId :TextView = itemView.findViewById(R.id.tv_id)
        val tvTimeZone :TextView = itemView.findViewById(R.id.tv_timezone)
        val tvDate :TextView = itemView.findViewById(R.id.tv_date)
    }
}