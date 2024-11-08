package com.example.labbluetooth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
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
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {
    private var name = ""
    private var devices: MutableList<Pair<String, Int>> = mutableListOf()
    private var messages: MutableList<Pair<String, MessageType>> = mutableListOf()
    private var currentlySelected: String = ""

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

        val userDropDown = findViewById<Spinner>(R.id.userDropDown)
        messagesView = findViewById(R.id.messagesView)
        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val sendMessageButton = findViewById<Button>(R.id.sendMessageButton)
        val mainLayoutButton = findViewById<Button>(R.id.mainLayoutButton)

        registerForContextMenu(messagesView)

        name = intent.getStringExtra("username") ?: "Guest"
        devices = (intent.getSerializableExtra("devices") as? Array<Pair<String, Int>> ?: arrayOf()).toMutableList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, devices.map { it.first }.toTypedArray())
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
                currentlySelected = userDropDown.selectedItem as String
                val index = devices.indexOfFirst { it.first == currentlySelected }
                messages = mutableListOf(Pair("... (${devices[index].second} сообщений)", MessageType.RECEIVED))
                messagesView.adapter = MessageItemAdapter(messages.toTypedArray())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val layoutManager = LinearLayoutManager(this)
        messagesView.layoutManager = layoutManager
        messagesView.adapter = MessageItemAdapter(messages.toTypedArray())

        sendMessageButton.setOnClickListener {
            messages.add(Pair(messageEditText.text.toString(), MessageType.SENT))
            messages.add(Pair(messageEditText.text.toString().reversed(), MessageType.RECEIVED))

            messageEditText.text.clear()

            messagesView.adapter = MessageItemAdapter(messages.toTypedArray())

            val index = devices.indexOfFirst { it.first == currentlySelected }
            devices[index] = Pair(currentlySelected, devices[index].second + 2)
        }

        mainLayoutButton.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java)
            mainIntent.putExtra("username", name)
            mainIntent.putExtra("devices", devices.toTypedArray())
            finish()
            startActivity(mainIntent)
        }
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
                messages.removeAt((messagesView.adapter as MessageItemAdapter).position)
                messagesView.adapter = MessageItemAdapter(messages.toTypedArray())
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}