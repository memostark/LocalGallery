package com.guillermonegrete.gallery.common

import android.content.Context
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
import com.guillermonegrete.gallery.MyApplication
import com.guillermonegrete.gallery.R
import com.guillermonegrete.gallery.databinding.ChoiceChipBinding
import com.guillermonegrete.gallery.databinding.DialogFileOrderByBinding
import com.guillermonegrete.gallery.tags.TagRepository
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class SortingDialog: BottomSheetDialogFragment() {

    private val args: SortingDialogArgs by navArgs()

    @Inject lateinit var tagRepository: TagRepository
    private val disposable = CompositeDisposable()

    private var checkedField = 0
    private var checkedOrder = Order.DEFAULT
    private var checkedTagId = NO_TAG_ID

    override fun onAttach(context: Context) {
        (context.applicationContext as MyApplication).appComponent.inject(this)
        super.onAttach(context)
    }

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
            checkedOrder = selections.sort
            fieldSort.check(checkedField)
            orderSort.check(checkedOrder.id)

            fieldSort.setOnCheckedChangeListener { _, i ->
                checkedField = i
            }

            orderSort.setOnCheckedChangeListener { _, i ->
                checkedOrder = Order.fromInteger(i)
            }

            doneButton.setOnClickListener {
                dismiss()
                setFragmentResult(RESULT_KEY, bundleOf(SORT_KEY to SortDialogChecked(checkedField, checkedOrder, checkedTagId)))
            }

            // handle tags
            val folderId = args.folderId
            if(folderId != 0L) {
                val tagSource = if(folderId == GET_ALL_TAGS) tagRepository.getTags() else tagRepository.getTags(folderId)
                disposable.add(tagSource
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

    override fun onDestroyView() {
        disposable.clear()
        super.onDestroyView()
    }

    companion object{
        const val RESULT_KEY = "sort_dialog_result"
        const val SORT_KEY = "sort_key"

        const val NO_TAG_ID = 0L

        /**
         * Used to indicate to get all the tags instead of just the tags of a specific folder
         */
        const val GET_ALL_TAGS = -1L
    }

    enum class Order(val id: Int, val oder: String){
        ASC(R.id.ascending_order, "asc"),
        DESC(R.id.descending_order, "desc");

        companion object {

            fun fromInteger(id: Int): Order {
                return values().firstOrNull { it.id == id} ?: DEFAULT
            }

            val DEFAULT = DESC
        }
    }
}

@Parcelize
data class SortDialogChecked(
    /**
     * The position in the array of the selected field.
     * The array is the other nav arg of the sorting dialog
     */
    val fieldIndex: Int,
    val sort: SortingDialog.Order,
    val tagId: Long = 0L,
): Parcelable
