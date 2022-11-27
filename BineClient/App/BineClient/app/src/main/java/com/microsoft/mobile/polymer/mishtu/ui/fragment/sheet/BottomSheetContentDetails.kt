package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.graphics.Outline
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetContentDetailsBinding
import com.microsoft.mobile.polymer.mishtu.databinding.LayoutContentInfoBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.ui.activity.ViewAllContentActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.SeriesAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentDetailsViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DeviceViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import com.microsoft.mobile.polymer.mishtu.utils.BinePlayerListenerActivity
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class BottomSheetContentDetails(private val content: ContentDownload, private val additionalAttrMap: Map<String,Any?>?) :
    BottomSheetContentShortInfo(BOConverter.getContentFromContentDownload(content)),
    SeriesAdapter.OnMovieClickListener {

    private val viewModel by viewModels<ContentDetailsViewModel>()
    private val deviceViewModel by viewModels<DeviceViewModel>()
    private lateinit var seriesAdapter: SeriesAdapter

    companion object{
    val CALLED_FROM = "BottomSheetContentDetails"
    }


    lateinit var bottomSheetContentBinding: BottomSheetContentDetailsBinding
    private var player: SimpleExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    @Inject lateinit var subscriptionManager: SubscriptionManager

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
        bottomSheetContentBinding =
            DataBindingUtil.inflate(inflater,
                R.layout.bottom_sheet_content_details,
                container,
                false)
        initView(bottomSheetContentBinding.contentInfo)
        setupPlayCapability()

        bottomSheetContentBinding.dialogClose.setOnClickListener {
            dismiss()
        }

        bottomSheetContentBinding.buttonShare.setOnClickListener {
            AppUtils.shareContent(content.title,
                content.contentId,
                content.contentProviderId,
                AnalyticsLogger.LongForm,
                requireContext())
        }

        AnalyticsLogger.getInstance().logScreenView(Screen.MEDIA_DETAILS, content.title)
        bottomSheetContentBinding.contentTitle.text =
            (if(content.isMovie) content.title else content.additionalTitle1 ?: content.title) +" • " + content.yearOfRelease
        if (!content.isMovie) {
            showSeasonInfo()
            observeData2(arrayOfSeasons[0])
        }
        return bottomSheetContentBinding.root
    }

    private fun observeData2(seasonName: String) {
        content.name?.let {
            deviceViewModel.getAllEpisodesOfSeason(it, seasonName, content.contentProviderId)
                .observe(viewLifecycleOwner) { it1 ->
                    if (!it1.isNullOrEmpty()) {
                        seriesAdapter.submitList(it1.sortedBy { con -> con.episode?.lowercase()?.replace("episode", "")?.toInt() })
                        setSeasonData(bottomSheetContentBinding.contentInfo, it1[0])
                    }
                }
        }

    }

    private fun setSeasonData(layoutContentInfoBinding: LayoutContentInfoBinding, episode1: ContentDownload) {
        releasePlayer()
        initializePlayer(episode1)

        //Setting Season's title on spinner change which we store in episode1.additionalTitle1 of every season
        bottomSheetContentBinding.contentTitle.text =
            episode1.additionalTitle1 ?: episode1.title +" • " + episode1.yearOfRelease

        //description of Season on spinner change which we store in episode1.additionalDescription1 of every season
        layoutContentInfoBinding.contentDetailsDescription.text = episode1.additionalDescription1 ?: episode1.longDescription
        layoutContentInfoBinding.contentDetailsDescription.text =
            if (content.isMovie)
                getSpannable("${getString(R.string.about_movie)}: ${layoutContentInfoBinding.contentDetailsDescription.text}",
                    "${getString(R.string.about_movie)}:")
            else
                getSpannable("${getString(R.string.story)}: ${layoutContentInfoBinding.contentDetailsDescription.text}",
                    "${getString(R.string.story)}:")
    }

    private var arrayOfSeasons = arrayListOf<String>()


    private fun showSeasonInfo() {
        arrayOfSeasons.add("${getString(R.string.season_listing)}1")

        bottomSheetContentBinding.contentInfo.seriesDetailsContainer.visibility = View.VISIBLE
        content.name?.let {
            deviceViewModel.getAllOfSeasonOfSeries(it, content.contentProviderId)
                .observe(this) { seasons ->
                    bottomSheetContentBinding.contentInfo.spinnerSeason.adapter =
                        ArrayAdapter(requireContext(),
                            R.layout.support_simple_spinner_dropdown_item,
                            seasons.map { ep -> ep.replace("season", getString(R.string.season_listing)) }
                        )


                    bottomSheetContentBinding.contentInfo.spinnerSeason.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long,
                            ) {
                                observeData2(seasons[position])
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }

                        }

                }
        }


        val attributeMap = HashMap<String, Any>()
        attributeMap["width"] = requireContext().resources.getDimension(R.dimen.dp_175).toInt()
        attributeMap["height"] = requireContext().resources.getDimension(R.dimen.dp_173).toInt()
        attributeMap["footerHeight"] =
            requireContext().resources.getDimension(R.dimen.dp_100).toInt()
        attributeMap["marginStart"] = requireContext().resources.getDimension(R.dimen.dp_10).toInt()
        attributeMap["isGridView"] = false
        attributeMap["isViewAllVisible"] = true


        seriesAdapter = SeriesAdapter(requireContext(), this, attributeMap, subscriptionManager)
        if(!additionalAttrMap.isNullOrEmpty()){
            seriesAdapter.hubConnected = additionalAttrMap[ViewAllContentActivity.EXTRA_HUB_CONNECTED] as? Boolean ?: false
        }

        /*TopContentAdapter(
            requireContext(),
            this,
            requireContext().resources.getDimension(R.dimen.dp_175).toInt(),
            requireContext().resources.getDimension(R.dimen.dp_98).toInt(),
            requireContext().resources.getDimension(R.dimen.dp_12).toInt(),
            isGridView = false,
            isViewAllVisible = true,
            titleVisible = true,
            gradientVisible = true,
            playVisible = false
        )*/


        bottomSheetContentBinding.contentInfo.recyclerView.adapter = seriesAdapter

        bottomSheetContentBinding.contentInfo.recyclerView.layoutManager =
            LinearLayoutManager(context,
                RecyclerView.HORIZONTAL, false)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer(content)
        }
    }

    override fun onResume() {
        super.onResume()
        if ((Util.SDK_INT < 24 || player == null)) {
            initializePlayer(content)
        }
    }

    private fun initializePlayer(content: ContentDownload) {
        if (content.hasTrailers()) {
            bottomSheetContentBinding.banner.visibility = View.GONE
            player = SimpleExoPlayer.Builder(requireContext())
                .build()
                .also { exoPlayer ->
                    bottomSheetContentBinding.playerView.player = exoPlayer
                    val mediaItem = MediaItem.fromUri(content.getTrailer())
                    exoPlayer.setMediaItem(mediaItem)
                }

            player?.playWhenReady = playWhenReady
            player?.seekTo(currentWindow, playbackPosition)
            player?.prepare()
        } else {
            val imageUrl = content.getThumbnailLandscapeImage() ?: content.getThumbnailImage()

            bottomSheetContentBinding.playerView.visibility = View.INVISIBLE
            Glide.with(this)
                .load(imageUrl)
                .dontAnimate()
                .into(bottomSheetContentBinding.banner)
        }

        val mViewOutlineProvider: ViewOutlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                val cornerRadiusDP = 16f
                val cornerRadius = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    cornerRadiusDP,
                    resources.displayMetrics
                )
                outline.setRoundRect(
                    0, 0, view.width,
                    (view.height + cornerRadius).toInt(), cornerRadius
                )
            }
        }
        bottomSheetContentBinding.contentDetailsParent.outlineProvider = mViewOutlineProvider
        bottomSheetContentBinding.contentDetailsParent.clipToOutline = true
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }


    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentWindowIndex
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }

    private fun setupPlayCapability() {
        Log.d("BSCD","content = ${content.title} and ${content.free}")
        if (content.free) {
            bottomSheetContentBinding.contentDetailsPlay.text = if(content.isMovie) getString(R.string.watch_for_free) else getString(R.string.watch_episode1)
            bottomSheetContentBinding.contentDetailsPlay.setOnClickListener {
                (requireActivity() as? BinePlayerListenerActivity)?.startPlayer(content)
                dismissSheet()
            }
        } else {
            /*observeData()
            viewModel.getActiveOrderAndPackDetails(content.contentProviderId)*/

            if(subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)){
                bottomSheetContentBinding.contentDetailsPlay.text = getString(R.string.btn_play)
                bottomSheetContentBinding.contentDetailsPlay.setOnClickListener {
                    (requireActivity() as? BinePlayerListenerActivity)?.startPlayer(content)
                    dismissSheet()
                }
            }else {
                bottomSheetContentBinding.contentDetailsPlay.text = getString(R.string.bn_select_a_pack)
                bottomSheetContentBinding.contentDetailsPlay.setOnClickListener {
                    AppUtils.startOrderFlow(childFragmentManager, requireContext(), content.contentProviderId)
                }
            }
        }
    }

    /*private fun observeData() {
        viewModel.activePackLiveData.observe(this, {
            bottomSheetContentBinding.contentDetailsPlay.text = getString(R.string.btn_play)
            bottomSheetContentBinding.contentDetailsPlay.setOnClickListener {
                (requireActivity() as? BinePlayerListenerActivity)?.startPlayer(content)
                dismissSheet()
            }
        })

    }*/

    private fun dismissSheet() {
        Handler(Looper.getMainLooper()).postDelayed({ dismiss() }, 200)
    }



    override fun onMovieClicked(content: ContentDownload) {
        val map = HashMap<String, Any?>()
        map[ViewAllContentActivity.EXTRA_CALLED_FROM] = CALLED_FROM
        (requireActivity() as BinePlayerListenerActivity).onItemClicked(content,false,map)
        dismiss()
    }

    override fun onViewAllCLicked(
        content: ContentDownload
    ) {

        val map = HashMap<String, Any?>()
        map[ViewAllContentActivity.EXTRA_CONTENT] = content
        map[ViewAllContentActivity.EXTRA_CALLED_FROM] = CALLED_FROM
            val hubConnected =
                (additionalAttrMap?.get(ViewAllContentActivity.EXTRA_HUB_CONNECTED) ?: false) as Boolean
            val hasActiveSubs = if(content.free) true else subscriptionManager.hasValidSubscriptionForContent(content.contentProviderId, content.contentId)

            AppUtils.startViewAllContentActivity(
                requireContext(),
                isFree = false,
                hubConnected = hubConnected,
                title = requireContext().getString(R.string.bn_paid_movies),
                activePack = hasActiveSubs, map)

    }

}