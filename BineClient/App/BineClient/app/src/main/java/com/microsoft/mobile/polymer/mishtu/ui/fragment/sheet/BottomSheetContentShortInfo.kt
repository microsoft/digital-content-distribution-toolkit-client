package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetContentShortInfoBinding
import com.microsoft.mobile.polymer.mishtu.databinding.LayoutContentInfoBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.IncentiveViewModel
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BottomSheetContentShortInfo(private val content: Content) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetContentShortInfoBinding
    val incentiveViewModel by viewModels<IncentiveViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding =
            DataBindingUtil.inflate(inflater,
                R.layout.bottom_sheet_content_short_info,
                container,
                false)
        initView(binding.contentInfo)
        binding.contentTitle.visibility = View.VISIBLE
        if(!content.isMovie) {
            binding.contentTitle.text = "${content.title}  ${
                requireContext().getString(R.string.season_suffix)
                    .let { content.season!!.lowercase().replace("season", it) }
            } • ${requireContext().getString(R.string.episode_suffix).let { content.episode?.lowercase()?.replace("episode", it) }}"
        }
        else{
            binding.contentTitle.text = content.title
        }
        if (!content.free) {
            setCoinInfo()
        }
        binding.dialogClose.setOnClickListener {
            dismiss()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun initView(layoutContentInfoBinding: LayoutContentInfoBinding) {
        layoutContentInfoBinding.contentLanguage.text =
                getSpannable("${getString(R.string.ln_language)}: ${content.language}","${getString(R.string.ln_language)}:")
        layoutContentInfoBinding.contentDetailsDescription.text = content.longDescription
        layoutContentInfoBinding.contentDetailsDescription.text =
            if (content.isMovie)
                getSpannable("${getString(R.string.about_movie)}: ${layoutContentInfoBinding.contentDetailsDescription.text}",
                    "${getString(R.string.about_movie)}:")
            else
                getSpannable("${getString(R.string.story)}: ${layoutContentInfoBinding.contentDetailsDescription.text}",
                    "${getString(R.string.story)}:")

        /*content.additionalDescription1?.let {
            if (it.length > 200) {
                makeTextViewResizable(layoutContentInfoBinding.contentDetailsDescription,
                    3,
                    getString(R.string.bn_view_more),
                    true)
            }
        }*/

        content.getDisplayCast()?.let { castString ->
            layoutContentInfoBinding.contentDetailsCast.text =
                getSpannable("${getString(R.string.cast)}: ${castString.first}",
                    "${getString(R.string.cast)}:")
            layoutContentInfoBinding.contentDetailsDirector.text =
                getSpannable("${getString(R.string.director)}: ${castString.second}",
                    "${getString(R.string.director)}:")
        } ?: run {
            layoutContentInfoBinding.contentDetailsCast.visibility = View.GONE
            layoutContentInfoBinding.contentDetailsDirector.visibility = View.GONE
        }

        content.contentAdvisory?.let { advisory ->
            layoutContentInfoBinding.contentDetailsAdvisory.text =
                getSpannable("${getString(R.string.content_advisory)}: $advisory",
                    "${getString(R.string.content_advisory)}:")
        } ?: run {
            layoutContentInfoBinding.contentDetailsAdvisory.text =
                getSpannable("${getString(R.string.content_advisory)}: ${getString(R.string.content_advisory_na)}",
                    "${getString(R.string.content_advisory)}:")
        }
    }

    private fun makeTextViewResizable(tv: TextView, maxLine: Int, expandText: String, viewMore: Boolean) {
        if (tv.tag == null) {
            tv.tag = tv.text
        }
        val vto: ViewTreeObserver = tv.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val text: String
                val lineEndIndex: Int
                val obs: ViewTreeObserver = tv.viewTreeObserver
                obs.removeOnGlobalLayoutListener(this)
                if (maxLine == 0) {
                    lineEndIndex = tv.layout.getLineEnd(0)
                    text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                } else if (maxLine > 0 && tv.lineCount >= maxLine) {
                    lineEndIndex = tv.layout.getLineEnd(maxLine - 1)
                    text = tv.text.subSequence(0, lineEndIndex - expandText.length + 1)
                        .toString() + " " + expandText
                } else {
                    lineEndIndex = tv.layout.getLineEnd(tv.layout.lineCount - 1)
                    text = tv.text.subSequence(0, lineEndIndex).toString() + " " + expandText
                }
                val string = getSpannable("${getString(R.string.about_movie)}: $text",
                    "${getString(R.string.about_movie)}:")
                tv.movementMethod = LinkMovementMethod.getInstance()
                //if (shouldShowViewMore)
                tv.setText(
                    addClickablePartTextViewResizable(
                        string,
                        tv,
                        expandText,
                        viewMore), TextView.BufferType.SPANNABLE)
            }
        })
    }

    private fun addClickablePartTextViewResizable(
        strSpanned: Spanned, tv: TextView,
        spannableText: String, viewMore: Boolean,
    ): SpannableStringBuilder {
        val ssb = SpannableStringBuilder(strSpanned)
        if (strSpanned.contains(spannableText)) {
            ssb.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        tv.layoutParams = tv.layoutParams
                        tv.setText(tv.tag.toString(), TextView.BufferType.SPANNABLE)
                        tv.invalidate()
                        if (viewMore) {
                            makeTextViewResizable(tv, -1, getString(R.string.bn_view_less), false)
                        } else {
                            makeTextViewResizable(tv, 3, getString(R.string.bn_view_more), true)
                        }
                    }
                },
                strSpanned.indexOf(spannableText),
                strSpanned.indexOf(spannableText) + spannableText.length,
                0
            )
        }

        return ssb
    }


     fun getSpannable(string: String, boldText: String): SpannableString {
        val txtSpannable = SpannableString(string)
        if (boldText.length > string.length) return txtSpannable
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, boldText.length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtSpannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(),
            R.color.dark_text_color)), 0, boldText.length - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return txtSpannable
    }

    private fun setCoinInfo() {
        val text = if(content.isMovie) getString(R.string.watch_movie_and_earn) else getString(R.string.watch_series_and_earn)
        val planList = incentiveViewModel.planContainsEvent(BOConverter.EventType.CONTENT_STREAMED,null)
        if(planList.isNotEmpty()) {
            planList[0]?.let {
                binding.contentDetailsCoins.visibility = View.VISIBLE
                binding.contentDetailsCoins.text = String.format(
                        text,
                        it.formula.firstOperand
                )
            }
        }
    }
}