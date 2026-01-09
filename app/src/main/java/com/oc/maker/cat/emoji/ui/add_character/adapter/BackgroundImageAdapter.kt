package com.oc.maker.cat.emoji.ui.add_character.adapter

import android.R
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.facebook.shimmer.ShimmerDrawable
import com.oc.maker.cat.emoji.core.base.BaseAdapter
import com.oc.maker.cat.emoji.core.extensions.gone
import com.oc.maker.cat.emoji.core.extensions.loadImage
import com.oc.maker.cat.emoji.core.extensions.select
import com.oc.maker.cat.emoji.core.extensions.tap
import com.oc.maker.cat.emoji.core.extensions.visible
import com.oc.maker.cat.emoji.core.utils.DataLocal
import com.oc.maker.cat.emoji.data.model.SelectedModel
import com.oc.maker.cat.emoji.databinding.ItemBackgroundImageBinding

class BackgroundImageAdapter :
    BaseAdapter<SelectedModel, ItemBackgroundImageBinding>(ItemBackgroundImageBinding::inflate) {
    var onAddImageClick: (() -> Unit) = {}
    var onBackgroundImageClick: ((String, Int) -> Unit) = {_,_ ->}
    var currentSelected = -1

    override fun onBind(binding: ItemBackgroundImageBinding, item: SelectedModel, position: Int) {

        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(DataLocal.shimmer)
        }
        binding.apply {
            vFocus.isVisible = item.isSelected
            if (position == 0) {
                lnlAddItem.visible()
                imvImage.gone()
                lnlAddItem.tap(2500) { onAddImageClick.invoke() }
            } else {
                lnlAddItem.gone()
                imvImage.visible()

                if(item.isSelected)
                {
                    cvItem.strokeColor = Color.TRANSPARENT
                }else {
                    cvItem.strokeColor = ContextCompat.getColor(root.context, R.color.white)
                }

                // Load image with 8dp rounded corners
                val cornerRadiusPx = (8 * root.context.resources.displayMetrics.density).toInt()
                Glide.with(root)
                    .load(item.path)


                    .override(100)
                    .encodeQuality(30)

                    .placeholder(shimmerDrawable).error(shimmerDrawable)

                    .into(imvImage)
                imvImage.tap { onBackgroundImageClick.invoke(item.path, position) }
            }
        }
    }

    fun submitItem(position: Int, list: ArrayList<SelectedModel>){
        if (position != currentSelected){
            items.clear()
            items.addAll(list)

            notifyItemChanged(currentSelected)
            notifyItemChanged(position)

            currentSelected = position
        }
    }
}