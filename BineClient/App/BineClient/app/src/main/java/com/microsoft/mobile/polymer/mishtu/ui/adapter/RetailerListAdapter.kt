// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.adapter

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ItemRetailerDetailsBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.Content
import com.microsoft.mobile.polymer.mishtu.ui.activity.NearbyStoresActivity
import com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet.BottomSheetAvailableContentAtHub
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils
import com.msr.bine_sdk.cloud.models.RetailerDistance
import kotlin.collections.ArrayList

//import com.msr.bine_sdk.cloud.model.RetailerDistance.Retailer


class RetailerListAdapter(
    private val context: Context,
    private val activity: Activity,
    private var retailerList: ArrayList<RetailerDistance>,
    private var retailerWithContentList: ArrayList<Pair<RetailerDistance, String>>,
    private val contentProviderId: String?,
    private val childFragmentManager: FragmentManager,
) : RecyclerView.Adapter<RetailerListAdapter.ViewHolder>() {

    private var selectedIndex = 0
    private var content: Content? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemRetailerDetailsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_retailer_details,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    fun setRetailerList(retailers: List<RetailerDistance>) {
        this.retailerList.addAll(retailers)
        notifyDataSetChanged()
    }

    fun setRetailerList(retailers: List<Pair<RetailerDistance, String>>, content: Content) {
        this.retailerWithContentList.addAll(retailers)
        this.content = content
        notifyDataSetChanged()
    }

    fun getSelectedRetailer(): RetailerDistance {
        if (content == null) {
            return retailerList[selectedIndex]
        } else {
            return retailerWithContentList[selectedIndex].first
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (content == null) {
            val dataModel = retailerList[position]
            holder.bind(dataModel, position, null)
        } else {
            val dataModel = (retailerWithContentList)[position]
            holder.bind(dataModel.first, position, dataModel.second)
        }

    }

    override fun getItemCount(): Int {
        if (content == null) {
            return (retailerList).size
        } else {
            return (retailerWithContentList).size
        }
    }

    inner class ViewHolder(var binding: ItemRetailerDetailsBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind(slotModel: RetailerDistance, position: Int, contentCount: String?) {
            binding.retailerName.text = slotModel.retailer.name
            binding.retailerDistance.text =
                String.format(context.resources.getString(R.string.bn_pack_distance),
                    slotModel.distanceMeters.toInt())
            binding.retailerAddress.text =
                "${slotModel.retailer.address.address1}, ${slotModel.retailer.address.address2}, " +
                        "${slotModel.retailer.address.address3}, ${slotModel.retailer.address.city}, ${slotModel.retailer.address.state} (${slotModel.retailer.address.pinCode})"
            binding.parent.setOnClickListener {
                selectedIndex = position
                notifyDataSetChanged()
            }
            if (content != null && contentCount?.toInt() != null) {
                val imageUrl = if(!content?.getThumbnailLandscapeImage().isNullOrEmpty()) content?.getThumbnailLandscapeImage() else content?.getThumbnailImage()
                AppUtils.loadImageWithRoundedCorners(context, imageUrl, binding.movieThumbnail)
                binding.moreMovies.text =
                    context.getString(R.string.watch_more_movies, contentCount.toInt()-1)
                binding.moreMoviesIcon.visibility = View.GONE
                val mlp = binding.moreMovies.layoutParams as ViewGroup.MarginLayoutParams
                mlp.setMargins(context.resources.getDimension(R.dimen.dp_20).toInt(), 0, 0, 0)

            } else {
                binding.movieThumbnail.visibility = View.GONE
                binding.plus.visibility = View.GONE
                binding.moreMovies.text = context.getString(R.string.available_movies)
                val mlp = binding.moreMovies.layoutParams as ViewGroup.MarginLayoutParams
                mlp.setMargins(0, context.resources.getDimension(R.dimen.dp_12).toInt(), 0, 0)

                val mlpRetailerName = binding.retailerName.layoutParams as ViewGroup.MarginLayoutParams
                mlpRetailerName.setMargins(0,context.resources.getDimension(R.dimen.dp_8).toInt(),0,0)

                if (slotModel.retailer.deviceAssignments.isNullOrEmpty()) {
                    binding.contentAvailableLayout.visibility = View.GONE
                    binding.moreMovies.visibility = View.GONE
                    binding.moreMoviesIcon.visibility = View.GONE
                }
            }

            binding.retailerDetailGoToStore.setOnClickListener {
                (activity as NearbyStoresActivity).startStoreDetails(slotModel.retailer)
            }

            binding.retailerCall.setOnClickListener {
                openWhatsAppConversation(slotModel.retailer.phoneNumber, "")
            }

            binding.moreMovies.setOnClickListener {
                if (!slotModel.retailer.deviceAssignments.isNullOrEmpty()) {
                    val bottomSheetAvailableContentAtHub =
                        BottomSheetAvailableContentAtHub(slotModel.retailer.deviceAssignments[0].deviceId,contentProviderId)
                    bottomSheetAvailableContentAtHub.show(childFragmentManager,
                        bottomSheetAvailableContentAtHub.tag)
                }
            }
        }
    }

    private fun openWhatsAppConversation(number: String, message: String?) {
        try {
            var number = number
            number = number.replace(" ", "").replace("+", "")
            val sendIntent = Intent("android.intent.action.MAIN")
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, message)
            sendIntent.component = ComponentName("com.whatsapp", "com.whatsapp.Conversation")
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(number) + "@s.whatsapp.net")
            context.startActivity(sendIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context,
                context.resources.getString(R.string.whatsapp_not_installed),
                Toast.LENGTH_SHORT).show()
        }
    }

}