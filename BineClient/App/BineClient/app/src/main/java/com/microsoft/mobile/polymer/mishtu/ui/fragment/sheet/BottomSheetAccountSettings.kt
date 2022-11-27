package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetAccountSettingsBinding
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetAccountSettings : BottomSheetDialogFragment() {

    private val viewModel by viewModels<ProfileViewModel>()
    lateinit var binding: BottomSheetAccountSettingsBinding

    private var shouldExitAppOnPause = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.bottom_sheet_account_settings,
            container,
            false
        )

        binding.downloadData.setOnClickListener {
            downloadDataState()
        }

        binding.deleteAccount.setOnClickListener {
            deleteAccount()
        }

        binding.close.setOnClickListener {
            dismiss()
        }
        observeData()
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        if (shouldExitAppOnPause) requireActivity().finish()
    }
    private fun observeData() {

        viewModel.loading.observe(this, {
            if (it) (requireActivity() as BaseActivity).showProgress()
            else (requireActivity() as BaseActivity).hideProgress()
        })

        viewModel.exportDataNewRequest.observe(this,{
            if(it) {
                downloadDataBottomSheet()
            }
        })
        viewModel.exportDataRequestSubmitted.observe(this,{
            if(it){
                requestSubmittedBottomSheet()
            }
        })
        viewModel.exportDataReadyToDownload.observe(this,{
            exportDataBottomSheet(it.first, it.second)
        })

        viewModel.exportDataNewRequestResponse.observe(this, {
            if (it) {
                requestSubmittedBottomSheet()
            }
        })
        viewModel.errorString.observe(this, {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        })

        viewModel.error.observe(this, {
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
        })
    }

    private fun downLoadData(exportedDataUrl: String?) {
        Toast.makeText(requireContext(), "Downloading data..", Toast.LENGTH_SHORT).show()
        requireContext().startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(exportedDataUrl)))
    }

    private fun placeNewRequest() {
        viewModel.createDataExportRequest("")
    }

    private fun downloadDataState() {
        viewModel.downloadUserDataState("")
    }

    private fun downloadDataBottomSheet() {
        BottomSheetGenericMessage(
            R.drawable.ic_download,
            getString(R.string.download_your_data),
            getString(R.string.download_dialog_text), true,
            getString(R.string.bn_download),
            null,
            { placeNewRequest() },
            null,null
        ).show(childFragmentManager,"tag")
    }

    private fun requestSubmittedBottomSheet() {
        BottomSheetGenericMessage(
            R.drawable.ic_download,
            getString(R.string.request_submitted),
            getString(R.string.download_data_response,21),
            true,
            getString(R.string.bn_okay),
            null,
            { dismiss() },
            null,null
        ).show(childFragmentManager,"tag")
    }

    private fun exportDataBottomSheet(exportedDataUrl: String?, exportedDataValidity: String?) {
        BottomSheetGenericMessage(
            R.drawable.ic_download,
            getString(R.string.export_data_ready, exportedDataValidity),
            null, true,
            getString(R.string.download_data),
            getString(R.string.place_new_request),
            { downLoadData(exportedDataUrl) },
            { placeNewRequest() },null
        ).show(childFragmentManager, "tag")
    }

    private fun deleteAccount() {
        BottomSheetGenericMessage(
            R.drawable.ic_delete,
            getString(R.string.delete_account_confirm),
            null, true,
            getString(R.string.bn_cancel),
            getString(R.string.btn_yes_proceed),
            {  },
            {
                observerDeleteAccount()
                viewModel.deleteAccount("")
            },null
        ).show(childFragmentManager, "tag")
    }

    private fun observerDeleteAccount() {
        viewModel.deleteAccountSuccess.observe(viewLifecycleOwner, {
            viewModel.logout(requireContext())
            val bottomSheet = BottomSheetGenericMessage(
                R.drawable.ic_smiley_sad,
                getString(R.string.account_delete_alert_title),
                getString(R.string.delete_account_alert_desc),
                false,
                getString(R.string.bn_okay),
                null,
                { requireActivity().finish()  },
                null,null
            )
            bottomSheet.isCancelable = false
            bottomSheet.show(childFragmentManager, "tag")
            shouldExitAppOnPause = true
        })
    }
}