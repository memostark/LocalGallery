package com.guillermonegrete.gallery.folders

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val navigationView = binding.mainNavView

        when(navigationView) {
            is NavigationBarView -> navigationView.setupWithNavController(navController)
            is NavigationView -> NavigationUI.setupWithNavController(navigationView, navController)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            navigationView.isGone = when (destination.id) {
                R.id.file_details_dest, R.id.addTagFragment -> true
                // Keep the bar hidden when navigating to the files list from details
                R.id.files_fragment_dest -> navController.previousBackStackEntry?.destination?.id == R.id.file_details_dest
                else -> false
            }

            if(destination.id == R.id.files_fragment_dest) {
                when(navigationView) {
                    is NavigationBarView -> navigationView.menu.findItem(R.id.folders_fragment_dest).isChecked = true
                    is NavigationView -> navigationView.menu.findItem(R.id.folders_fragment_dest).isChecked = true
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            binding.landscapeLayout?.let {
                binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                }
                navigationView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = insets.top
                }
            }

            windowInsets
        }
    }

    fun showSnackBar(message: String) {
        val navBar = binding.mainNavView
        val snackBar = Snackbar.make(navBar, message, Snackbar.LENGTH_SHORT)
        if (binding.landscapeLayout == null) snackBar.setAnchorView(navBar)
        binding.root.post {
            snackBar.show()
        }
    }
}
