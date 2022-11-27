package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.Manifest
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentDownloadBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.ui.activity.DeviceConnectFragment
import com.microsoft.mobile.polymer.mishtu.ui.activity.HubScanBaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.DownloadedListAdapter
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetGenericMessage
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.microsoft.mobile.polymer.mishtu.utils.SubscriptionManager

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DownloadFragment : DeviceConnectFragment(),
    DownloadedListAdapter.DownloadsListClickListener {

    private val contentViewModel by viewModels<ContentViewModel>()

    private lateinit var binding: FragmentDownloadBinding

    private lateinit var queuedAdapter: DownloadedListAdapter
    private lateinit var downloadedAdapter: DownloadedListAdapter
    private var viewEpisodes = true
    @Inject lateinit var subscriptionManager: SubscriptionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_download, container, false
        )

        queuedAdapter = DownloadedListAdapter(requireContext(), this, subscriptionManager)
        binding.downloadingRecyclerView.adapter = queuedAdapter

        downloadedAdapter = DownloadedListAdapter(requireContext(), this, subscriptionManager)
        binding.downloadedRecyclerView.adapter = downloadedAdapter

        observeData()

        //if (AppUtils.shouldShowDownloadInstructions()) {
        setInstructionView()
        //}

        // This button would show only if not connected
        binding.goToStoreButton.setOnClickListener {
            //If permission granted go to nearby stores Type-3
            if (!AppUtils.isGPSLocationEnabled(requireActivity())) {
                showNoGPSDialogDialog()
            }
            else if (isLocationPermissionsGranted()) {
                AppUtils.showNearbyStore(requireContext(), null, true, null, null)
            }
            else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.buttonBack.setOnClickListener {
            binding.buttonBack.visibility = View.GONE
            binding.downloadsTitle.text = requireContext().getString(R.string.your_downloads)
            binding.downloadedTitle.visibility = View.VISIBLE
            viewEpisodes = true
            contentViewModel.selectedEpisode.postValue(null)
        }


        return binding.root
    }

    private fun observeData() {
        contentViewModel.selectedEpisode.postValue(null)
        binding.buttonBack.visibility = View.GONE
        binding.downloadsTitle.text = requireContext().getString(R.string.your_downloads)
        binding.downloadedTitle.visibility = View.VISIBLE
        contentViewModel.downloadedContent.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.noDownloadsLayout.visibility = View.GONE
                binding.goToStoreButton.visibility = View.GONE
                binding.downloadedLayout.visibility = View.VISIBLE
                downloadedAdapter.setDataList(if(it[0].isMovie) it else it.sortedBy { con-> con.episode?.lowercase()?.replace("episode", "")?.toInt() }, viewEpisodes)
            } else {
                binding.downloadedLayout.visibility = View.GONE
                setNoDownloadScreen()
            }
        }
        contentViewModel.queuedAndOtherContent.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                binding.noDownloadsLayout.visibility = View.GONE
                binding.goToStoreButton.visibility = View.GONE
                binding.downloadingLayout.visibility = View.VISIBLE
                Log.d("QueueAdapter ", "it = ${it[0].title} ${it[0].episode}")
                queuedAdapter.setDataList( it.sortedBy { con->  con.episode?.lowercase()?.replace("episode", "")?.replace("","0")?.toInt() }, false)
            } else {
                binding.downloadingLayout.visibility = View.GONE
            }
        })

