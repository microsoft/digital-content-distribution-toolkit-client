package com.microsoft.mobile.polymer.mishtu.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.storage.repositories.ContentRepository
import com.microsoft.mobile.polymer.mishtu.ui.fragment.SearchResultFragment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val contentRepository: ContentRepository) : ViewModel() {
    internal var searchResultsLiveData = MutableLiveData<List<Content>>()
    val downloads = contentRepository.getAllDownloads()

    fun getResults(queryText: String, searchType: SearchResultFragment.SEARCH_TYPE) {
        Log.e("type of search", searchType.toString())
        var queryText1 = queryText.lowercase().replace(" movies","").replace(" movie","")
        CoroutineScope(Dispatchers.Default).launch {
            val result = when (searchType) {
                SearchResultFragment.SEARCH_TYPE.ALL -> {
                    contentRepository.getContentByQuery(queryText1)
                }
                SearchResultFragment.SEARCH_TYPE.LANGUAGE -> {
                    contentRepository.getContentByLanguage(queryText)
                }
                SearchResultFragment.SEARCH_TYPE.GENRES -> {
                    contentRepository.getContentByGenre(queryText)
                }
            }

            if (result.isNotEmpty()) {
                searchResultsLiveData.postValue(result)
            } else {
                searchResultsLiveData.postValue(arrayListOf())
            }
        }
    }

    fun getAllContent(): LiveData<List<Content>> {
        return contentRepository.getAllContent()
    }
}