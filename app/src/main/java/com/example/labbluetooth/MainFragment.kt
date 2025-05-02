package com.example.labbluetooth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.labbluetooth.databinding.FragmentMainBinding

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        val binding: FragmentMainBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val resultsView = view.findViewById<RecyclerView>(R.id.resultsView)
        val resultsRefresh = view.findViewById<SwipeRefreshLayout>(R.id.resultsRefresh)
        val exportStatsButton = view.findViewById<Button>(R.id.exportStatsButton)

        nameEditText.setText(viewModel.username)

        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            resultsView.adapter = DeviceItemAdapter(uiState.chats.toTypedArray())
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