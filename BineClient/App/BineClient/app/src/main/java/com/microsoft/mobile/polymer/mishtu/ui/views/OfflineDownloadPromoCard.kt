package com.microsoft.mobile.polymer.mishtu.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.microsoft.mobile.polymer.mishtu.R
import com.microsoft.mobile.polymer.mishtu.databinding.ViewOfflineDownloadCardBinding
import com.microsoft.mobile.polymer.mishtu.storage.entities.ContentDownload
import com.microsoft.mobile.polymer.mishtu.utils.AppUtils

class OfflineDownloadPromoCard @JvmOverloads constructor(context: Context,
                                                         attrs: AttributeSet? = null,
                                                         defStyle: Int = 0):
    FrameLayout(context, attrs, defStyle)  {

    private var binding: ViewOfflineDownloadCardBinding
    private var backgroundRendered: Boolean = false

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ViewOfflineDownloadCardBinding.inflate(inflater,this, true)

        binding.cardInfoText.text = AppUtils.getOrangeTextSpannable(context,
            context.getString(R.string.find_a_nearest_shop_and_save_data),
            context.getString(R.string.up_to_100))

        binding.buttonGoToStore.setOnClickListener {
            AppUtils.showNearbyStore(context, null, true, null, null)
        }
    }

    fun setVisible(visibility: Int) {
        binding.offlineCardParent.visibility = visibility
    }

    fun setBackground(content: List<ContentDownload>) {
        if (content.isEmpty() || backgroundRendered) return
        var index = 0
        val iSize = content.size-1
        while (index < 6) {
            val c = if (index >= iSize) content[iSize % index] else content[index]
            val imageUrl = c.getThumbnailImage() ?: c.getThumbnailLandscapeImage()

            val imageView = when(index) {
                0 -> binding.cardBgImage1
                1 -> binding.cardBgImage2
                2 -> binding.cardBgImage3
                3 -> binding.cardBgImage4
                4 -> binding.cardBgImage5
                5 -> binding.cardBgImage6
                else -> {binding.cardBgImage1}
            }
            Glide.with(this)
                .load(imageUrl)
                .dontAnimate()
                .into(imageView)
            index++
        }
        backgroundRendered = true
    }
}