package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ServiceGridAdapter
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentDiscoverBinding
import com.microsoft.mobile.polymer.mishtu.telemetry.AnalyticsLogger
import com.microsoft.mobile.polymer.mishtu.telemetry.Screen
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants

class DiscoverFragment(private var serviceSelectListener: ServiceSelectListener?) : Fragment(), ServiceGridAdapter.ItemClickListener {

    lateinit var binding: FragmentDiscoverBinding

    constructor() : this(null) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = androidx.databinding.DataBindingUtil.inflate(
            inflater, R.layout.fragment_discover, container, false)

        binding.discoverRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(activity)

        val adapter = ServiceGridAdapter(requireContext(), this)
        binding.discoverRecyclerView.adapter = adapter

        binding.discoverNotificationMenu.setOnClickListener {
           /* childFragmentManager?.let { it1 ->
                NotificationDialogFragment().newInstance(
                    NotificationActionHandler(requireActivity(), it1)
                ).show(it1, null)
            }*/
        }
        AnalyticsLogger.getInstance().logScreenView(Screen.DISCOVER)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(serviceSelectListener: ServiceSelectListener) = DiscoverFragment(serviceSelectListener)
    }

    override fun onServiceClicked(service: String, position: Int) {
        when(position) {
            0 -> serviceSelectListener?.onServiceSelected(BNConstants.Service.ENTERTAINMENT)
            1 -> serviceSelectListener?.onServiceSelected(BNConstants.Service.JOBS)
            2 -> serviceSelectListener?.onServiceSelected(BNConstants.Service.OFFERS)
        }
    }

    interface ServiceSelectListener {
        fun onServiceSelected(service: BNConstants.Service)
    }
}