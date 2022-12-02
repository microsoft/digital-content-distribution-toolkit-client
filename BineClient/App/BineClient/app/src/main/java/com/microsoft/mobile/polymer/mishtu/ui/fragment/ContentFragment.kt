package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.Manifest
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.loopnow.fireworklibrary.views.OnItemClickedListener
import com.loopnow.fireworklibrary.views.VideoFeedView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.*
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.NetworkCapabilities
import com.microsoft.mobile.polymer.mishtu.kaizala_utils.NetworkConnectivity
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Event
import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper
import com.microsoft.mobile.polymer.mishtu.ui.activity.*
import com.microsoft.mobile.polymer.mishtu.ui.adapter.*
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.microsoft.mobile.polymer.mishtu.storage.entities.ActiveSubscription
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetContentProvider
import com.microsoft.mobile.polymer.mishtu.telemetry.bootmarker.BootTelemetryLogger
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.SubscriptionViewModel
import com.microsoft.mobile.polymer.mishtu.ui.views.*
import com.microsoft.mobile.polymer.mishtu.utils.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@AndroidEntryPoint
class ContentFragment(var type: TYPE, var contentProviderId: String) : DeviceConnectFragment(),
    ErrorView.OnErrorViewRetryClickedListener,
    BigMovieCard.OnPlayClickListener,
    OnItemClickListener,
    PackPromotionBanner.OnClickListener,
    ContentProviderAdapter.onItemClickListner
    {
    enum class TYPE {
        /*ALL,*/
        CLIPS,
        MOVIES,
        SERIES
    }

    private lateinit var binding: FragmentContentBinding
    private val contentViewModel by viewModels<ContentViewModel>()
    private val subscriptionViewModel by viewModels<SubscriptionViewModel>()

    private var offlineDownloadPromoCard: OfflineDownloadPromoCard? = null
    private var freeMoviesView: TopContentListView? = null
    private var activeOrderViewBinding: ViewActiveOrderCardBinding? = null
    private var packPromotionBanner: PackPromotionBanner? = null
    private var bigMovieCardViewList: MutableList<BigMovieCard> = ArrayList()
    private var playingIndex: Int = -1

    private var continueWatchListAdapter: ContinueWatchListAdapter? = null
    private var contentProviderAdapter: ContentProviderAdapter? = null
    private var viewActiveOrderAdapter: ViewActiveOrderAdapter? = null

    private var continueWatchBinding: ViewContinueWatchBinding? = null

    private var viewFwFeedBinding: ViewFwFeedBinding? = null
    private var viewMishtuClipsFwFeedBinding: ViewFwFeedBinding? = null
    private var fwVideoView: VideoFeedView? = null
    private var viewFwCategoryBinding: ViewFwCategoryBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            when (requireArguments().getInt(ARG_PARAM1)) {
                0 -> type = TYPE.CLIPS

            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_content, container, false
        )

        val children = getFragmentChildren()
        addBootTelemetryLogs()

        if (type != TYPE.CLIPS) {
            binding.changeContentProvider.visibility = View.VISIBLE
            setContentProviderInfo()
            binding.changeContentProvider.setOnClickListener {
                changeContentProvider()
            }
            addActiveOrderOrSubscription()
            deviceViewModel.loadGlobalData()
        }
        else{
            binding.changeContentProvider.visibility = View.GONE
        }
        addFragmentChildView(children)

        observeData()
        return binding.root
    }

        private fun addFragmentChildView(children: Array<String>) {
            for (model in children) {
                when (model) {
                    BNConstants.CHILDTYPE.FIREWORKSHORTCLIPS.value -> {
                        viewFwFeedBinding = addFireworkShortClips(
                            getString(R.string.bn_more_short_clips),  BNConstants.FIREWORK_SHORT_VIDEO_CHANNEL_ID, null)
                    }
                    BNConstants.CHILDTYPE.MISHTUSHORTCLIPS.value -> {
                        viewMishtuClipsFwFeedBinding = addFireworkShortClips(
                            getString(R.string.bn_mishtu_short_clips),BNConstants.FIREWORK_MISHTU_TRAILER_CHANNEL_ID, BNConstants.FIREWORK_MISHTU_TRAILER_PLAYLIST_ID)
                    }
                    BNConstants.CHILDTYPE.LANGUAGELIST.value -> addLanguagesList()
                    BNConstants.CHILDTYPE.GENRESLIST.value -> addGenresList()
                    BNConstants.CHILDTYPE.FIREWORKCATEGORIES.value -> addFWCategories()
                    BNConstants.CHILDTYPE.CONTINUEWATCH.value -> addContinueToWatchList()
                    BNConstants.CHILDTYPE.OFFLINEPROMOCARD.value -> addOfflinePromoCard()
                    BNConstants.CHILDTYPE.FREEMOVIES.value -> addFreeMovies()
                    BNConstants.CHILDTYPE.PACK_PROMOTION_BANNER.value -> addPackPromoBanner()
                    BNConstants.CHILDTYPE.BIGMOVIECARD.value -> addBigMovieCard()
                }

                binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
                    scrollChangeAction()
                })
            }
        }

        private fun getSelectedContentProvider(type: String) =
            SharedPreferenceStore.getInstance().get(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER + type)

        private fun getFragmentChildren() = when (type) {
            TYPE.CLIPS -> BNConstants.CLIPS
            TYPE.MOVIES -> BNConstants.MOVIES
            TYPE.SERIES -> BNConstants.SERIES
        }

        private fun addBootTelemetryLogs() {
            when (type) {
                TYPE.CLIPS -> BootTelemetryLogger.getInstance()
                    .recordPageEvent(BootTelemetryLogger.BootPage.FIREWORKS,
                        BootTelemetryLogger.BootMarker.PAGE_CREATE)
                TYPE.MOVIES -> BootTelemetryLogger.getInstance()
                    .recordPageEvent(BootTelemetryLogger.BootPage.FILMS,
                        BootTelemetryLogger.BootMarker.PAGE_CREATE)
                TYPE.SERIES -> {}
            }
        }

        private fun setContentProviderInfo() {
            val cpName = AppUtils.getContentProviderNameFromId(this.contentProviderId)
            binding.selectedContentProvider.text = requireContext().getString(R.string.you_are_watching,cpName)
            AppUtils.loadImage(requireContext(),
                AppUtils.getContentProviderSquareLogoURL(this.contentProviderId),
                binding.cpImageview)
        }

        private fun changeContentProvider() {
            val bottomSheet = BottomSheetContentProvider(this)
            bottomSheet.show(childFragmentManager, bottomSheet.tag)
        }

    private fun scrollChangeAction() {
        Log.d("OnPuase","ScrollAction")
        packPromotionBanner?.let {
            if (isViewVisible(it)) {
                (requireActivity() as MainActivity).start(TooltipDisplayHelper.FTUScreen.BuyPack)
            }
        }

        bigMovieCardViewList.let {
            var cardAlreadyVisible: Boolean = false
            for (card in it) {
                if (isViewVisible(card) && !cardAlreadyVisible) {
                    playingIndex = it.indexOf(card)
                    cardAlreadyVisible = true
                    card.playBigMovieCard()
                    if (deviceViewModel.isConnectionActive()) {
                        card.animateDownload()
                    }
                } else {
                    card.pauseBigMovieCard()
                    if (deviceViewModel.isConnectionActive()) {
                        card.stopDownloadAnimation()
                    }
                }
            }
        }
    }

    private fun addBigMovieCard() {
        //Enable download only if status is Hub_Exist
        val bigMovieCard = BigMovieCard(this,
            ((requireActivity() as? HubScanBaseActivity)?.getLastStatus() ?: HubScanBaseActivity.HubScanStatus.NO_PERMISSION_GRANTED)
                    == HubScanBaseActivity.HubScanStatus.HUB_EXIST,
            requireContext(),childFragmentManager)

        binding.parent.addView(bigMovieCard)
        bigMovieCardViewList.add(bigMovieCard)
        if (bigMovieCardViewList.size == 1) {
            //Add marker to record complete load of first card without data
            addObserverToLogBootEvent(bigMovieCard, BootTelemetryLogger.BootPage.FILMS,
                BootTelemetryLogger.BootMarker.FILMS_PRE_LOAD)
        }
    }

    private fun addOfflinePromoCard() {
        //No use of showing the card if NO_HUB_EXIST
        if (((requireActivity() as? HubScanBaseActivity)?.getLastStatus() ?: HubScanBaseActivity.HubScanStatus.NO_PERMISSION_GRANTED)
            != HubScanBaseActivity.HubScanStatus.NO_HUB_EXIST) {
            offlineDownloadPromoCard = OfflineDownloadPromoCard(requireContext())
            binding.parent.addView(offlineDownloadPromoCard)
        }
    }

    fun scrollDown() {
        binding.scrollView.post {
            val sHeight: Int = binding.scrollView.bottom
            binding.scrollView.smoothScrollTo(0, (sHeight) / 2)
        }
    }

    private fun isViewVisible(view: View): Boolean {
        val scrollBounds = Rect()
        binding.scrollView.getDrawingRect(scrollBounds)
        val top = view.y
        val bottom = top + view.height
        return scrollBounds.top <= top && scrollBounds.bottom >= bottom
    }

    override fun onResume() {
        super.onResume()

        if (!NetworkConnectivity.getInstance(requireContext()).isNetworkConnected) {
            showNoInternetConnection()
        } else {
            dismissNoInternetConnection()
        }

        SharedPreferenceStore.getInstance().get(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER)?.let {
            if (it == "1") {
                AnalyticsLogger.getInstance().logShortFormContentPause()
                SharedPreferenceStore.getInstance()
                    .save(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER, "0")
                SharedPreferenceStore.getInstance().save(SharedPreferenceStore.KEY_FW_CATEGORY, "")
            }
        }

        if (type != TYPE.CLIPS) {
            contentViewModel.getContinueMoviesWatchList(if(type == TYPE.MOVIES) 1 else 0)
        }
    }

    override fun onDestroy() {
        NetworkConnectivity.getInstance(requireContext()).unregisterListener(this)
        NetworkCapabilities.getInstance().unregisterListener(this)
        if (playingIndex != -1) {
            val playingCard: BigMovieCard = bigMovieCardViewList[playingIndex]
            playingCard.releasePlayer()
        }
        Log.d("Firework1", "exit ")
        super.onDestroy()
    }

    private fun showNoInternetConnection() {
        activity?.runOnUiThread {
            if (type == TYPE.CLIPS) {
                binding.contentErrorView.setErrorType(ErrorView.INTERNET_CONNECTION)
                binding.contentErrorView.visibility = View.VISIBLE
                binding.contentContainer.visibility = View.GONE
            }
            viewFwFeedBinding?.fwFeedView?.visibility = View.GONE
            viewFwFeedBinding?.fwFeedErrorContainer?.visibility = View.VISIBLE

            viewMishtuClipsFwFeedBinding?.fwFeedView?.visibility = View.GONE
            viewMishtuClipsFwFeedBinding?.fwFeedErrorContainer?.visibility = View.VISIBLE

            viewFwCategoryBinding?.fwCategoryParent?.visibility = View.GONE
        }
    }

    private fun dismissNoInternetConnection() {
        activity?.runOnUiThread {
            binding.contentErrorView.visibility = View.GONE
            binding.contentContainer.visibility = View.VISIBLE

            viewFwFeedBinding?.fwFeedView?.visibility = View.VISIBLE
            viewFwFeedBinding?.fwFeedErrorContainer?.visibility = View.GONE

            viewMishtuClipsFwFeedBinding?.fwFeedView?.visibility = View.VISIBLE
            viewMishtuClipsFwFeedBinding?.fwFeedErrorContainer?.visibility = View.GONE
            fwVideoView?.start()

            viewFwCategoryBinding?.fwCategoryParent?.visibility = View.VISIBLE
        }
    }

    fun observeData() {
        if (type != TYPE.CLIPS) {
            observeContentData()
            observerOrderData()
            contentViewModel.selectedContetnProvider.postValue(getSelectedContentProvider(type.name))
        }
    }

    private fun observeContentData() {
        when (type) {
            TYPE.MOVIES -> {
                observeMoviesData()
                deviceViewModel.selectedMovieContetnProvider.postValue(getSelectedContentProvider(type.name))
            }
            TYPE.SERIES -> {
                observeSeriesData()
                deviceViewModel.selectedSeriesContetnProvider.postValue(getSelectedContentProvider(type.name))
            }
            else ->{}
        }
    }

    private fun observeMoviesData() {

        deviceViewModel.contentMovieLiveData(8).observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                addBootTetemetryLogsForFilmsAndSeries()
            }

                for (view in bigMovieCardViewList) {
                    val viewIndex = bigMovieCardViewList.indexOf(view)
                    if (viewIndex < it.size) {
                        view.hasActiveSubscription = subscriptionViewModel.hasValidSubscriptionForContent(contentProviderId, it[viewIndex].contentId)
                        if (view.content?.contentId != it[viewIndex].contentId) {
                            view.initBigMovieCardView(it[viewIndex])
                        } else {
                            view.updateDownloadStatus(it[viewIndex])
                        }
                    } else {
                        view.hideView()
                    }
                }


            /*offlineDownloadPromoCard?.setBackground(if (it.size < 6) it.subList(0,
                it.size) else it.subList(0, 6))*/
        }


        deviceViewModel.paidContentMovieLiveDataDeviceAware(5).observe(viewLifecycleOwner) {
            packPromotionBanner?.setData(it, if (it.isNotEmpty()) it[0].contentProviderId else "")

        }

        deviceViewModel.freeContentMovieLiveDataDeviceAware(5).observe(viewLifecycleOwner) {
            freeMoviesView?.setData(it,
                    getString(R.string.text_free_movies),
                    ContextCompat.getDrawable(requireContext(), R.drawable.bn_ic_erosnow),
                    if (it.isNotEmpty()) it[0].contentProviderId else "")

        }

        contentViewModel.moviesWatchListLiveData.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                continueWatchBinding?.parentCardView?.visibility = View.VISIBLE
                continueWatchListAdapter?.setDataList(it)
            }
        }

        contentViewModel.getContinueMoviesWatchList(if(type == TYPE.MOVIES) 1 else 0)

        contentViewModel.downloadProgress.observe(viewLifecycleOwner) {
            if (it) (requireActivity() as? MainActivity)?.showProgress()
            else (requireActivity() as? MainActivity)?.hideProgress()
        }

        contentViewModel.goToDownloads.observe(viewLifecycleOwner) {
            if (it) (requireActivity() as? MainActivity)?.selectTab(2)
        }
    }

    private fun observeSeriesData() {
        deviceViewModel.contentSeriesLiveData(8).observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                addBootTetemetryLogsForFilmsAndSeries()
            }
                for (view in bigMovieCardViewList) {
                    val viewIndex = bigMovieCardViewList.indexOf(view)
                    if (viewIndex < it.size) {
                        view.hasActiveSubscription = subscriptionViewModel.hasValidSubscriptionForContent(contentProviderId, it[viewIndex].contentId)
                        if (view.content?.contentId != it[viewIndex].contentId) {
                            view.initBigMovieCardView(it[viewIndex])
                        } else {
                            view.updateDownloadStatus(it[viewIndex])
                        }
                    } else {
                        view.hideView()
                    }
                }


            /*offlineDownloadPromoCard?.setBackground(if (it.size < 6) it.subList(0,
                it.size) else it.subList(0, 6))*/
        }


        deviceViewModel.paidContentSeriesLiveDataDeviceAware(5).observe(viewLifecycleOwner) {
            packPromotionBanner?.setData(it, if (it.isNotEmpty()) it[0].contentProviderId else "")

        }

        deviceViewModel.freeContentSeriesLiveDataDeviceAware(5).observe(viewLifecycleOwner) {
            freeMoviesView?.setData(it,
                    getString(R.string.text_free_movies),
                    ContextCompat.getDrawable(requireContext(), R.drawable.bn_ic_erosnow),
                    if (it.isNotEmpty()) it[0].contentProviderId else "")

        }

        contentViewModel.moviesWatchListLiveData.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                continueWatchBinding?.parentCardView?.visibility = View.VISIBLE
                continueWatchListAdapter?.setDataList(it)
            }
        }

        contentViewModel.getContinueMoviesWatchList(if(type == TYPE.MOVIES) 1 else 0)

        contentViewModel.downloadProgress.observe(viewLifecycleOwner) {
            if (it) (requireActivity() as? MainActivity)?.showProgress()
            else (requireActivity() as? MainActivity)?.hideProgress()
        }

        contentViewModel.goToDownloads.observe(viewLifecycleOwner) {
            if (it) (requireActivity() as? MainActivity)?.selectTab(2)
        }
    }

        private fun addBootTetemetryLogsForFilmsAndSeries() {
            BootTelemetryLogger.getInstance().recordPageEvent(
                BootTelemetryLogger.BootPage.FILMS,
                BootTelemetryLogger.BootMarker.FILMS_DB_FETCH
            )
            //Add marker to record complete load of first card after
            //actual data fetched
            addObserverToLogBootEvent(bigMovieCardViewList[0],
                BootTelemetryLogger.BootPage.FILMS,
                BootTelemetryLogger.BootMarker.FILMS_LOAD_COMPLETE)
        }

        private fun observerOrderData() {
            var showOrderCard = false
            var showExpiredSubsCard = false
            contentViewModel.getActiveOrderForContentProvider().observe(viewLifecycleOwner) {
                it?.let { orderList ->
                    showOrderCard = true
                    viewActiveOrderAdapter?.setOrderList(orderList)
                }
                if(it.isNullOrEmpty()){
                    showOrderCard = false
                }
            }

            contentViewModel.activeSubscriptionLiveData().observe(viewLifecycleOwner) { subscription ->
                subscription?.let {
                    if (it.isNotEmpty()) {
                        packPromotionBanner?.setActiveSubscription(subscriptionViewModel.hasSvodActiveSubscription(it[0].providerId))
                        viewActiveOrderAdapter?.setActiveSubsList(it.filter { activeSub -> activeSub.isExpired() }.map { activeSub -> activeSub.subscription })
                        showExpiredSubsCard = true
                    }
                }
                if (subscription.isNullOrEmpty()) {
                    packPromotionBanner?.setActiveSubscription(false)
                    showExpiredSubsCard = false
                }

                if (showOrderCard || showExpiredSubsCard) {
                    activeOrderViewBinding?.root?.visibility = View.VISIBLE
                } else {
                    activeOrderViewBinding?.root?.visibility = View.GONE
                }
                setBMCFooterOnPackPurchased(subscription)
            }
        }

        private fun setBMCFooterOnPackPurchased(subscriptionList: List<ActiveSubscription>?) {
            for (view in bigMovieCardViewList) {
                view.content?.let {
                    val hasValidSubscription = subscriptionViewModel.hasValidSubscriptionForContent(it.contentProviderId,
                            it.contentId, subscriptionList)
                    view.hasActiveSubscription = hasValidSubscription
                    if (!it.free) {
                        if (hasValidSubscription) {
                            view.setBMCFooterForActiveSubscription()
                        } else {
                            view.setBMCFooterForNoSubscription()
                        }
                    }
                }
            }
        }


    private fun addFireworkShortClips(title: String, channelId: String, playListId: String?): ViewFwFeedBinding {
        val inflater: LayoutInflater =
            requireActivity().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val bindingFW = ViewFwFeedBinding.inflate(inflater)
        binding.parent.addView(bindingFW.root)

        bindingFW.fwViewTitle.text = title
        fwVideoView = bindingFW.fwFeedView
        if(playListId.isNullOrEmpty()) {
            fwVideoView?.setChannel(channelId)
        }else{
            fwVideoView?.setPlaylist(channelId, playListId)
        }

        val cardViewMarginParams =
            bindingFW.parentCardView.layoutParams as ViewGroup.MarginLayoutParams
        cardViewMarginParams.setMargins(0, 0, 0, resources.getDimension(R.dimen.dp_8).toInt())
        bindingFW.parentCardView.layoutParams = cardViewMarginParams

        fwVideoView?.addOnItemClickedListener(object : OnItemClickedListener {
            override fun onItemClicked(index: Int, title: String, id: String, videoDuration: Long) {
                SharedPreferenceStore.getInstance()
                    .save(SharedPreferenceStore.KEY_SHORT_PLAY_TRACKER, "1")
                AnalyticsLogger.getInstance().logShortFormContentView()
            }
        })
        return bindingFW
    }

    override fun onPause() {
        onFragmentChanged()
        super.onPause()
    }

    fun onFragmentChanged() {
        if (playingIndex != -1) {
            val playingCard: BigMovieCard = bigMovieCardViewList[playingIndex]
            playingCard.pauseMedia()
        }
    }

    override fun onStop() {
        if (playingIndex != -1) {
            val playingCard: BigMovieCard = bigMovieCardViewList[playingIndex]
            playingCard.pauseMedia()
        }
        super.onStop()
    }

    private fun addFWCategories() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewFwCategoryBinding = ViewFwCategoryBinding.inflate(inflater)
        binding.parent.addView(viewFwCategoryBinding?.root)

        val mAdapter = FireworkCategoryAdapter(requireContext(),
            object : FireworkCategoryAdapter.ItemClickListener {
                override fun onCategoryClicked(categoryName: String, categoryID: String) {
                    //FwSDK.play(requireActivity(), 121, FeedType.CHANNEL, categoryID, null)
                    val intent = Intent(context, FireworkCategoryActivity::class.java)
                    intent.putExtra(FireworkCategoryActivity.EXTRA_CATEGORY_TITLE, categoryName)
                    intent.putExtra(FireworkCategoryActivity.EXTRA_CATEGORY_ID, categoryID)
                    requireContext().startActivity(intent)

                    val params = HashMap<String, String>()
                    params["provider"] = "Firework"
                    params["category"] = categoryName
                    AnalyticsLogger.getInstance().logEvent(Event.SHORT_FORM_CATEGORY, params)
                }
            })
        viewFwCategoryBinding?.fwCategoryRecyclerview?.adapter = mAdapter
        val cardViewMarginParams =
            viewFwCategoryBinding?.parentCardView?.layoutParams as ViewGroup.MarginLayoutParams
        cardViewMarginParams.setMargins(0,
            resources.getDimension(R.dimen.dp_8).toInt(),
            0,
            resources.getDimension(R.dimen.dp_8).toInt())
        viewFwCategoryBinding?.parentCardView?.layoutParams = cardViewMarginParams

        viewFwCategoryBinding?.fwCategoryRecyclerview?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewFwCategoryBinding?.fwCategoryRecyclerview?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                BootTelemetryLogger.getInstance().recordPageEvent(
                    BootTelemetryLogger.BootPage.FIREWORKS,
                    BootTelemetryLogger.BootMarker.FIREWORK_CATEGORY_LOAD
                )
            }
        })
    }

    private fun addLanguagesList() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val languageBinding = ViewLangaguesListBinding.inflate(inflater)
        val adapter = LanguagesListAdapter(requireContext(),
            requireContext().resources.getDimension(R.dimen.dp_90).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_90).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_12).roundToInt(),
            isGridView = false,
            isViewAllVisible = true,
            listener = object : LanguagesListAdapter.OnLanguageItemClickListener {
                override fun onLanguageClicked(language: String) {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra(SearchActivity.EXTRA_KEY_PRESELECT, "Language")
                    intent.putExtra(SearchActivity.EXTRA_KEY_PRESELECT_VALUE, language)
                    intent.putExtra(SearchActivity.IS_BACK_TRACE, true)
                    intent.putExtra(SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID, contentProviderId)
                    intent.putExtra(SearchActivity.EXTRA_KEY_IS_MOVIE, if(type == TYPE.MOVIES) true else false)
                    activity?.startActivity(intent)
                }

                override fun onViewAllCLicked() {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra(SearchActivity.EXTRA_KEY_PRESELECT, "LanguageViewAll")
                    intent.putExtra(SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID, contentProviderId)
                    intent.putExtra(SearchActivity.EXTRA_KEY_IS_MOVIE, if(type == TYPE.MOVIES) true else false)
                    intent.putExtra(SearchActivity.IS_BACK_TRACE, true)

                    activity?.startActivity(intent)
                }
            }
        )
        binding.parent.addView(languageBinding.root)
        val cardViewMarginParams =
            languageBinding.parentCardView.layoutParams as ViewGroup.MarginLayoutParams
        cardViewMarginParams.setMargins(0, 0, 0, resources.getDimension(R.dimen.dp_8).toInt())
        languageBinding.parentCardView.layoutParams = cardViewMarginParams
        languageBinding.recyclerView.adapter = adapter
    }

    private fun addGenresList() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val genresListBinding = ViewGenresListBinding.inflate(inflater)
        val adapter = GeneresListAdapter(requireContext(),
            requireContext().resources.getDimension(R.dimen.dp_90).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_120).roundToInt(),
            requireContext().resources.getDimension(R.dimen.dp_12).roundToInt(),
            isGridView = false,
            isViewAllVisible = true,
            listener = object : GeneresListAdapter.OnGenresItemClickListener {
                override fun onGenresClicked(genres: String) {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra(SearchActivity.EXTRA_KEY_PRESELECT_VALUE, genres)
                    intent.putExtra(SearchActivity.EXTRA_KEY_PRESELECT, "Genre")
                    intent.putExtra(SearchActivity.IS_BACK_TRACE, true)
                    intent.putExtra(SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID, contentProviderId)
                    intent.putExtra(SearchActivity.EXTRA_KEY_IS_MOVIE, if(type == TYPE.MOVIES) true else false)
                    activity?.startActivity(intent)
                }

                override fun onViewAllCLicked() {
                    val intent = Intent(requireContext(), SearchActivity::class.java)
                    intent.putExtra(SearchActivity.EXTRA_KEY_PRESELECT, "GenreViewAll")
                    intent.putExtra(SearchActivity.IS_BACK_TRACE, true)
                    intent.putExtra(SearchActivity.EXTRA_KEY_CONTENT_PROVIDER_ID, contentProviderId)
                    intent.putExtra(SearchActivity.EXTRA_KEY_IS_MOVIE, if(type == TYPE.MOVIES) true else false)
                    activity?.startActivity(intent)
                }
            }
        )
        binding.parent.addView(genresListBinding.root)

        genresListBinding.recyclerView.adapter = adapter
    }

    private fun addFreeMovies() {
        freeMoviesView = TopContentListView(requireContext(),
            resources.getDimension(R.dimen.dp_120).toInt(),
            resources.getDimension(R.dimen.dp_180).toInt())
        binding.parent.addView(freeMoviesView)

        freeMoviesView?.setItemClickListener(this)
    }

    private fun addActiveOrderOrSubscription() {
        val inflater: LayoutInflater = LayoutInflater.from(requireContext())
        activeOrderViewBinding = ViewActiveOrderCardBinding.inflate(inflater)
        viewActiveOrderAdapter = ViewActiveOrderAdapter(requireContext(),childFragmentManager)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = RecyclerView.HORIZONTAL
        activeOrderViewBinding?.root?.visibility = View.VISIBLE
        activeOrderViewBinding?.recyclerView?.layoutManager = layoutManager
        activeOrderViewBinding?.recyclerView?.adapter = viewActiveOrderAdapter
        binding.parent.addView(activeOrderViewBinding?.root)
    }

    private fun addPackPromoBanner() {
        this.packPromotionBanner = PackPromotionBanner(requireContext(), contentProviderId, type)
        this.packPromotionBanner?.addOnClickListener(this)
        binding.parent.addView(this.packPromotionBanner)
    }

    private fun addContinueToWatchList() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        continueWatchBinding = ViewContinueWatchBinding.inflate(inflater)
        continueWatchListAdapter =
            ContinueWatchListAdapter(requireContext(),subscriptionViewModel.subscriptionManager, object : OnItemClickListener {
                override fun onItemCLicked(content: ContentDownload) {
                    (requireActivity() as? MainActivity)?.startPlayer(content)
                }
            })

        continueWatchBinding?.recyclerView?.adapter = continueWatchListAdapter
        binding.parent.addView(continueWatchBinding?.root)
        continueWatchBinding?.parentCardView?.visibility = View.GONE

        val cardViewMarginParams =
            continueWatchBinding?.parentCardView?.layoutParams as ViewGroup.MarginLayoutParams
        cardViewMarginParams.setMargins(0, 0, 0, resources.getDimension(R.dimen.dp_8).toInt())
        continueWatchBinding?.parentCardView?.layoutParams = cardViewMarginParams
    }

    override fun onDeviceConnectionChanged(isConnected: Boolean, hubId: String?) {
        if (isConnected) {
            binding.activeConnection.fadeVisibility(View.VISIBLE)
        } else {
            binding.activeConnection.fadeVisibility(View.GONE, 200)
        }

        offlineDownloadPromoCard?.setVisible(if (!isConnected &&
            AppUtils.shouldShowDownloadInstructions()
        )
            View.VISIBLE
        else View.GONE) //Show card 1. If Hub not connected 2.Not already downloaded

        freeMoviesView?.setHubConnected(isConnected)
        packPromotionBanner?.setHubConnected(isConnected)
        for (view in bigMovieCardViewList) view.hubConnected = isConnected
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) dismissNoInternetConnection()
        else showNoInternetConnection()
    }

    override fun isDeviceConnectionObserver(): Boolean {
        return !(type == TYPE.CLIPS)
    }

    override fun onConnectedRetailerNameRetrieved(retailerName: String) {
        binding.activeConnection.text =
            String.format(getString(R.string.connection_active_with_retailer_name), retailerName)
    }

        override fun getCurrentContentProviderId(): String {
            return this.contentProviderId
        }

        private fun View.fadeVisibility(visibility: Int, duration: Long = 400) {
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = duration
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
        this.visibility = visibility
    }

    companion object {
        private const val ARG_PARAM1 = "type"
        private const val ARG_PARAM2 = "contentProviderId"
        val CALLED_FROM = "ContentFragment"


        fun newInstance(type: TYPE, contentProviderId: String): ContentFragment {
            val fragment = ContentFragment(type, contentProviderId)
            val args = Bundle()
            args.putInt(ARG_PARAM1, type.ordinal)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onRefreshClicked() {}

    override fun onRetryClicked() {}

    fun getActiveOrderCardCTA(): View? {
        return activeOrderViewBinding?.root
    }

    fun buyPackButton(): Button? {
        return packPromotionBanner?.findViewById(R.id.pack_promo_button) as? Button
    }

    override fun onPlayClicked(content: ContentDownload) {
        (requireActivity() as? MainActivity)?.startPlayer(content)
    }

    override fun onPaidContentClicked(content: ContentDownload) {
        AppUtils.startOrderFlow(childFragmentManager,requireContext(), content.contentProviderId)
    }

    /**
     * Download Clicked
     * 1. Already connected - Begin download
     * 2. If first time download experience - Show Instructions
     * 3. If Permission not given - Request Permissions
     *      a. After permissions granted - Check connection - If still not active then Nearby stores
     *      b. If Denied - No Permission dialog
     * 4. If Permission given - Nearby stores
     */
    override fun onDownloadClicked(content: ContentDownload) {
        //Check if hub connected
        contentViewModel.downloadManager.setListener(deviceViewModel)
        if (deviceViewModel.postConnectionActivePreLoad()) {
            if(content.isMovie){
                if (contentViewModel.hasAvailableStorage(content, false)) {
                    contentViewModel.beginDownload(content.contentId,false, requireContext())
                } else {
                    AppUtils.storageFullBottomSheet(childFragmentManager, requireActivity())
                }
            }else{
                if (contentViewModel.hasAvailableStorage(content, true)) {
                    contentViewModel.beginDownload(content.contentId,true, requireContext())
                } else {
                    AppUtils.storageFullBottomSheet(childFragmentManager, requireActivity())
                }
            }

            return
        }
        //Not connected, check if is a new user with no history of download
        if (AppUtils.shouldShowDownloadInstructions()) {
            (requireActivity() as? MainActivity)?.selectTab(2)
        } else if (!AppUtils.isGPSLocationEnabled(requireActivity())) {
            showNoGPSDialogDialog()
        } else if (!isLocationPermissionsGranted()) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Go to store with movie instance - TYPE-2
            AppUtils.showNearbyStore(requireContext(),
                    BOConverter.getContentFromContentDownload(content),
                    true, null, null)
        }
    }

    override fun onCancelDownloadClicked(content: ContentDownload) {
        (requireActivity() as? MainActivity)?.cancelClicked(content)
    }

    override fun onPayAtStoreClicked() {
        if (subscriptionViewModel.hasActiveOrder(contentProviderId))
            AppUtils.startActiveOrderDialog(childFragmentManager, contentProviderId)
        else AppUtils.startOrderFlow(childFragmentManager,requireContext(), contentProviderId)
    }

    override fun onItemCLicked(content: ContentDownload) {
        val additonalAttr = HashMap<String, Any?>()
        additonalAttr[ViewAllContentActivity.EXTRA_CALLED_FROM] = CALLED_FROM
        (requireActivity() as? MainActivity)?.onItemClicked(content,
            deviceViewModel.isConnectionActive(), additonalAttr)
    }

    // called when a new ContentProvider is selected
    override fun onItemClicked(contentProviderId: String) {
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER + type.name, contentProviderId)
        SharedPreferenceStore.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER +"_${SubscriptionPackListActivity.SUBSCRIPTION}" , contentProviderId)
        this.contentProviderId = contentProviderId
        setContentProviderInfo()
        clearBigMovieCardList()
        if(type == TYPE.MOVIES){
            deviceViewModel.selectedMovieContetnProvider.postValue(contentProviderId)
        }else {
            deviceViewModel.selectedSeriesContetnProvider.postValue(contentProviderId)
        }
        contentViewModel.selectedContetnProvider.postValue(contentProviderId)
    }

    private fun clearBigMovieCardList() {
        for(view in bigMovieCardViewList){
            view.resetBigMovieCard()
        }
    }
        private fun addObserverToLogBootEvent(bigMovieCard: BigMovieCard,
                                              bootPage: BootTelemetryLogger.BootPage,
                                              bootMarker: BootTelemetryLogger.BootMarker) {
            bigMovieCard.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    bigMovieCard.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    BootTelemetryLogger.getInstance().recordPageEvent(
                        bootPage,
                        bootMarker
                    )
                }
            })
        }
}