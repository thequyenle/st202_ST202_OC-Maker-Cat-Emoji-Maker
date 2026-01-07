package com.ocmaker.pixcel.maker.ui.choose_character

import com.ocmaker.pixcel.maker.core.base.BaseAdapter
import com.ocmaker.pixcel.maker.core.extensions.gone
import com.ocmaker.pixcel.maker.core.extensions.loadImage
import com.ocmaker.pixcel.maker.core.extensions.loadImageRounded
import com.ocmaker.pixcel.maker.core.extensions.tap
import com.ocmaker.pixcel.maker.data.model.custom.CustomizeModel
import com.ocmaker.pixcel.maker.databinding.ItemChooseAvatarBinding

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