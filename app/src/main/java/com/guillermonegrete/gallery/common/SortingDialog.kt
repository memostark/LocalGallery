package com.guillermonegrete.gallery.common

import android.os.Bundle
import android.os.Parcelable
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
import kotlinx.parcelize.Parcelize

class SortingDialog: BottomSheetDialogFragment() {

    private val args: SortingDialogArgs by navArgs()

    private var checkedField = 0
    private var checkedOrder = DEFAULT_ORDER

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

            val selections = args.selections
            checkedField = selections.fieldIndex
            checkedOrder = selections.sortId
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
                setFragmentResult(RESULT_KEY, bundleOf(SORT_KEY to SortDialogChecked(checkedField, checkedOrder)))
            }

        }


        return binding.root
    }

    companion object{
        const val RESULT_KEY = "sort_dialog_result"
        const val SORT_KEY = "sort_key"

        const val DEFAULT_ORDER = R.id.ascending_order

        val sortIdMap = mapOf(
            DEFAULT_ORDER to "asc",
            R.id.descending_order to "desc",
        )
    }
}

@Parcelize
data class SortDialogChecked(val fieldIndex: Int, val sortId: Int): Parcelable
