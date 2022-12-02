// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.ui.adapter.GeneresListAdapter
import com.microsoft.mobile.polymer.mishtu.ui.adapter.LanguagesListAdapter
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentSearchContentBinding
import com.microsoft.mobile.polymer.mishtu.utils.VoiceSearchUtil
import java.util.*
import kotlin.math.roundToInt

class SearchHomeFragment : SearchBaseFragment(), VoiceSearchUtil.VoiceSearchCallBack {

    private lateinit var binding: FragmentSearchContentBinding
    private lateinit var listener: OnLanguageItemClickListener
    private lateinit var contract: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_search_content, container, false
        )

        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolBar)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolBar.setNavigationIcon(R.drawable.bn_close_black)

        binding.toolBar.setNavigationOnClickListener {
            activity?.finish()
        }

        binding.searchContainer.setOnClickListener {
            listener.onSearchClicked()
        }
        binding.idMic.setOnClickListener {
            initVoiceInput()
        }

        setLanguageListView()
        setGenreListView()
        contract = VoiceSearchUtil.getNewInstance().getContract(this, this)
        return binding.root
    }

    private fun initVoiceInput() {
       VoiceSearchUtil.getNewInstance().voiceInput(requireContext() ,contract)
    }

    private fun setLanguageListView() {
        binding.languageList.subTitle.text =
            resources.getString(R.string.bn_hindi_english_kannada_and_more)
        binding.languageList.subTitle.visibility = View.VISIBLE

        binding.languageList.iconPlaceHolder.setImageDrawable(ContextCompat.getDrawable(
            requireContext(),
            R.drawable.bn_ic_language_placeholder))
        binding.languageList.iconPlaceHolder.visibility = View.VISIBLE

        val adapter = LanguagesListAdapter(requireContext(),
            requireContext().resources.getDimension(R.dimen.dp_90).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_90).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_12).roundToInt(),
            isGridView = false, isViewAllVisible = true, listener = object : LanguagesListAdapter.OnLanguageItemClickListener {
                override fun onLanguageClicked(language: String) {
                    listener.onLanguageSelected(language)
                }

                override fun onViewAllCLicked() {
                    listener.onLanguageItemClicked()
                }
            }
        )
        binding.languageList.recyclerView.adapter = adapter
    }

    private fun setGenreListView() {
        val genreAdapter = GeneresListAdapter(requireContext(),
            requireContext().resources.getDimension(R.dimen.dp_90).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_120).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_12).roundToInt(),
            isGridView = false, isViewAllVisible = true, listener = object : GeneresListAdapter.OnGenresItemClickListener {
                override fun onGenresClicked(genres: String) {
                    listener.onGenreSelected(genres)
                }

                override fun onViewAllCLicked() {
                    listener.onGenresItemClicked()
                }
            }
        )

        binding.genresList.subTitle.text =
            resources.getString(R.string.comedy_horror_thriller_and_more)
        binding.genresList.subTitle.visibility = View.VISIBLE

        binding.genresList.iconPlaceHolder.setImageDrawable(ContextCompat.getDrawable(requireContext(),
            R.drawable.bn_ic_language_placeholder))
        binding.genresList.iconPlaceHolder.visibility = View.VISIBLE

        binding.genresList.recyclerView.adapter = genreAdapter
    }

    fun setListener(listener: OnLanguageItemClickListener) {
        this.listener = listener
    }

    interface OnLanguageItemClickListener {
        fun onLanguageItemClicked()
        fun onGenresItemClicked()
        fun onSearchClicked()
        fun onLanguageSelected(language: String)
        fun onGenreSelected(genre: String)
        fun onQuerySearchClicked(query: String)
    }

    override fun getSearchMicView(): ImageView {
        return binding.idMic
    }

    override fun onSerachQueryDetected(res: String) {
        binding.searchContainerTv.text = res
        listener.onQuerySearchClicked(res)
    }
}