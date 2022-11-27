package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.listeners.OnGenreSelectListener
import com.microsoft.mobile.polymer.mishtu.listeners.OnLanguageSelectListener
import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper
import com.microsoft.mobile.polymer.mishtu.ui.fragment.*
import com.microsoft.mobile.polymer.mishtu.utils.BinePlayerListenerActivity

class SearchActivity: BinePlayerListenerActivity(), SearchHomeFragment.OnLanguageItemClickListener,
    OnLanguageSelectListener, OnGenreSelectListener {

    var isBackTrace=false
    var searchBaseFragment: SearchBaseFragment? = null
    var contentProviderId: String? = null
    var isMovie: Boolean = true

    companion object {
        const val EXTRA_KEY_PRESELECT = "PreSelect"
        const val IS_BACK_TRACE = "isBackTraceEnabled"
        const val EXTRA_KEY_PRESELECT_VALUE = "PreSelectValue"
        const val EXTRA_KEY_CONTENT_PROVIDER_ID = "ContentProviderId"
        const val EXTRA_KEY_IS_MOVIE = "isMovie"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        isBackTrace=intent.getBooleanExtra(IS_BACK_TRACE,false)
        contentProviderId = intent.getStringExtra(SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID)
        isMovie = intent.getBooleanExtra(SearchActivity.EXTRA_KEY_IS_MOVIE, true)

        when (intent.getStringExtra(EXTRA_KEY_PRESELECT)) {
            "Language" -> intent.getStringExtra(EXTRA_KEY_PRESELECT_VALUE)?.let { onLanguageSelected(it) }
            "Genre" -> intent.getStringExtra(EXTRA_KEY_PRESELECT_VALUE)?.let{onGenreSelected(it)}
            "LanguageViewAll" -> onLanguageItemClicked()
            "GenreViewAll" -> onGenresItemClicked()
            else -> loadContentSearchHomeFragment()
        }
    }

    override fun teachingScreenName(): TooltipDisplayHelper.FTUScreen {
        return TooltipDisplayHelper.FTUScreen.Search
    }

    override fun teachingContainer(): Int {
        return R.id.teachingContainer
    }

    override fun completionCallback(): TooltipDisplayHelper.TooltipDisplayCompletion? {
        return null
    }

    override fun shouldShowOnResume(): Boolean {
        return true
    }

    override fun onLanguageItemClicked() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SearchByLanguageFragment()
        val additionalAttr = HashMap<String,Any?>()
        additionalAttr[SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID] = contentProviderId
        additionalAttr[SearchActivity.EXTRA_KEY_IS_MOVIE] = isMovie
        fragment.setListener(this)
        transaction.replace(R.id.frame, fragment)
        if(!isBackTrace)
        transaction.addToBackStack(null)
        transaction.commit()

        searchBaseFragment = fragment
    }

    override fun onGenresItemClicked() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SearchByGenresFragment()
        fragment.setListener(this)
        transaction.replace(R.id.frame, fragment)
        if(!isBackTrace)
        transaction.addToBackStack(null)
        transaction.commit()

        searchBaseFragment = fragment
    }

    override fun onSearchClicked() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SearchResultFragment.newInstance("",
            SearchResultFragment.SEARCH_TYPE.ALL,
            null)
        transaction.replace(R.id.frame, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun loadContentSearchHomeFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SearchHomeFragment()
        fragment.setListener(this)
        transaction.add(R.id.frame, fragment)
        transaction.commit()

        searchBaseFragment = fragment
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onLanguageSelected(language: String) {
        Log.e("Selected",language)
        val transaction = supportFragmentManager.beginTransaction()
        val additionalAttr = HashMap<String,Any?>()
        additionalAttr[SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID] = contentProviderId
        additionalAttr[SearchActivity.EXTRA_KEY_IS_MOVIE] = isMovie
        val fragment = SearchResultFragment.newInstance(language, SearchResultFragment.SEARCH_TYPE.LANGUAGE, additionalAttr)
        transaction.replace(R.id.frame, fragment)
        if(!isBackTrace)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onLanguageViewAllClicked() {

    }

    override fun onGenreViewAllSelected() {

    }

    override fun onGenreSelected(genre: String) {
        Log.e("Selected",genre)
        val additionalAttr = HashMap<String,Any?>()
        additionalAttr[SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID] = contentProviderId
        additionalAttr[SearchActivity.EXTRA_KEY_IS_MOVIE] = isMovie
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SearchResultFragment.newInstance(genre,
            SearchResultFragment.SEARCH_TYPE.GENRES,
            additionalAttr)
        transaction.replace(R.id.frame, fragment)
        if(!isBackTrace)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onQuerySearchClicked(query: String) {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = SearchResultFragment.newInstance(query,
            SearchResultFragment.SEARCH_TYPE.ALL,
            null)
        transaction.replace(R.id.frame, fragment)
        transaction.addToBackStack(null)
        transaction.commit()    }

    fun getSearchMicView(): ImageView? {
        return searchBaseFragment?.getSearchMicView()
    }
}