/*
        contentViewModel.activeSubscription.observe(this, { sub ->
            sub?.let {
                val days = it.getDaysToExpire()
                when {
                    it.isExpired() -> {
                        binding.downloadsPackWillExpireLayout.visibility = View.GONE
                        binding.downloadsPackExpiredLayout.visibility = View.VISIBLE
                        binding.downloadsPackExpiredLayout.findViewById<TextView>(R.id.home_card_subs_pack_title).text =
                            String.format(getString(R.string.bn_pack_rs), it.subscription.price)
                        downloadedAdapter.setExpired(true)
                        binding.downloadsPackExpiredLayout.findViewById<Button>(R.id.home_card_subs_renew)?.setOnClickListener {
                            AppUtils.startOrderFlow(childFragmentManager, "SHOW_ALL")
                        }
                    }
                    days <= 5 -> {
                        binding.downloadsPackWillExpireLayout.visibility = View.VISIBLE
                        binding.downloadsPackExpiredLayout.visibility = View.GONE
                        binding.downloadsPackWillExpireAlert.text =
                            if (days > 0L) String.format(getString(R.string.bn_expire_in_x_days), it.getDaysToExpire())
                            else getString(R.string.bn_expire_today)
                    }
                    else -> {
                        binding.downloadsPackExpiredLayout.visibility = View.GONE
                        binding.downloadsPackWillExpireLayout.visibility = View.GONE
                    }
                }
            }
        })
*/
    }

    override fun onSeriesClicked(content: ContentDownload) {
        binding.buttonBack.visibility = View.VISIBLE

        binding.downloadsTitle.text = content.additionalTitle2 ?: content.title
        binding.downloadedTitle.visibility = View.GONE
        contentViewModel.selectedEpisode.postValue(content)
        viewEpisodes = false

    }

    override fun onPlayClicked(content: ContentDownload) {
        (requireActivity() as? MainActivity)?.startPlayer(content)
    }

    override fun onDeleteClicked(content: ContentDownload, deleteAllEpisodes: Boolean) {
        val titleMsg = if(deleteAllEpisodes) getString(R.string.confirm_delete_series) else  getString(R.string.confirm_delete)
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.ic_delete,
                titleMsg,
                null, false,
                getString(R.string.btn_no),
                getString(R.string.btn_yes),
                null,
                {
                    contentViewModel.deleteDownload(requireContext(), content, deleteAllEpisodes)
                }, null
            )
        bottomSheetFragment.isCancelable = true
        bottomSheetFragment.show(
            childFragmentManager,
            bottomSheetFragment.tag
        )
    }

    override fun onCancelClicked(content: ContentDownload) {
        val bottomSheetFragment =
            BottomSheetGenericMessage(
                R.drawable.cross,
                getString(R.string.confirm_cancel),
                null,false,
                getString(R.string.btn_no),
                getString(R.string.btn_yes),
                null,
                {
                    contentViewModel.cancelDownload(requireContext(), content)
                },
                null
                )
        bottomSheetFragment.isCancelable = true
        bottomSheetFragment.show(
            childFragmentManager,
            bottomSheetFragment.tag
        )
    }


    override fun onDeviceConnectionChanged(isConnected: Boolean, hubId: String?) {
        if (isConnected) {
            contentViewModel.setConnectedHubId(hubId!!)
        }
    }
    override fun onNetworkConnectionChanged(isConnected: Boolean) {}

    override fun isDeviceConnectionObserver(): Boolean {
        return true
    }
    override fun onConnectedRetailerNameRetrieved(retailerName: String){}
    override fun getCurrentContentProviderId(): String {
        return ""
    }


    private fun setInstructionView() {
        binding.noDownloadsLayout.visibility = View.VISIBLE
        binding.goToStoreButton.visibility = View.VISIBLE
        binding.noDownloadsStep1Text.text = getSpannable(getString(R.string.go_to_nearby_store_and_take_assistance), getString(R.string.go_to_nearby_store))
        binding.noDownloadsStep2Text.text = getSpannable(getString(R.string.turn_on_wifi), getString(R.string.phone_wifi))
        binding.noDownloadsStep3Text.text = getSpannable(getString(R.string.join_a_network_with_name_nearme_xxxxxx), getString(R.string.nearme_xxxxxx))
        binding.noDownloadsStep4Text.text = getSpannable(getString(R.string.download_films_for_free_at_the_store), getString(R.string.download))
    }

    private fun getSpannable(string: String, boldText: String): SpannableString {
        val txtSpannable = SpannableString(string)
        if (boldText.length > string.length) return txtSpannable
        val boldSpan = StyleSpan(Typeface.BOLD)
        if (string.indexOf(boldText) != -1) {
            txtSpannable.setSpan(
                boldSpan,
                string.indexOf(boldText),
                string.indexOf(boldText) + boldText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            txtSpannable.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.button_color
                    )
                ),
                string.indexOf(boldText),
                string.indexOf(boldText) + boldText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return txtSpannable
    }

    private fun setNoDownloadScreen() {
        (requireActivity() as? HubScanBaseActivity)?.getLastStatus()?.let {
            if (it == HubScanBaseActivity.HubScanStatus.NO_HUB_EXIST) {
                binding.downloadsNoHubs.visibility = View.VISIBLE
                binding.goToStoreButton.visibility = View.GONE

                binding.cardInfoText.text = AppUtils.getOrangeTextSpannable(requireContext(),
                    getString(R.string.free_download_not_available_watch_films),
                    getString(R.string.free_download))

                binding.watchFilms.setOnClickListener {
                    (requireActivity() as? MainActivity)?.startFilmsTab()
                }
            }
            else {
                binding.downloadsNoHubs.visibility = View.GONE
            }
        }
    }
}