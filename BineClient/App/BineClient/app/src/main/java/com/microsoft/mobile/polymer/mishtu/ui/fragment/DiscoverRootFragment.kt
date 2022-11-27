package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentDiscoverRootBinding
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants


class DiscoverRootFragment : Fragment(), DiscoverFragment.ServiceSelectListener {
    lateinit var binding: FragmentDiscoverRootBinding
    private var discoverFragment: DiscoverFragment? = null
    private var selectedService: BNConstants.Service = BNConstants.Service.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = androidx.databinding.DataBindingUtil.inflate(
            inflater, R.layout.fragment_discover_root, container, false)
        startDiscoverPage()
        return binding.root
    }

    fun startDiscoverPage() {
        selectedService = BNConstants.Service.NONE
        val transaction = childFragmentManager.beginTransaction()
        if (discoverFragment == null) {
            discoverFragment = DiscoverFragment(this)
        }
        if(parentFragmentManager.backStackEntryCount == 0) {
            transaction.replace(R.id.root_parent, discoverFragment!!)
            transaction.commit()
        }
        else {
            parentFragmentManager.popBackStack()
        }
    }

    private fun selectService(service:BNConstants.Service) {
        if (selectedService == service) return
        selectedService = service
        val transaction = childFragmentManager.beginTransaction()
        when(service) {
            BNConstants.Service.ENTERTAINMENT -> {
                transaction.add(R.id.root_parent, MediaLandingFragment())
            }
            BNConstants.Service.JOBS -> {
                Toast.makeText(requireContext(), "Coming shortly. We are working on this service.", Toast.LENGTH_LONG).show()
            }
            BNConstants.Service.OFFERS -> {
                Toast.makeText(requireContext(), "Coming shortly. We are working on this service.", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun selectedService(service: BNConstants.Service) {
        selectService(service)
    }

    override fun onServiceSelected(service: BNConstants.Service) {
        selectService(service)
    }
}