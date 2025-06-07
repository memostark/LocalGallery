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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
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
            is NavigationBarView -> {
                navigationView.setOnItemSelectedListener {
                    val currentDest = navController.currentDestination?.id

                    if (currentDest == R.id.files_fragment_dest &&
                        it.itemId == R.id.all_files_dest) {
                        val builder = NavOptions.Builder().setRestoreState(true)
                        builder.setPopUpTo(
                            navController.graph.findStartDestination().id,
                            inclusive = true,
                            saveState = true
                        )
                        navController.navigate(it.itemId, null, builder.build())
                    } else {
                        NavigationUI.onNavDestinationSelected(it, navController)
                    }
                    return@setOnItemSelectedListener true
                }
            }
            is NavigationView -> NavigationUI.setupWithNavController(navigationView, navController)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            navigationView.isGone = when (destination.id) {
                R.id.file_details_dest, R.id.addTagFragment -> true
                // Keep the bar hidden when navigating to the files list from details
                R.id.files_fragment_dest -> navController.previousBackStackEntry?.destination?.id == R.id.file_details_dest
                else -> false
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            var nv = binding.landscapeLayout
            if (nv != null) {
                navigationView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    leftMargin = insets.left
                    topMargin = insets.top
                }
            }

            windowInsets
        }
    }

    fun showSnackBar(message: String) {
        val navBar = binding.mainNavView
        val snackBar = Snackbar.make(navBar, message, Snackbar.LENGTH_SHORT)
        snackBar.setAnchorView(navBar)
        snackBar.show()
    }
}
