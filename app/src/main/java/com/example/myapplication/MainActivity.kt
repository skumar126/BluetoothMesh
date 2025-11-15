package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var root: CoordinatorLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var contentContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_MyApplication)
        setContentView(binding.root)
        root = binding.rootMain
        toolbar = binding.toolbar
        contentContainer = binding.contentContainer

        if (Build.VERSION.SDK_INT >= 35) {
            enableEdgeToEdge()
        } else {
            setupToolbarAsActionBar()
            applyInsetsForLegacyToolbar()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun setupToolbarAsActionBar() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setSupportActionBar(toolbar)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)

        toolbar.elevation =
            resources.getDimension(com.google.android.material.R.dimen.material_emphasis_medium)

    }

    private fun applyInsetsForLegacyToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val sys =
                insets.getInsets(
                    WindowInsetsCompat.Type.navigationBars()
                            or WindowInsetsCompat.Type.displayCutout()
                )
            toolbar.updatePadding(top = sys.top)
            val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            contentContainer.updatePadding(bottom = sys.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}