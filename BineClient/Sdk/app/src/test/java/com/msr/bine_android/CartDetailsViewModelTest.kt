package com.msr.bine_android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.msr.bine_android.base.AppBaseTest
import com.msr.bine_android.viewmodels.CartDetailViewModel
import com.msr.bine_sdk.BineAPI
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class CartDetailsViewModelTest : AppBaseTest() {

    lateinit var viewModel: CartDetailViewModel

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private var bineAPI: BineAPI = mockk()

    private var dataRepository: DataRepository = mock()

    private lateinit var bineApplication: BineApplication;

    @Before
    fun setConfig() {
        super.setUp()
        viewModel = CartDetailViewModel(context, bineAPI, dataRepository)
    }

    @After
    fun setTearDown() {
        super.tearDown()
    }

    @Test
    fun `test viewmodel is fetching cart items`() {
        val content = getDummyCartItems()
        print("dummy content${content.value}")
        runBlocking {
            whenever(dataRepository.getAllCartItems()).thenReturn(content)
        }

        mainCoroutineRule.pauseDispatcher()

        viewModel.getCartItems()

        viewModel.cartItemList.observeForTesting {
            MatcherAssert.assertThat("", viewModel.cartItemList.getOrAwaitValue().size == 1)
        }

        // Load the task in the view model
        viewModel.cartItemList.observeForTesting {
            print("size is"+viewModel.cartItemList.getOrAwaitValue().size)
            assert(viewModel.cartItemList.getOrAwaitValue().size == 1)
        }
        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()
    }

    @Test
    fun `check is price fetching `() {
        assert(viewModel.getPrice(getSVODContentFolderEntity()) != 0.0)
        assert(viewModel.getPrice(getSVODContentFolderEntity()) == 99.0)
        assert(viewModel.getPrice(getTVODContentFolderEntity()) != 0.0)
        assert(viewModel.getPrice(getTVODContentFolderEntity()) == 99.0)

    }

    @Test
    fun `check is cart total is calculating`() {
        assert(viewModel.getTotalPrice(getDummyContentList()) != 0.0)
        assert(viewModel.getTotalPrice(getDummyContentList()) == 198.0)
    }

    @Test
    fun `check number of movies and series count`() {
        assert(viewModel.getMovieSeriesCount(getDummyContentList()).second == 2)
    }

    @Test
    fun `check number of movies and series count string`() {
        assert(viewModel.getNumberOfMoviesSeries(getDummyContentList()) == "(2 Series)")
    }
}