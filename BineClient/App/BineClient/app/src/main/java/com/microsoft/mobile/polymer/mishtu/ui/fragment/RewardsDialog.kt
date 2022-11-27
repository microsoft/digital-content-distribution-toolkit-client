package com.microsoft.mobile.polymer.mishtu.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import androidx.fragment.app.Fragment
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.DialogViewScratchCardBinding
import com.microsoft.mobile.polymer.mishtu.utils.BOConverter


class RewardsDialog(private val eventType: BOConverter.EventType,
                    private val firstOperand: Int, private val contentProviderId: String?,
                    val dialogCloseListener: DialogCloseListener): Fragment() {

    lateinit var binding: DialogViewScratchCardBinding

    companion object {
        fun newInstance(
            eventType: BOConverter.EventType, firstOperand: Int,
            contentProviderId: String?,
            dialogCloseListener: DialogCloseListener
        ): RewardsDialog {
            return RewardsDialog(eventType, firstOperand,contentProviderId, dialogCloseListener)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = androidx.databinding.DataBindingUtil.inflate(
            inflater, R.layout.dialog_view_scratch_card, container, false)

        binding.rewardsConfettiTop.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in_repeat))
        Handler(Looper.getMainLooper()).postDelayed({
            if (context != null) {
                binding.rewardsConfettiBottom.startAnimation(
                    AnimationUtils.loadAnimation(
                        requireContext(),
                        R.anim.fade_in_repeat
                    )
                )
            }
        }, 2000)

        binding.handIndicator.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.scratch_hand_animation))

        binding.scratchCoins.text = firstOperand.toString()

        binding.rewardsCloseButton.setOnClickListener {
            binding.scratchView.clear()
            Handler(Looper.getMainLooper()).postDelayed({
                disappearCard()
            },1000)
        }

        binding.rewardDescription.text = when(eventType) {
            BOConverter.EventType.FIRST_SIGN_IN -> getString(R.string.rewards_app_install)
            BOConverter.EventType.APP_ONCE_OPEN -> getString(R.string.rewards_daily_open)
            BOConverter.EventType.CONTENT_STREAMED -> getString(R.string.rewards_content_streamed)
            BOConverter.EventType.ON_BOARDING_RATING -> getString(R.string.rewards_retailer_rating)
            BOConverter.EventType.CONSUMER_INCOME_ORDER_COMPLETED -> getString(R.string.rewards_order_complete)
            BOConverter.EventType.DOWNLOAD_COMPLETE -> getString(R.string.rewards_download_complete)
            else ->  getString(R.string.rewards_app_install)
        }
        return binding.root
    }

    private fun disappearCard() {
        if (!isAdded) return
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
        animation.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                binding.rewardsParent.visibility = ViewGroup.GONE
                dialogCloseListener.onCloseClick()
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
        binding.cardScratchedLayout.startAnimation(animation)
    }

    override fun onDetach() {
        binding.rewardsConfettiTop.clearAnimation()
        binding.rewardsConfettiBottom.clearAnimation()
        super.onDetach()
    }

    override fun onStop() {
        binding.rewardsConfettiTop.clearAnimation()
        binding.rewardsConfettiBottom.clearAnimation()
        super.onStop()
    }
    interface DialogCloseListener {
        fun onCloseClick()
    }
}