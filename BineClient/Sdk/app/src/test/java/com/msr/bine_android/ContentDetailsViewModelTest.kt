// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.msr.bine_android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.msr.bine_android.base.AppBaseTest
import com.msr.bine_android.data.entities.CartEntity
import com.msr.bine_android.viewmodels.ContentDetailViewModel
import com.msr.bine_sdk.BineAPI
import com.msr.bine_sdk.models.*
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class ContentDetailsViewModelTest : AppBaseTest() {
    lateinit var viewModel: ContentDetailViewModel

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var bineAPI: BineAPI = mockk()
    private var dataRepository: DataRepository = mock()

    @Before
    fun setConfig() {
        super.setUp()
        viewModel= ContentDetailViewModel(bineAPI, dataRepository)
    }

    @After
    fun setTearDown() {
        super.tearDown()
    }

    /*@Test
    fun `fetching saved content`() {
        assert(dataRepository.getContent("BADLAPUR") != null)
        assert(dataRepository.getContent("BADLAPUR")?.content?.metadata?.title=="Dunkrik")

    }*/

    @Test
    fun `test viewmodel is fetching  content`() {
        val content = getSVODContentFolderEntity()
        runBlocking {
            whenever(dataRepository.getContent("BADLAPUR")).thenReturn(content)
        }

        // Load the task in the viewmodel
        viewModel.getContentData("BADLAPUR")

        viewModel.content.observeForTesting {
            assertThat("", viewModel.content.getOrAwaitValue().content.id == "BADLAPUR")
        }
    }

    @Test
    fun `is free content`() {
        println((viewModel.isFreeContent(getDummyFreeContent())))
        assert(viewModel.isFreeContent(getDummyFreeContent()))
        assert(!viewModel.isFreeContent(getDummySVODContent()))
        assert(!viewModel.isFreeContent(getDummyTVODContent()))
    }

    @Test
    fun `get SVOD details`() {
        assert(viewModel.getSVODDetails(getDummyFreeContent()) == null)
        assert(viewModel.getSVODDetails(getDummyTVODContent()) == null)
        val svod = viewModel.getSVODDetails(getDummySVODContent())
        assert(svod != null)
        assert(svod?.first == 99.0)
        assert(svod?.second == Status.NotPurchased)
    }

    @Test
    fun `get TVOD details`() {
        assert(viewModel.getTVODDetails(getDummyFreeContent()) == null)
        assert(viewModel.getTVODDetails(getDummySVODContent()) == null)
        val tvod = viewModel.getTVODDetails(getDummyTVODContent())
        assert(tvod != null)
        assert(tvod?.first == 99.0)
        assert(tvod?.second == Status.NotPurchased)
    }

    @Test
    fun `get trailer`() {
        val url = viewModel.trailerURL(getDummyTVODContent())
        assert(url != null)
        assert(url == "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    }

    @Test
    fun `get recommendations`() {
        val recommendations = viewModel.getRecommendations(getDummyTVODContent())
        assert(recommendations.size == 1)
        assert(recommendations[0].id == "Welcome_Back")
    }

    @Test
    fun `get content type`() {
        val content = viewModel.getContentType(getDummySVODContent())
        assert(content == ContentType.SERIES.value)
        val movie = viewModel.getContentType(getDummyMovieContent())
        assert(movie == ContentType.MOVIE.value)
    }

    @Test
    fun `is Item In Cart`() {
        val list = ArrayList<CartEntity>()
        assert(!viewModel.isItemAddedInCart("BADLAPUR", list))
        list.add(CartEntity("BADLAPUR"))
        assert(viewModel.isItemAddedInCart("BADLAPUR", list))
    }
}