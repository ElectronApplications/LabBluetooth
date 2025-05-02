package com.example.labbluetooth

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val mainLayoutButton = findViewById<Button>(R.id.mainLayoutButton)
        val chatLayoutButton = findViewById<Button>(R.id.chatLayoutButton)

        mainLayoutButton.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById((R.id.fragmentView))
            if (fragment is ChatFragment) {
                viewModel.saveCurrentMessages()
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<MainFragment>(R.id.fragmentView)
                }
            }
        }

        chatLayoutButton.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById((R.id.fragmentView))
            if (fragment is MainFragment) {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace<ChatFragment>(R.id.fragmentView)
                }
            }
        }
    }

    override fun onDestroy() {
        val fragment = supportFragmentManager.findFragmentById((R.id.fragmentView))
        if (fragment is ChatFragment) {
            viewModel.saveCurrentMessages()
        }

        super.onDestroy()
    }
}