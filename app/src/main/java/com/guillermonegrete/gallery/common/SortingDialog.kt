package com.guillermonegrete.gallery.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.DialogFileOrderByBinding

class SortingDialog: BottomSheetDialogFragment() {

    private val args: SortingDialogArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogFileOrderByBinding.inflate(inflater, container, false)

        with(binding){
            fieldSort.removeAllViews()

            args.options.mapIndexed { index, option ->
                val rb = RadioButton(ContextThemeWrapper(context, R.style.SortDialogRadioButton)).apply {
                    text = option
                    id = index
                }
                fieldSort.addView(rb)
            }

        }


        return binding.root
    }
}
