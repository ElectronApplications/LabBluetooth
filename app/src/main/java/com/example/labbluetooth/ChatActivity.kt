package com.example.labbluetooth

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {
    private var name = ""
    private lateinit var chats: Array<Pair<String, Int?>>

    private var currentlySelected: String = ""
    private var messageLoader: MessageLoader? = null
    private lateinit var statsLoader: StatsLoader

    private var sentMessages = 0
    private var deletedMessages = 0

    private lateinit var messagesView: RecyclerView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        statsLoader = StatsLoader(this)

        val userDropDown = findViewById<Spinner>(R.id.userDropDown)
        messagesView = findViewById(R.id.messagesView)
        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val sendMessageButton = findViewById<Button>(R.id.sendMessageButton)
        val mainLayoutButton = findViewById<Button>(R.id.mainLayoutButton)

        registerForContextMenu(messagesView)

        name = intent.getStringExtra("username") ?: "Guest"
        chats = intent.getSerializableExtra("chats") as? Array<Pair<String, Int?>> ?: arrayOf()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, chats.map { it.first }.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userDropDown.adapter = adapter

        userDropDown.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            @SuppressLint("SetTextI18n")
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                saveCurrentMesages()

                currentlySelected = userDropDown.selectedItem as String
                messageLoader = MessageLoader(this@ChatActivity, currentlySelected)
                messagesView.adapter = MessageItemAdapter(messageLoader!!.messages.toTypedArray())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val layoutManager = LinearLayoutManager(this)
        messagesView.layoutManager = layoutManager
        messagesView.adapter = MessageItemAdapter(arrayOf())

        sendMessageButton.setOnClickListener {
            messageLoader?.messages?.add(Pair(messageEditText.text.toString(), MessageType.SENT))
            messageLoader?.messages?.add(Pair(messageEditText.text.toString().reversed(), MessageType.RECEIVED))

            messageEditText.text.clear()

            messagesView.adapter = MessageItemAdapter(messageLoader?.messages?.toTypedArray() ?: arrayOf())
            sentMessages += 2
        }

        mainLayoutButton.setOnClickListener {
            saveCurrentMesages()
            statsLoader.save()
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.putExtra("username", name)
            finish()
            startActivity(mainIntent)
        }
    }

    fun saveCurrentMesages() {
        if (currentlySelected != "") {
            messageLoader?.save()
            val deviceId = statsLoader.data.indexOfFirst { it.deviceName == currentlySelected }
            if (deviceId != -1) {
                statsLoader.data[deviceId] = statsLoader.data[deviceId].copy(
                    totalMessages = statsLoader.data[deviceId].totalMessages + sentMessages,
                    deletedMessages = statsLoader.data[deviceId].deletedMessages + deletedMessages
                )
            } else {
                statsLoader.data.add(StatsLoader.Item(
                    deviceName = currentlySelected,
                    totalMessages = sentMessages,
                    deletedMessages = deletedMessages
                ))
            }
        }
        sentMessages = 0
        deletedMessages = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        saveCurrentMesages()
        statsLoader.save()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.message_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_message -> {
                messageLoader?.messages?.removeAt((messagesView.adapter as MessageItemAdapter).position)
                messagesView.adapter = MessageItemAdapter(messageLoader?.messages?.toTypedArray() ?: arrayOf())
                deletedMessages += 1
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}