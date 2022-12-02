// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.mobile.polymer.mishtu.ui.fragment.sheet

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.FragmentAvatarBottomSheetBinding
import com.microsoft.mobile.polymer.mishtu.databinding.ListItemAvatarListBinding
import com.microsoft.mobile.polymer.mishtu.utils.BNConstants

import com.microsoft.mobile.polymer.mishtu.storage.SharedPreferenceStore

class AvatarBottomSheetFragment(private val avatarSelectListener: AvatarSelectListener?) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAvatarBottomSheetBinding
    private var selectedAvatarIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAvatarBottomSheetBinding.inflate(inflater, container, false)
        val sharedPreferences = SharedPreferenceStore.getInstance()

        val indexString = sharedPreferences.get(SharedPreferenceStore.KEY_AVATAR_INDEX)
        if (indexString.isNullOrEmpty()) {
            selectedAvatarIndex = -1
            binding.avatarSubmit.text = getString(R.string.add_photo)
        }
        else {
            selectedAvatarIndex = indexString.toInt()
            binding.avatarSubmit.text = getString(R.string.update_photo)
        }

        binding.close.setOnClickListener { dismiss() }

        binding.avatarSubmit.isEnabled = false
        binding.avatarSubmit.setOnClickListener {
            sharedPreferences.save(SharedPreferenceStore.KEY_AVATAR_INDEX, selectedAvatarIndex.toString())
            avatarSelectListener?.onAvatarUpdated(selectedAvatarIndex)
            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.avatarList.layoutManager = GridLayoutManager(context, 3)
        binding.avatarList.adapter = AvatarAdapter()
    }

    private inner class ViewHolder(binding: ListItemAvatarListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val imageView: ImageView = binding.imageView
        val selectedLayout: RelativeLayout = binding.selectedItemLayout
        val parentView: ConstraintLayout = binding.avatarParentLayout
    }

    private inner class AvatarAdapter :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            return ViewHolder(
                ListItemAvatarListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val id: Int = requireContext().resources.getIdentifier(BNConstants.Avatars[holder.absoluteAdapterPosition], "drawable",
                requireContext().packageName)
            holder.imageView.setImageDrawable(ContextCompat.getDrawable(requireContext(), id))

            if (holder.absoluteAdapterPosition == selectedAvatarIndex) {
                holder.selectedLayout.visibility = View.VISIBLE
            }
            else {
                holder.selectedLayout.visibility = View.GONE
            }

            holder.parentView.setOnClickListener {
                val oldSelected = selectedAvatarIndex
                if (selectedAvatarIndex == holder.absoluteAdapterPosition) {
                    selectedAvatarIndex = -1
                    notifyItemChanged(oldSelected)
                    binding.avatarSubmit.isEnabled = false
                }
                else {
                    selectedAvatarIndex = holder.absoluteAdapterPosition
                    notifyItemChanged(oldSelected)
                    notifyItemChanged(selectedAvatarIndex)
                    binding.avatarSubmit.isEnabled = true
                }
            }
        }

        override fun getItemCount(): Int {
            return BNConstants.Avatars.size
        }
    }

    interface AvatarSelectListener {
        fun onAvatarUpdated(index: Int)
    }
}