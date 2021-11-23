package com.guillermonegrete.gallery.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.DialogFileOrderByBinding

class SortingDialog: BottomSheetDialogFragment() {

    private val args: SortingDialogArgs by navArgs()

    private var checkedField = 0
    private var checkedOrder = R.id.ascending_order

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogFileOrderByBinding.inflate(inflater, container, false)

        with(binding){
            fieldSort.removeAllViews()

            args.options.mapIndexed { index, option ->
                val rb = RadioButton(ContextThemeWrapper(context, R.style.SortDialogRadioButton)).apply {
                    text = option.capitalize()
                    id = index
                }
                fieldSort.addView(rb)
            }

            fieldSort.check(checkedField)
            orderSort.check(checkedOrder)

            fieldSort.setOnCheckedChangeListener { _, i ->
                checkedField = i
            }

            orderSort.setOnCheckedChangeListener { _, i ->
                checkedOrder = i
            }

            doneButton.setOnClickListener {
                dismiss()
                val field = args.options[checkedField]
                val order = sortIdMap[checkedOrder]
                setFragmentResult(RESULT_KEY, bundleOf(SORT_KEY to "$field,$order"))
            }

        }


        return binding.root
    }

    private val sortIdMap = mapOf(
        R.id.ascending_order to "asc",
        R.id.descending_order to "desc",
    )

    companion object{
        const val RESULT_KEY = "sort_dialog_result"
        const val SORT_KEY = "sort_key"
    }
}
