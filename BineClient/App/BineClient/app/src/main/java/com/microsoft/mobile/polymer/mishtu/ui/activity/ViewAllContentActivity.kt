package com.microsoft.mobile.polymer.mishtu.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.microsoft.mobile.polymer.mishtu.R

import com.microsoft.mobile.polymer.mishtu.databinding.ActivityContentViewAllBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.ui.TooltipDisplayHelper
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ViewAllContentAdapter
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetContentDetails
import com.microsoft.mobile.polymer.mishtu.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewAllContentActivity : BinePlayerListenerActivity(), OnItemClickListener {
    private lateinit var binding: ActivityContentViewAllBinding
    private var title: String? = ""
    private lateinit var adapter: ViewAllContentAdapter
    private var isFree = true
    private var isPackActive: Boolean = false
    private var isHubConnected: Boolean = false
    private var backgroundRendered: Boolean = false
    private lateinit var contentDownload: ContentDownload
    private lateinit var calledFrom: String
    private var dismisBottomSheet: Any? = null
    private val pageCount = 200
    @Inject  lateinit var subscriptionManager: SubscriptionManager

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_HUB_CONNECTED = "hubConnected"
        const val EXTRA_IS_FREE = "contentList"
        const val EXTRA_BOOLEAN_ACTIVE_SUBS = "hasActiveSubscription"
        const val EXTRA_CALLED_FROM = "calledFrom"
        const val EXTRA_DISMIS_BOTTOMSHEET = "dismisBottomSheet"
        const val EXTRA_CONTENT = "content"
        const val CALLED_FROM = "ViewAllContentActivity"


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_content_view_all)
        isPackActive = intent.getBooleanExtra(EXTRA_BOOLEAN_ACTIVE_SUBS, false)
        isHubConnected = intent.getBooleanExtra(EXTRA_HUB_CONNECTED, false)
        setSupportActionBar(binding.toolBar)

        title = intent.getStringExtra(EXTRA_TITLE)
        isFree = intent.getBooleanExtra(EXTRA_IS_FREE, true)
        contentDownload = intent.getSerializableExtra(EXTRA_CONTENT) as ContentDownload
        calledFrom = intent.getStringExtra(EXTRA_CALLED_FROM) as String
        dismisBottomSheet = intent.getSerializableExtra(EXTRA_DISMIS_BOTTOMSHEET)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            if (calledFromContentDetailBottomSheet()) {
                it.title =
                    if (!contentDownload.additionalTitle1.isNullOrEmpty()) "${contentDownload.additionalTitle1}" else "${contentDownload.title}" + " • " + "${contentDownload.season}"
            } else {
                it.title = title
            }
        }
        binding.closeButton.setOnClickListener {
            finish()
        }
        binding.buttonSelectPack.setOnClickListener {
            AppUtils.startOrderFlow(supportFragmentManager, this, contentDownload.contentProviderId)
        }
        if (!isPackActive) {
            if (calledFromContentDetailBottomSheet()) {
                binding.buttonSelectPackSeries.visibility = View.VISIBLE
                binding.buttonSelectPackSeries.setOnClickListener {
                    AppUtils.startOrderFlow(supportFragmentManager, this,
                        contentDownload.contentProviderId)
                }
            } else {
                binding.backgroundDark.visibility = View.VISIBLE
                binding.contentProviderContainer.visibility = View.VISIBLE
                binding.toolBar.visibility = View.GONE
                AppUtils.loadImage(this,
                    AppUtils.getContentProviderSquareLogoURL(contentDownload.contentProviderId),
                    binding.logo)
                if(!contentDownload.isMovie) {
                    binding.moviesInPack.text = getString(R.string.serial_in_pack)
                }
            }
        }

        adapter = ViewAllContentAdapter(this, this, subscriptionManager, isHubConnected, calledFrom)
        var spanCount = 3
        if (calledFromContentDetailBottomSheet()) {
            spanCount = 2
        }
        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, spanCount)
        val includeEdge = false
        binding.recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount,
            AppUtils.dpToPx(10.0f, resources),
            includeEdge))
        binding.recyclerView.layoutManager = mLayoutManager
        binding.recyclerView.adapter = adapter

        observeData()
    }

    override fun teachingScreenName(): TooltipDisplayHelper.FTUScreen {
        return TooltipDisplayHelper.FTUScreen.None
    }

    override fun teachingContainer(): Int {
        return 0
    }

    override fun completionCallback(): TooltipDisplayHelper.TooltipDisplayCompletion? {
        return null
    }

    override fun shouldShowOnResume(): Boolean {
        return false
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    /*
    * calledFromBottomSheet -> if true then donot open the same BottomSheet again
    * */
    override fun onItemCLicked(content: ContentDownload) {
        val additonalAttr = HashMap<String, Any?>()
        //BottomsheetContentDetails -> viewAllEpisodes -> onItemClicked -> close the activity and play or download
        if (shouldCloseActivity()) {
            finish()
        }
        //if ViewAll already calledFrom the BSCD, then dont change the called from coz on
        //itemClicked() we will startPlayer coz it will be episode
        calledFrom =
            if (calledFromContentDetailBottomSheet()) calledFrom else ViewAllContentActivity.CALLED_FROM
        additonalAttr[EXTRA_CALLED_FROM] = calledFrom
        onItemClicked(content, deviceViewModel.isConnectionActive(), additonalAttr)
    }

    private fun observeData() {
        if (contentDownload.isMovie) {
            observeMovieData()
        } else {
            observeSeriesData()
        }
        deviceViewModel.postConnectionActivePreLoad()
    }


    private fun observeMovieData() {
        if (isFree) {
            deviceViewModel.getConnectedDevice()?.let {
                deviceViewModel.getHubFreeContentLiveData(it.id,
                    pageCount,
                    1,
                    contentDownload.contentProviderId).observe(this, observer)
            } ?: run {
                deviceViewModel.freeContentLiveData(pageCount, 1, contentDownload.contentProviderId)
                    .observe(this, observer)
            }
        } else {
            deviceViewModel.getConnectedDevice()?.let {
                deviceViewModel.getHubPaidContentLiveData(it.id,
                    pageCount,
                    1,
                    contentDownload.contentProviderId).observe(this, observer)
            } ?: run {
                deviceViewModel.paidContentLiveData(pageCount, 1, contentDownload.contentProviderId)
                    .observe(this, observer)
            }
        }
    }

    private fun observeSeriesData() {
        if (calledFromContentDetailBottomSheet()) {
            deviceViewModel.getAllEpisodesOfSeason(contentDownload.name!!,
                contentDownload.season!!,
                contentDownload.contentProviderId).observe(this, observer)
        } else {
            if (isFree) {
                deviceViewModel.getConnectedDevice()?.let {
                    deviceViewModel.getHubFreeContentLiveData(it.id,
                        pageCount,
                        0,
                        contentDownload.contentProviderId).observe(this, observer)
                } ?: run {
                    deviceViewModel.freeContentLiveData(pageCount,
                        0,
                        contentDownload.contentProviderId)
                        .observe(this, observer)
                }
            } else {
                deviceViewModel.getConnectedDevice()?.let {
                    deviceViewModel.getHubPaidContentLiveData(it.id,
                        pageCount,
                        0,
                        contentDownload.contentProviderId).observe(this, observer)
                } ?: run {
                    deviceViewModel.paidContentLiveData(pageCount,
                        0,
                        contentDownload.contentProviderId)
                        .observe(this, observer)
                }
            }
        }
    }

    private val observer = Observer<List<ContentDownload>> {
        Log.d("AppU_1", it.size.toString())
        if(it.isNotEmpty()) {
            adapter.submitList(if(it[0].isMovie) it else it.sortedBy { con-> con.episode?.lowercase()?.replace("episode", "")?.toInt() })
        }
        if (calledFromContentDetailBottomSheet())
            setBackgroundImages(it)
    }

    private fun shouldCloseActivity() =
        calledFromContentDetailBottomSheet() && isPackActive

    private fun calledFromContentDetailBottomSheet() =
        !calledFrom.isNullOrEmpty() && calledFrom.equals(BottomSheetContentDetails.CALLED_FROM)

    private fun setBackgroundImages(content: List<ContentDownload>) {
        if (content.isEmpty() || backgroundRendered) return
        var index = 0
        val iSize = content.size - 1
        while (index < 6) {
            val c = if (index >= iSize) content[iSize % index] else content[index]
            val imageUrl = c.getThumbnailImage() ?: c.getThumbnailLandscapeImage()

            val imageView = when (index) {
                0 -> binding.cardBgImage1
                1 -> binding.cardBgImage2
                2 -> binding.cardBgImage3
                3 -> binding.cardBgImage4
                4 -> binding.cardBgImage5
                5 -> binding.cardBgImage6
                else -> {
                    binding.cardBgImage1
                }
            }
            Glide.with(this)
                .load(imageUrl)
                .dontAnimate()
                .into(imageView)
            index++
        }
        backgroundRendered = true
    }
}