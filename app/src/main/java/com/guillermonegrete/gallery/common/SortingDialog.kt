package com.guillermonegrete.gallery.common

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.data.Tag
import com.guillermonegrete.gallery.databinding.ChoiceChipBinding
import com.guillermonegrete.gallery.databinding.DialogFileOrderByBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.*

class SortingDialog: BottomSheetDialogFragment() {

    private val args: SortingDialogArgs by navArgs()

    private val disposable = CompositeDisposable()

    private var checkedField = 0
    private var checkedOrder = DEFAULT_ORDER
    private var checkedTagId = NO_TAG_ID

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DialogFileOrderByBinding.inflate(inflater, container, false)

        with(binding){
            fieldSort.removeAllViews()

            args.options.mapIndexed { index, option ->
                val rb = RadioButton(ContextThemeWrapper(context, R.style.SortDialogRadioButton)).apply {
                    text = option.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
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
                setFragmentResult(RESULT_KEY, bundleOf(SORT_KEY to SortDialogChecked(checkedField, checkedOrder, checkedTagId)))
            }

            // handle tags
            if(args.folderId != 0L) {
                disposable.add(Single.just((1..20).map { Tag("placeholder$it", Date(), it.toLong()) })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { tags ->
                            chipScroll.isVisible = true
                            tags.forEach { tag ->
                                val chip =  ChoiceChipBinding.inflate(LayoutInflater.from(context)).root
                                chip.text = tag.name
                                if (tag.id == args.selections.tagId) chip.isChecked = true
                                chip.setOnCheckedChangeListener { _, isChecked ->
                                    if(isChecked) checkedTagId = tag.id
                                    else if (checkedTagId == tag.id) checkedTagId = NO_TAG_ID // unselected
                                }
                                tagsGroup.addView(chip)
                            }
                        },
                        { Timber.e(it) }
                    )
                )
            }

        }


        return binding.root
    }

    companion object{
        const val RESULT_KEY = "sort_dialog_result"
        const val SORT_KEY = "sort_key"

        const val DEFAULT_ORDER = R.id.descending_order
        const val NO_TAG_ID = 0L

        val sortIdMap = mapOf(
            R.id.ascending_order to "asc",
            DEFAULT_ORDER to "desc",
        )
    }
}

@Parcelize
data class SortDialogChecked(
    val fieldIndex: Int,
    val sortId: Int,
    val tagId: Long = 0L,
): Parcelable
