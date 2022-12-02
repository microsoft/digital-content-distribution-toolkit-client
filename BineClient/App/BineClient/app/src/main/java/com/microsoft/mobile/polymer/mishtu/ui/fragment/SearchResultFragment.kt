// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentGlobalSearchContentBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.ui.activity.SearchActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.ViewAllContentActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.SearchListAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.SearchViewModel
import com.microsoft.mobile.polymer.mishtu.ui.views.SearchView
import com.microsoft.mobile.polymer.mishtu.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchResultFragment : Fragment(), OnItemClickListener,VoiceSearchUtil.VoiceSearchCallBack {
    enum class SEARCH_TYPE {
        ALL,
        LANGUAGE,
        GENRES
    }

    private lateinit var binding: FragmentGlobalSearchContentBinding
    private lateinit var searchResultsContentAdapter: SearchListAdapter
    private var searchQuery: String = ""
    private val viewModel by viewModels<SearchViewModel>()
    private lateinit var contract: ActivityResultLauncher<Intent>
    private var searchType = SEARCH_TYPE.ALL
    @Inject lateinit var subscriptionManager: SubscriptionManager
    companion object {

        private const val ARG_PARAM1 = "query"
        private const val ARG_PARAM2 = "search_type"
        private var additionalAttr: Map<String, Any?>? = null
        val CALLED_FROM = "SearchResultFragment"



        fun newInstance(
            query: String,
            searchType: SEARCH_TYPE,
            additionalAttr1: HashMap<String, Any?>?
        ): SearchResultFragment {
            val fragment = SearchResultFragment()
            val args = Bundle()
            additionalAttr = additionalAttr1
            args.putString(ARG_PARAM1, query)
            args.putInt(ARG_PARAM2, searchType.ordinal)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            searchQuery = it.getString(ARG_PARAM1).toString()
            when (it.getInt(ARG_PARAM2)) {
                0 -> searchType = SEARCH_TYPE.ALL
                1 -> searchType = SEARCH_TYPE.LANGUAGE
                2 -> searchType = SEARCH_TYPE.GENRES
            }
        }

        setHasOptionsMenu(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_global_search_content,
                container,
                false
        )

        val hubId = (requireActivity() as? SearchActivity)?.deviceViewModel?.getConnectedDevice()?.id
        searchResultsContentAdapter = SearchListAdapter(requireContext(), this,
            hubId ?: "",subscriptionManager
        )

        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(requireContext(), 3)

        val spanCount = 3 // 3 columns
        val includeEdge = false
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, AppUtils.dpToPx(10.0f, resources), includeEdge))


        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.adapter = searchResultsContentAdapter

        if (searchQuery.isNotEmpty()) {
            viewModel.getResults(searchQuery, searchType)
        }


        binding.inputSearch.setOnSearchViewListener(object : SearchView.OnSearchViewListener {
            override fun onBackButtonCLicked() {
                val data=activity?.supportFragmentManager?.backStackEntryCount
                if(data!=null&&data==0){
                    activity?.finish()
                }else {
                    activity?.supportFragmentManager?.popBackStack()
                }
            }

            override fun onMicButtonClicked() {
                Log.d("here", "onmicClicked")
                VoiceSearchUtil.getNewInstance().voiceInput(requireContext() ,contract)
            }

            override fun onTextChanged(result: String) {
                searchQuery = result
                searchType = SEARCH_TYPE.ALL
                if (result.isEmpty()) {
                    binding.inputSearch.setData(getString(R.string.bn_search_by_title_actor_genres),true,true,true)
                    binding.recyclerView.visibility = View.GONE
                    binding.titleResults.visibility = View.GONE
                } else {
                    viewModel.getResults(result, SEARCH_TYPE.ALL)
                }
            }

            override fun onInputEnterKeyCLicked(charSequence: String) {

            }
        })

        if(!searchQuery.isEmpty()) {
            binding.inputSearch.setData(searchQuery,
                true, true, false
            )

        }else{
            binding.inputSearch.setData(
                resources.getString(R.string.bn_search_by_title_actor_genres),
                true, true, true
            )
        }

        viewModel.downloads.observe(viewLifecycleOwner, {
            searchResultsContentAdapter.downloadsList = it
        })

        viewModel.searchResultsLiveData.observe(viewLifecycleOwner, {
            var filteredList = getFilteredContentList(it)
            if (filteredList.isNullOrEmpty()) {
                showEmptyResults(searchQuery)
            } else {
                setResultsFound(filteredList)
            }
        })

        val inputMethodManager: InputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_UNCHANGED_HIDDEN)
        binding.inputSearch.findViewById<EditText>(R.id.inputText).requestFocus()

        contract = VoiceSearchUtil.getNewInstance().getContract(this, this)
        return binding.root
    }


    override fun onPause() {
        super.onPause()
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
        super.onStop()
    }

    private fun setResultsFound(resultList: List<Content>) {
        if (!searchQuery.isEmpty()) {
            val txtSpannable = SpannableString(searchQuery)
            txtSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.titleResults.text = TextUtils.concat("Showing results for ", txtSpannable)
        }
        binding.titleResults.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.VISIBLE

        searchResultsContentAdapter.contentList = resultList
    }

    fun getFilteredContentList(contentList: List<Content>): List<Content> {
        var filteredList = contentList
        if (!additionalAttr.isNullOrEmpty()) {
            val contentProviderId =
                    additionalAttr?.get(SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID) as String?
            val isMovie =
                    additionalAttr?.get(SearchActivity.EXTRA_KEY_IS_MOVIE) as Boolean?
            if (contentProviderId != null && isMovie != null) {
                filteredList = contentList.filter { content -> content.isMovie == isMovie && content.contentProviderId == contentProviderId }
                filteredList = if (isMovie) {
                    filteredList
                } else {
                    filteredList.sortedBy { con -> con.episode?.lowercase()?.replace("episode", "")?.toInt() }
                            .distinctBy { content -> content.name }
                }
            } else {
                filteredList = contentList.sortedBy { con-> con.episode?.lowercase()?.replace("episode", "")
                        ?.replace("","0")?.toInt() }
                        .distinctBy { content -> content.name }
            }
        } else {
            filteredList = contentList.sortedBy { con-> con.episode?.lowercase()?.replace("episode", "")
                    ?.replace("","0")?.toInt() }
                    .distinctBy { content -> content.name }
        }
        return filteredList
    }

    /**
     * method will show
     */
    private fun showEmptyResults(charSequence: String?) {
        val text = resources.getString(
                R.string._results,
                0
        )
        val txtSpannable = SpannableString(text)
        txtSpannable.setSpan(
                StyleSpan(Typeface.BOLD),
                0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.titleResults.text =
                TextUtils.concat(
                        txtSpannable,
                        resources.getString(R.string.bn_results_sub_text, charSequence)
                )

        viewModel.getAllContent().observe(viewLifecycleOwner, {
            var resultList = getFilteredContentList(it)
            searchResultsContentAdapter.contentList = resultList
        })

        binding.recyclerView.visibility = View.VISIBLE
        binding.titleResults.visibility = View.VISIBLE

    }

    override fun onItemCLicked(content: ContentDownload) {
        val params = HashMap<String, String>()
        params["Query"] = searchQuery
        AnalyticsLogger.getInstance().logEvent(Event.CONTENT_SEARCH, params)
        val map = HashMap<String, Any?>()
        map[ViewAllContentActivity.EXTRA_CALLED_FROM] = SearchResultFragment.CALLED_FROM
        (requireActivity() as? SearchActivity)?.onItemClicked(content,
            (requireActivity() as? SearchActivity)?.deviceViewModel?.isConnectionActive() ?: false, map)
    }

    override fun onSerachQueryDetected(res: String) {
        searchQuery = res
        binding.inputSearch.setData(res, true, true, false)
        viewModel.getResults(res, SEARCH_TYPE.ALL)
    }
}