package com.guillermonegrete.gallery.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.createViewModelLazy
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.navigation.fragment.findNavController

/**
 * A modified version of the original hiltNavGraphViewModels but this takes a lambda parameter that returns the nav graph id.
 */
inline fun <reified VM : ViewModel> Fragment.hiltNavGraphViewModels(
    crossinline navGraphId: () -> Int,
): Lazy<VM> {
    val backStackEntry by lazy {
        findNavController().getBackStackEntry(navGraphId())
    }
    val storeProducer: () -> ViewModelStore = {
        backStackEntry.viewModelStore
    }
    return createViewModelLazy(
        VM::class, storeProducer,
        factoryProducer = {
            HiltViewModelFactory(requireActivity(), backStackEntry.defaultViewModelProviderFactory)
        },
        extrasProducer = { backStackEntry.defaultViewModelCreationExtras }
    )
}
