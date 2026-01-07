package com.oc.maker.cat.emoji.ui.choose_character

import com.oc.maker.cat.emoji.core.base.BaseAdapter
import com.oc.maker.cat.emoji.core.extensions.gone
import com.oc.maker.cat.emoji.core.extensions.loadImage
import com.oc.maker.cat.emoji.core.extensions.loadImageRounded
import com.oc.maker.cat.emoji.core.extensions.tap
import com.oc.maker.cat.emoji.data.model.custom.CustomizeModel
import com.oc.maker.cat.emoji.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}
    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            // Set rounded corners for shimmer
            setupRoundedView(sflShimmer, 24)

            loadImageRounded(item.avatar, imvImage, cornerRadius = 24, onDismissLoading = {
                sflShimmer.stopShimmer()
                sflShimmer.gone()
            })
            root.tap { onItemClick.invoke(position) }
        }
    }

    private fun setupRoundedView(view: android.view.View, cornerRadiusDp: Int) {
        view.apply {
            clipToOutline = true
            outlineProvider = object : android.view.ViewOutlineProvider() {
                override fun getOutline(v: android.view.View, outline: android.graphics.Outline) {
                    outline.setRoundRect(0, 0, v.width, v.height, cornerRadiusDp * android.content.res.Resources.getSystem().displayMetrics.density)
                }
            }
        }
    }
}