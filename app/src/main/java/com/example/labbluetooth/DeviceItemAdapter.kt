package com.example.labbluetooth

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DeviceItemAdapter(private val dataset: Array<Pair<String, Int>>) : RecyclerView.Adapter<DeviceItemAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageAmountTextView: TextView = view.findViewById(R.id.messageAmountTextView)
        val deviceNameTextView: TextView = view.findViewById(R.id.deviceNameTextView)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_device, viewGroup, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.deviceNameTextView.text = dataset[position].first
        viewHolder.messageAmountTextView.text = dataset[position].second.toString()
    }

    override fun getItemCount() = dataset.size
}