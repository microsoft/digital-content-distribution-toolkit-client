package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.ui.adapter.LanguagesListAdapter
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentSearchContentByLanguageBinding
import com.microsoft.mobile.polymer.mishtu.listeners.OnLanguageSelectListener
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.GridSpacingItemDecoration
import kotlin.math.roundToInt

class SearchByLanguageFragment : SearchBaseFragment(), LanguagesListAdapter.OnLanguageItemClickListener {

    private lateinit var binding: FragmentSearchContentByLanguageBinding
    private lateinit var adapter: LanguagesListAdapter
    private lateinit var listener: OnLanguageSelectListener

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_search_content_by_language, container, false
        )

        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolBar)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolBar.setNavigationIcon(R.drawable.bn_ic_back_arrow)

        binding.toolBar.setNavigationOnClickListener {
            val data=activity?.supportFragmentManager?.backStackEntryCount
            if(data!=null&&data==0){
                activity?.finish()
            }else {
                activity?.supportFragmentManager?.popBackStack()
            }
        }

        adapter = LanguagesListAdapter(
            requireContext(),
            resources.getDimension(R.dimen.dp_100).toInt(),
            resources.getDimension(R.dimen.dp_100).toInt(),
            resources.getDimension(R.dimen.dp_2).toInt(),
            isGridView = true, isViewAllVisible = false, listener = this)

        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(requireContext(), 3)
        val spanCount = 3 // 3 columns
        val includeEdge = false
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, AppUtils.dpToPx(10.0f, resources), includeEdge))
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.adapter = adapter


        binding.searchContainer.setOnClickListener {
            listener.onLanguageSelected("")
        }
        return binding.root
    }

    override fun onLanguageClicked(language: String) {
        listener.onLanguageSelected(language)
    }

    override fun onViewAllCLicked() {

    }

    fun setListener(listener: OnLanguageSelectListener) {
        this.listener = listener
    }

    override fun getSearchMicView(): ImageView {
        return binding.idMic
    }
}