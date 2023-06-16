package app.bicast.finma.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import app.bicast.finma.R
import app.bicast.finma.db.models.ExpenseGroup

class ColorsSpinnerAdapter : BaseAdapter {
    lateinit var inflater: LayoutInflater
    lateinit var items :List<ExpenseGroup>
    constructor(context :Context,items :List<ExpenseGroup>){
        this.items = items
        inflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = inflater.inflate(R.layout.item_expense_group,parent,false)
        view.findViewById<TextView>(R.id.tv_name).setText(items.get(position).name)
        return view
    }
}