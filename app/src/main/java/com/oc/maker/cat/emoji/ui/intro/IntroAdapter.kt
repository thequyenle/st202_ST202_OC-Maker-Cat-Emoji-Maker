package com.oc.maker.cat.emoji.ui.intro

import android.content.Context
import com.oc.maker.cat.emoji.core.base.BaseAdapter
import com.oc.maker.cat.emoji.core.extensions.loadImage
import com.oc.maker.cat.emoji.core.extensions.select
import com.oc.maker.cat.emoji.core.extensions.strings
import com.oc.maker.cat.emoji.data.model.IntroModel
import com.oc.maker.cat.emoji.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}