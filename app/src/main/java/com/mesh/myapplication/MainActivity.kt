package com.mesh.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.mesh.myapplication.adapter.MeshNetworkAdapter
import com.mesh.myapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var networkAdapter: MeshNetworkAdapter

    private val meshRepository by lazy {
        (application as BluetoothMeshApp).meshRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setSupportActionBar(binding.toolbar)
        applyEdgeToEdge()

        networkAdapter = MeshNetworkAdapter { network ->
            startActivity(
                Intent(this, MeshScanActivity::class.java).putExtra(
                    MeshScanActivity.EXTRA_NETWORK_NAME,
                    network.name
                )
            )
        }
        binding.networkList.layoutManager = LinearLayoutManager(this)
        binding.networkList.adapter = networkAdapter
        binding.addMeshButton.setOnClickListener { showCreateMeshDialog() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                meshRepository.observeMeshes().collect { networks ->
                    networkAdapter.submitList(networks)
                    binding.emptyView.isVisible = networks.isEmpty()
                    binding.networkList.isVisible = networks.isNotEmpty()
                }
            }
        }
    }

    private fun showCreateMeshDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.mesh_name_hint)
            setSingleLine()
        }
        val padding = (20 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this).apply {
            setPadding(padding, padding / 2, padding, 0)
            addView(input)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.create_mesh)
            .setView(container)
            .setPositiveButton(R.string.create) { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(
                        this,
                        getString(R.string.mesh_name_required),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    lifecycleScope.launch {
                        meshRepository.createMesh(name)
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun applyEdgeToEdge() {
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
