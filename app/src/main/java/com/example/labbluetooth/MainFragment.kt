package com.example.labbluetooth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val resultNameTextView = view.findViewById<TextView>(R.id.resultNameTextView)
        val resultsView = view.findViewById<RecyclerView>(R.id.resultsView)
        val resultsRefresh = view.findViewById<SwipeRefreshLayout>(R.id.resultsRefresh)
        val statsTextView = view.findViewById<TextView>(R.id.statsTextView)
        val exportStatsButton = view.findViewById<Button>(R.id.exportStatsButton)

        nameEditText.setText(viewModel.username)

        viewModel.uiState.observe(requireActivity()) { uiState ->
            resultNameTextView.text = uiState.username
            resultsView.adapter = DeviceItemAdapter(uiState.chats.toTypedArray())

            val stats = uiState.stats
            if (stats.isNotEmpty()) {
                statsTextView.text = ""
                statsTextView.append("\nВсего ${stats.sumOf { it.totalMessages }} сообщений")
                val maxTotal = stats.maxBy { it.totalMessages }
                statsTextView.append("\nБольше всего сообщений - ${maxTotal.deviceName} (${maxTotal.totalMessages})")
                val maxDeleted = stats.maxBy { it.deletedMessages }
                statsTextView.append("\nБольше всего удалено сообщений - ${maxDeleted.deviceName} (${maxDeleted.deletedMessages})")
            }
        }

        exportStatsButton.setOnClickListener {
            val uri = viewModel.exportStats()
            Toast.makeText(requireActivity(), "Экспортировано в $uri", LENGTH_LONG).show()
        }

        val layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        resultsView.layoutManager = layoutManager

        resultsRefresh.setOnRefreshListener {
            viewModel.updateDevices()
            resultsRefresh.isRefreshing = false
        }

        nameEditText.setOnKeyListener { _, _, _ ->
            viewModel.changeUsername(nameEditText.text.toString())
            true
        }
    }
}