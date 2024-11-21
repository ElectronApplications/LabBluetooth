package com.example.labbluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
//    private lateinit var bluetoothAdapter: BluetoothAdapter
//    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var chats: Array<Pair<String, Int?>>
    private var name = "Guest"

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        name = intent.getStringExtra("username") ?: "Guest"
        chats = activeChats(this)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val resultNameTextView = findViewById<TextView>(R.id.resultNameTextView)
        val resultsView = findViewById<RecyclerView>(R.id.resultsView)
        val chatLayoutButton = findViewById<Button>(R.id.chatLayoutButton)
        val resultsRefresh = findViewById<SwipeRefreshLayout>(R.id.resultsRefresh)
        val statsTextView = findViewById<TextView>(R.id.statsTextView)
        val exportStatsButton = findViewById<Button>(R.id.exportStatsButton)

        val statsLoader = StatsLoader(this)
        val stats = statsLoader.data
        if (stats.isNotEmpty()) {
            statsTextView.append("\nВсего ${stats.sumOf { it.totalMessages }} сообщений")

            val maxTotal = stats.maxBy { it.totalMessages }
            statsTextView.append("\nБольше всего сообщений - ${maxTotal.deviceName} (${maxTotal.totalMessages})")

            val maxDeleted = stats.maxBy { it.deletedMessages }
            statsTextView.append("\nБольше всего удалено сообщений - ${maxDeleted.deviceName} (${maxDeleted.deletedMessages})")
        }

        exportStatsButton.setOnClickListener {
            val uri = saveImage(this@MainActivity, generateStatsImage(statsLoader))
            Toast.makeText(this@MainActivity, "Экспортировано в $uri", LENGTH_LONG).show()
        }

        nameEditText.setText(name)
        resultNameTextView.text = name

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        resultsView.layoutManager = layoutManager
        resultsView.adapter = DeviceItemAdapter(chats)

        resultsRefresh.setOnRefreshListener {
            val name = Random.nextInt(0x1000000).toString(16)
            MessageLoader(this, name).save()
            if (Random.nextFloat() > 0.75 && chats.isNotEmpty()) {
                MessageLoader(this, chats.last().first).delete()
            }
            chats = activeChats(this@MainActivity)

            resultsView.adapter = DeviceItemAdapter(chats)
            resultsRefresh.isRefreshing = false
        }

        nameEditText.setOnKeyListener { _, _, _ ->
            name = nameEditText.text.toString()
            resultNameTextView.text = name
            true
        }

        chatLayoutButton.setOnClickListener {
            val chatIntent = Intent(this, ChatActivity::class.java)
            chatIntent.putExtra("username", name)
            chatIntent.putExtra("chats", chats)
            finish()
            startActivity(chatIntent)
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION), 42)
//        } else {
//            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_FINE_LOCATION), 42)
//        }

//        bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
//        val success = bluetoothAdapter.startDiscovery()
//        Log.d("DEBUG", "$success")
//
//        broadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                Log.d("DEBUG", "received action: ${intent?.action}")
//                if (intent?.action == BluetoothDevice.ACTION_FOUND) {
//                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//
//                    Log.d("DEBUG", "found result: ${device?.name}")
//                    if (device != null) {
//                        devices.putIfAbsent(device.name, 0)
//                        resultsTextView.text = devices.map { "${it.key} (${it.value} сообщений)" }.fold("") { acc, line -> "${acc}\n${line}" }
//                    }
//                }
//            }
//        }
//
//        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
//        registerReceiver(broadcastReceiver, filter)
    }
}