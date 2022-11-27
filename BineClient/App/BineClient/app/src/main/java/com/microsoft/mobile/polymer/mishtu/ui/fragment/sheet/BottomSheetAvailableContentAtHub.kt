package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetAvailableContentAtHubBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.ui.adapter.TopContentAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DeviceViewModel
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetAvailableContentAtHub(private val deviceID: String, private val contentProviderId: String?) : BottomSheetDialogFragment(),
    TopContentAdapter.OnMovieClickListener {
    lateinit var binding: BottomSheetAvailableContentAtHubBinding
    lateinit var progressDialog: AlertDialog
    val deviceViewModel by activityViewModels<DeviceViewModel>()
    val contentViewModel by activityViewModels<ContentViewModel>()
    var contentAtDevice = ArrayList<ContentDownload>()
    lateinit var adapter: TopContentAdapter
    private lateinit var layoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        setProgressDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.bottom_sheet_available_content_at_hub,
            container,
            false)
        adapter = TopContentAdapter(requireContext(),
            this,
            requireContext().resources.getDimension(R.dimen.dp_300).toInt(),
            requireContext().resources.getDimension(R.dimen.dp_420).toInt(),
            20,
            isGridView = false,
            isViewAllVisible = false,
            titleVisible = false,
            gradientVisible = true,
            playVisible = false
        )
        binding.recyclerViewMoreMovies.adapter = adapter
        layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding.recyclerViewMoreMovies.layoutManager = layoutManager

        binding.dialogClose.setOnClickListener {
            dismiss()
        }
        if(contentProviderId.isNullOrEmpty()) {
            contentViewModel.allContentProvidersLiveData.observe(viewLifecycleOwner,{
                it?.let {
                    deviceViewModel.getContentForDevice(deviceID,it.map { con-> con.id })
                }
            })
        }else {
            val cpList = ArrayList<String>()
            cpList.add(contentProviderId)
            deviceViewModel.getContentForDevice(deviceID, cpList)
        }
        observeData()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun observeData() {
        deviceViewModel.loading.observe(this, {
            if (it) showProgress()
            else hideProgress()
        })

        var isContentAvailable = false
        deviceViewModel.deviceContent.observe(viewLifecycleOwner, {
            contentAtDevice.clear()
            if (!it.isNullOrEmpty()) {
                isContentAvailable = true
                for(con in it) {
                    contentAtDevice.add(BOConverter.getContentDownloadFromContent(con))
                }
                adapter.submitList(contentAtDevice.distinctBy { content -> content.name })
            }
        })
        if(!isContentAvailable) {
            deviceViewModel.error.observe(viewLifecycleOwner, {
                Toast.makeText(requireContext(), "Error: $it", Toast.LENGTH_SHORT).show()
            })
        }
    }

    override fun onMovieClicked(content: ContentDownload) {
        Log.d("BottomSheetContent", "onMovieClicked")
    }

    override fun onViewAllCLicked(content: ContentDownload) {
        Log.d("BottomSheetContent", "ViewAllClicked")
    }

    private fun setProgressDialog() {
        val llPadding = 30
        val linearLayout = LinearLayout(requireContext())
        linearLayout.orientation = LinearLayout.HORIZONTAL
        linearLayout.setPadding(llPadding, llPadding, llPadding, llPadding)
        linearLayout.gravity = Gravity.CENTER
        var layoutParams1 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams1.gravity = Gravity.CENTER
        linearLayout.layoutParams = layoutParams1
        val progressBar = ProgressBar(requireContext())
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = layoutParams1
        layoutParams1 = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams1.gravity = Gravity.CENTER
        val tvText = TextView(requireContext())
        tvText.text = getString(R.string.please_wait)
        tvText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        tvText.textSize = 18f
        tvText.typeface = ResourcesCompat.getFont(requireContext(), R.font.mukta_medium)
        tvText.layoutParams = layoutParams1
        linearLayout.addView(progressBar)
        linearLayout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.transparent_light))
        linearLayout.addView(tvText)
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        builder.setView(linearLayout)
        progressDialog = builder.create()

        progressDialog.window?.let {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(it.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }
    fun showProgress() {
        if (!progressDialog.isShowing) progressDialog.show()
    }

    fun hideProgress() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }
}