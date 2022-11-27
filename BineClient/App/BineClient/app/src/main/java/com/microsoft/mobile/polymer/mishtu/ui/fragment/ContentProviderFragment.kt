package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentContentProviderBinding
import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore
import com.microsoft.mobile.polymer.mishtu.ui.activity.BaseActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.MainActivity
import com.microsoft.mobile.polymer.mishtu.ui.activity.SubscriptionPackListActivity
import com.microsoft.mobile.polymer.mishtu.ui.adapter.ContentProviderAdapter
import com.microsoft.mobile.polymer.mishtu.ui.viewmodels.ContentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContentProviderFragment(var type: ContentFragment.TYPE?) : Fragment(),
    ContentProviderAdapter.onItemClickListner {


    lateinit var binding: FragmentContentProviderBinding
    private val contentViewModel by viewModels<ContentViewModel>()
    private var contentProviderAdapter: ContentProviderAdapter? = null
    var contentFragmentListner: ContentFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_content_provider, container, false)
        contentProviderAdapter =
            ContentProviderAdapter(requireContext(), this, null)
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = contentProviderAdapter

        //binding.dialogClose.visibility = View.GONE

        observeData()
        contentViewModel.getAllContentProviders()
        return binding.root
    }

    private fun observeData() {
        contentViewModel.allContentProvidersLiveData.observe(viewLifecycleOwner, {
            Log.d("CPF2", it.toString());
            contentProviderAdapter?.setData(it)
        })
    }

    override fun onItemClicked(contentProviderId: String) {
        if (type == null) {
            SharedPreferenceStore.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER + "_${SubscriptionPackListActivity.SUBSCRIPTION}", contentProviderId)
            val intent = Intent(requireContext(), SubscriptionPackListActivity::class.java)
            intent.putExtra(SubscriptionPackListActivity.CONTENTPROVIDER_ID, contentProviderId)
            return
        }
        type?.let {
            SharedPreferenceStore.getInstance().save(SharedPreferenceStore.SELECTED_CONTENT_PROVIDER + it.name, contentProviderId)
            val transaction = childFragmentManager.beginTransaction()
            val fragment = ContentFragment(it, contentProviderId)
            contentFragmentListner = fragment
            transaction.replace(R.id.container_content_provider_fragment, fragment)
            transaction.commit()
        }
    }

    fun onFragmentChanged() {
        if (contentFragmentListner != null)
            contentFragmentListner?.onFragmentChanged()
    }
}