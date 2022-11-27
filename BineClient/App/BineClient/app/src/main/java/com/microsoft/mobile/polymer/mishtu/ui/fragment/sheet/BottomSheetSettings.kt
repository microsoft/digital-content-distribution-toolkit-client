package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.BottomSheetSettingsBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.telemetry.ClientLogging
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.DeviceViewModel
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants
import dagger.hilt.android.AndroidEntryPoint
import java.util.*


@AndroidEntryPoint
class BottomSheetSettings: BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetSettingsBinding
    lateinit var bineSharedPreferenceStore: SharedPreferenceStore

    private val viewModel by activityViewModels<DeviceViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)

        bineSharedPreferenceStore = SharedPreferenceStore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_settings, container,false)

        binding.editTextIP.setText(BNConstants.getDeviceIP())

        var ssid = BNConstants.DEVICE_SSID
        if(!bineSharedPreferenceStore.get(BNConstants.KEY_EXTRA_DEVICE_SSID).isNullOrEmpty()) {
            ssid = bineSharedPreferenceStore.get(BNConstants.KEY_EXTRA_DEVICE_SSID).toString()
        }
        binding.editTextSSID.setText(ssid)

        binding.butttonSave.setOnClickListener { onSaveClicked() }
        binding.clientLog.setOnClickListener {
            showClientLogs()
        }
        return binding.root
    }

    private fun showClientLogs() {
        //val intent = Intent(Intent.ACTION_GET_CONTENT)
       // intent.setDataAndType(Uri.fromFile(ClientLogging.outputFile),"*/*")
        //startActivity(intent)

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("mishtu@cloudpoint.co.in"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mishtu Logs ${Date()}")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Mishtu Logs here, PFA")

        val file = ClientLogging.outputFile
        if (!file.exists() || !file.canRead()) {
            return
        }
        val uri = FileProvider.getUriForFile(requireContext(), context?.getApplicationContext()?.getPackageName() + ".provider", file)
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"))
    }

    private fun onSaveClicked() {
        if (isValidIP()) {
            bineSharedPreferenceStore.save(BNConstants.KEY_EXTRA_DEVICE_IP, binding.editTextIP.text.trim().toString())
        }

        bineSharedPreferenceStore.save(BNConstants.KEY_EXTRA_DEVICE_SSID, binding.editTextSSID.text.trim().toString())
        viewModel.isHubDeviceConnected()
        Toast.makeText(requireContext(), "Settings Saved!!", Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun isValidIP(): Boolean {
        if (binding.editTextIP.text.trim().isEmpty() || binding.editTextIP.text.trim().toString() == BNConstants.DEVICE_HOSTNAME) return true

        if (!binding.editTextIP.text.matches(Regex("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?"))) {
            Toast.makeText(requireContext(), "Invalid IP", Toast.LENGTH_SHORT).show()
            return false
        } else {
            val splits = binding.editTextIP.text.split(".").toTypedArray()
            for (split in splits) {
                if (split.toInt() > 255) {
                    Toast.makeText(requireContext(), "Invalid IP", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
        }
        return true
    }
}