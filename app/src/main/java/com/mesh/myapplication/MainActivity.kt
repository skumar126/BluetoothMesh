package com.mesh.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.mesh.myapplication.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 1. Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 3. Setup AppBar / Toolbar
        setSupportActionBar(binding.toolbar)

        // 4. Apply window insets to toolbar and content
        applyEdgeToEdge()
    }

    private fun applyEdgeToEdge(){
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { view, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.setPadding(0, sysBars.top, 0, 0)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.content) { view, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, 0, 0, navBars.bottom)
            insets
        }
    }
}

