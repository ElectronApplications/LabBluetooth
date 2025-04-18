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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatFragment : Fragment(R.layout.fragment_chat) {
    private lateinit var viewModel: MainViewModel

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val userDropDown = view.findViewById<Spinner>(R.id.userDropDown)
        val messagesView = view.findViewById<RecyclerView>(R.id.messagesView)
        val messageEditText = view.findViewById<EditText>(R.id.messageEditText)
        val sendMessageButton = view.findViewById<Button>(R.id.sendMessageButton)
        val tagsTextView = view.findViewById<TextView>(R.id.tagsTextView)

        var previousChats = emptyList<Long>()
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            val currentChats = uiState.chats.map { it.id }
            if (currentChats != previousChats) {
                val adapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    uiState.chats.map { it.name }.toTypedArray()
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                userDropDown.adapter = adapter

                previousChats = currentChats
            }

            messagesView.adapter =
                MessageItemAdapter(uiState.messages?.toTypedArray() ?: emptyArray())

            if (uiState.tags != null) {
                tagsTextView.text = "Теги: " + uiState.tags.joinToString(", ") { it.name }
            } else {
                tagsTextView.text = ""
            }
        }

        registerForContextMenu(messagesView)

        userDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.switchChat(userDropDown.selectedItem as String)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val layoutManager = LinearLayoutManager(requireActivity())
        messagesView.layoutManager = layoutManager

        sendMessageButton.setOnClickListener {
            viewModel.addMessage(messageEditText.text.toString())
            messageEditText.text.clear()
        }
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
                viewModel.deleteMessage(item.order)
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }
}