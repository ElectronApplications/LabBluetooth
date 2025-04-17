package com.example.labbluetooth

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.labbluetooth.database.DaoSession
import com.example.labbluetooth.database.Device
import com.example.labbluetooth.database.DeviceTag
import kotlin.random.Random

val defaultTags =
    arrayOf("Лучший друг", "Семья", "Друг", "Незнакомец", "sugar daddy", "sugar mommy")

class MainFragment : Fragment(R.layout.fragment_main) {
    private lateinit var chats: Array<Device>
    private lateinit var daoSession: DaoSession
    var username = "Guest"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            username = it.getString("username") ?: "Guest"
        }

        daoSession = (requireActivity().application as App).daoSession

        chats = daoSession.deviceDao.loadAll().toTypedArray()

        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val resultNameTextView = view.findViewById<TextView>(R.id.resultNameTextView)
        val resultsView = view.findViewById<RecyclerView>(R.id.resultsView)
        val resultsRefresh = view.findViewById<SwipeRefreshLayout>(R.id.resultsRefresh)
        val statsTextView = view.findViewById<TextView>(R.id.statsTextView)
        val exportStatsButton = view.findViewById<Button>(R.id.exportStatsButton)

        val statsLoader = StatsLoader(requireActivity())
        val stats = statsLoader.data
        if (stats.isNotEmpty()) {
            statsTextView.append("\nВсего ${stats.sumOf { it.totalMessages }} сообщений")

            val maxTotal = stats.maxBy { it.totalMessages }
            statsTextView.append("\nБольше всего сообщений - ${maxTotal.deviceName} (${maxTotal.totalMessages})")

            val maxDeleted = stats.maxBy { it.deletedMessages }
            statsTextView.append("\nБольше всего удалено сообщений - ${maxDeleted.deviceName} (${maxDeleted.deletedMessages})")
        }

        exportStatsButton.setOnClickListener {
            val uri = saveImage(requireActivity(), generateStatsImage(statsLoader))
            Toast.makeText(requireActivity(), "Экспортировано в $uri", LENGTH_LONG).show()
        }

        nameEditText.setText(username)
        resultNameTextView.text = username

        val layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        resultsView.layoutManager = layoutManager
        resultsView.adapter = DeviceItemAdapter(chats)

        resultsRefresh.setOnRefreshListener {
            val name = Random.nextInt(0x1000000).toString(16)

            val device = Device(null, name, 0)
            daoSession.deviceDao.save(device)

            defaultTags.toList().shuffled().take(Random.nextInt(defaultTags.size))
                .forEach { tagName ->
                    val tag = DeviceTag(null, tagName, device.id)
                    daoSession.deviceTagDao.save(tag)
                }

            MessageLoader(requireActivity(), name).save()

            if (Random.nextFloat() > 0.75 && chats.isNotEmpty()) {
                val lastDevice = chats.last()
                MessageLoader(requireActivity(), lastDevice.name).delete()
                daoSession.deviceDao.delete(lastDevice)
            }
            chats = daoSession.deviceDao.loadAll().toTypedArray()

            resultsView.adapter = DeviceItemAdapter(chats)
            resultsRefresh.isRefreshing = false
        }

        nameEditText.setOnKeyListener { _, _, _ ->
            username = nameEditText.text.toString()
            resultNameTextView.text = username
            true
        }
    }
}