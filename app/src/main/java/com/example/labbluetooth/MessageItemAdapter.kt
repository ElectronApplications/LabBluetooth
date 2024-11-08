package com.example.labbluetooth

import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

enum class MessageType {
    SENT,
    RECEIVED
}

class MessageItemAdapter(private val dataset: Array<Pair<String, MessageType>>) :
    RecyclerView.Adapter<MessageItemAdapter.ViewHolder>() {
    var position: Int = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val messageTextView: TextView = view.findViewById(R.id.messageTextView)

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position].second) {
            MessageType.SENT -> MessageType.SENT.ordinal
            MessageType.RECEIVED -> MessageType.RECEIVED.ordinal
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = if (viewType == MessageType.SENT.ordinal) {
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_message_sent, viewGroup, false)
        } else {
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_message_received, viewGroup, false)
        }

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.messageTextView.text = dataset[position].first
        viewHolder.itemView.setOnLongClickListener {
            this.position = viewHolder.adapterPosition
            false
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.itemView.setOnLongClickListener(null)
        super.onViewRecycled(holder)
    }

    override fun getItemCount() = dataset.size
}