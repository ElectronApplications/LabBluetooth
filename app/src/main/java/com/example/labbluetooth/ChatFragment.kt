package com.example.labbluetooth

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.labbluetooth.database.DaoSession
import com.example.labbluetooth.database.Device

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private lateinit var chats: Array<Device>
    private var currentlySelected: Device? = null

    private var messageLoader: MessageLoader? = null
    private lateinit var statsLoader: StatsLoader
    private lateinit var daoSession: DaoSession
    private var sentMessages = 0

    private var deletedMessages = 0
    private lateinit var messagesView: RecyclerView

    var username = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            username = it.getString("username") ?: "Guest"
        }

        statsLoader = StatsLoader(requireActivity())
        daoSession = (requireActivity().application as App).daoSession

        chats = daoSession.deviceDao.loadAll().toTypedArray()

        val userDropDown = view.findViewById<Spinner>(R.id.userDropDown)
        messagesView = view.findViewById(R.id.messagesView)
        val messageEditText = view.findViewById<EditText>(R.id.messageEditText)
        val sendMessageButton = view.findViewById<Button>(R.id.sendMessageButton)
        val tagsTextView = view.findViewById<TextView>(R.id.tagsTextView)

        registerForContextMenu(messagesView)

        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            chats.map { it.name }.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userDropDown.adapter = adapter

        userDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                saveCurrentMessages()
                try {
                    currentlySelected =
                        chats.first { it.name == userDropDown.selectedItem as String }
                    messageLoader = MessageLoader(requireActivity(), currentlySelected!!.name)
                    messagesView.adapter =
                        MessageItemAdapter(messageLoader!!.messages.toTypedArray())
                    tagsTextView.text =
                        "Теги: " + currentlySelected!!.deviceTags.joinToString(", ") { it.name }
                } catch (e: Exception) {
                    e.printStackTrace()
                    currentlySelected = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val layoutManager = LinearLayoutManager(requireActivity())
        messagesView.layoutManager = layoutManager
        messagesView.adapter = MessageItemAdapter(arrayOf())

        sendMessageButton.setOnClickListener {
            messageLoader?.messages?.add(Pair(messageEditText.text.toString(), MessageType.SENT))
            messageLoader?.messages?.add(
                Pair(
                    messageEditText.text.toString().reversed(),
                    MessageType.RECEIVED
                )
            )

            messageEditText.text.clear()

            messagesView.adapter =
                MessageItemAdapter(messageLoader?.messages?.toTypedArray() ?: arrayOf())
            sentMessages += 2
            if (currentlySelected?.messagesAmount != null) {
                currentlySelected!!.messagesAmount += 2
            }
        }
    }

    fun saveCurrentMessages() {
        if (currentlySelected != null) {
            daoSession.deviceDao.save(currentlySelected)
            messageLoader?.save()
            val deviceId =
                statsLoader.data.indexOfFirst { it.deviceName == currentlySelected!!.name }
            if (deviceId != -1) {
                statsLoader.data[deviceId] = statsLoader.data[deviceId].copy(
                    totalMessages = statsLoader.data[deviceId].totalMessages + sentMessages,
                    deletedMessages = statsLoader.data[deviceId].deletedMessages + deletedMessages
                )
            } else {
                statsLoader.data.add(
                    StatsLoader.Item(
                        deviceName = currentlySelected!!.name,
                        totalMessages = sentMessages,
                        deletedMessages = deletedMessages
                    )
                )
            }
        }
        sentMessages = 0
        deletedMessages = 0

        statsLoader.save()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.message_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_message -> {
                messageLoader?.messages?.removeAt((messagesView.adapter as MessageItemAdapter).position)
                messagesView.adapter =
                    MessageItemAdapter(messageLoader?.messages?.toTypedArray() ?: arrayOf())
                deletedMessages += 1
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }
}