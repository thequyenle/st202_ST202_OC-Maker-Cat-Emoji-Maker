package com.oc.maker.cat.emoji.dialog

import android.app.Activity
import android.graphics.drawable.AnimationDrawable
import com.oc.maker.cat.emoji.R
import com.oc.maker.cat.emoji.core.base.BaseDialog
import com.oc.maker.cat.emoji.core.extensions.setBackgroundConnerSmooth
import com.oc.maker.cat.emoji.databinding.DialogLoadingBinding

class WaitingDialog(val context: Activity) :
    BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    override fun initView() {
        // Start loading animation for dot

    }

    override fun initAction() {}

    override fun onDismissListener() {}

}