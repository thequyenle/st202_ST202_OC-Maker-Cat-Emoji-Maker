package com.oc.maker.cat.emoji.ui.view

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.lvt.ads.util.Admob
import com.oc.maker.cat.emoji.R
import com.oc.maker.cat.emoji.core.base.BaseActivity
import com.oc.maker.cat.emoji.core.extensions.checkPermissions
import com.oc.maker.cat.emoji.core.extensions.goToSettings
import com.oc.maker.cat.emoji.core.extensions.gone
import com.oc.maker.cat.emoji.core.extensions.handleBackLeftToRight
import com.oc.maker.cat.emoji.core.extensions.hideNavigation
import com.oc.maker.cat.emoji.core.extensions.invisible
import com.oc.maker.cat.emoji.core.extensions.loadImage
import com.oc.maker.cat.emoji.core.extensions.loadImageFromFile
import com.oc.maker.cat.emoji.core.extensions.loadImageRounded
import com.oc.maker.cat.emoji.core.extensions.loadNativeCollabAds
import com.oc.maker.cat.emoji.core.extensions.requestPermission
import com.oc.maker.cat.emoji.core.extensions.select
import com.oc.maker.cat.emoji.core.extensions.setImageActionBar
import com.oc.maker.cat.emoji.core.extensions.setTextActionBar
import com.oc.maker.cat.emoji.core.extensions.showInterAll
import com.oc.maker.cat.emoji.core.extensions.tap

import com.oc.maker.cat.emoji.core.extensions.startIntentWithClearTop
import com.oc.maker.cat.emoji.core.extensions.visible
import com.oc.maker.cat.emoji.core.helper.LanguageHelper
import com.oc.maker.cat.emoji.core.utils.key.IntentKey
import com.oc.maker.cat.emoji.core.utils.key.RequestKey
import com.oc.maker.cat.emoji.core.utils.key.ValueKey
import com.oc.maker.cat.emoji.core.utils.state.HandleState
import com.oc.maker.cat.emoji.databinding.ActivityViewBinding
import com.oc.maker.cat.emoji.dialog.YesNoDialog
import com.oc.maker.cat.emoji.ui.home.HomeActivity
import com.oc.maker.cat.emoji.ui.my_creation.MyCreationActivity
import com.oc.maker.cat.emoji.core.extensions.startIntentRightToLeft
import com.oc.maker.cat.emoji.core.extensions.strings
import com.oc.maker.cat.emoji.core.helper.UnitHelper
import com.oc.maker.cat.emoji.core.utils.DataLocal
import com.oc.maker.cat.emoji.ui.customize.CustomizeCharacterActivity
import com.oc.maker.cat.emoji.ui.home.DataViewModel
import com.oc.maker.cat.emoji.ui.my_creation.fragment.MyAvatarFragment
import com.oc.maker.cat.emoji.ui.my_creation.view_model.MyAvatarViewModel
import com.oc.maker.cat.emoji.ui.permission.PermissionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private val viewModel: ViewViewModel by viewModels()
    private val myAvatarViewModel: MyAvatarViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    override fun initView() {
        binding.actionBar.tvCenter.select()

        dataViewModel.ensureData(this)
        viewModel.setPath(intent.getStringExtra(IntentKey.INTENT_KEY)!!)
        viewModel.updateStatusFrom(intent.getIntExtra(IntentKey.STATUS_KEY, ValueKey.AVATAR_TYPE))
        viewModel.setType(intent.getIntExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW))

        if (viewModel.typeUI.value == ValueKey.TYPE_VIEW) {

            val params = binding.cvImageWhite.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(
                16.dpToPx(), // left margin
                36.dpToPx(), // top margin
                16.dpToPx(), // right margin
                36.dpToPx()  // bottom margin
            )
            binding.cvImageWhite.layoutParams = params



            binding.frameBg.setImageResource(R.drawable.frame_bg_view)
            binding.includeLayoutBottom.apply {
                tvWhatsapp.apply {
                    setTextColor(Color.parseColor("#2993FF"))
                }
                tvTelegram.apply {
                    setTextColor(Color.parseColor("#2993FF"))
                }
            }

        }
        // Set bg_btn_bottom for both buttons
        setButtonBackgrounds()
    }

    private fun setButtonBackgrounds() {
        binding.includeLayoutBottom.apply {
            // Left button (Whatsapp)
            btnWhatsapp.setBackgroundResource(R.drawable.bg_btn_bottom)
            btnWhatsapp.setPadding(0, 0, 0, 0)
            val paramsLeft =
                btnWhatsapp.layoutParams as? androidx.appcompat.widget.LinearLayoutCompat.LayoutParams
            paramsLeft?.apply {
                height = UnitHelper.dpToPx(this@ViewActivity, 48f).toInt()
                marginEnd = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                marginStart = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                btnWhatsapp.layoutParams = this
            }
            // Hide the CardView with rounded corners
            val cardViewLeft = btnWhatsapp.getChildAt(0) as? androidx.cardview.widget.CardView
            cardViewLeft?.gone()
            // Hide WhatsApp icon
            val lnlInLeft = btnWhatsapp.getChildAt(1) as? ViewGroup
            lnlInLeft?.getChildAt(0)?.gone()

            // Update tvWhatsapp text properties
            tvWhatsapp.textSize = 16f
            tvWhatsapp.setTypeface(
                ResourcesCompat.getFont(
                    this@ViewActivity,
                    R.font.baloobhaijaan_regular
                )
            )

            // Right button (Telegram)
            btnTelegram.setBackgroundResource(R.drawable.bg_btn_bottom)
            btnTelegram.setPadding(0, 0, 0, 0)
            val paramsRight =
                btnTelegram.layoutParams as? androidx.appcompat.widget.LinearLayoutCompat.LayoutParams
            paramsRight?.apply {
                height = UnitHelper.dpToPx(this@ViewActivity, 48f).toInt()
                marginStart = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                marginEnd = UnitHelper.dpToPx(this@ViewActivity, 4f).toInt()
                btnTelegram.layoutParams = this
            }
            // Hide the CardView with rounded corners
            val cardViewRight = btnTelegram.getChildAt(0) as? androidx.cardview.widget.CardView
            cardViewRight?.gone()
            // Hide Telegram icon
            val lnlInRight = btnTelegram.getChildAt(1) as? ViewGroup
            lnlInRight?.getChildAt(0)?.gone()

            tvTelegram.textSize = 16f
            tvTelegram.setTypeface(
                ResourcesCompat.getFont(
                    this@ViewActivity,
                    R.font.baloobhaijaan_regular
                )
            )

            // Hide download button
        }
    }

//    @SuppressLint("CheckResult")
//    fun loadImageFromFile(path: String) {
//        val file = File(path)
//        val request = Glide.with(context)
//            .load(file)
//
//        request.signature(ObjectKey(file.lastModified()))
//
//        request.into(this)
//    }

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val newPath =
                    result.data?.getStringExtra("NEW_PATH") ?: return@registerForActivityResult
                viewModel.setPath(newPath)
                binding.imvImage.loadImageFromFile(newPath) // hoặc loadImage(...) của bạn
            }
        }


    fun loadImageCallBack(
        path: Any,
        imageView: ImageView,
        onShowLoading: (() -> Unit)? = null,
        onDismissLoading: (() -> Unit)? = null
    ) {
        onShowLoading?.invoke()
        Glide.with(imageView.context).load(path).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<Drawable?>,
                isFirstResource: Boolean
            ): Boolean {
                onDismissLoading?.invoke()
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable?>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                onDismissLoading?.invoke()
                return false
            }
        }).into(imageView)
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
    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pathInternal.collect { path ->

                        if (viewModel.typeUI.value == ValueKey.TYPE_VIEW) {
                            setupRoundedView(binding.sflShimmer, 24)

//                            binding.sflShimmer.visible()       // Hiện shimmer view
//                            binding.sflShimmer.startShimmer()
                            loadImageRounded(
                                path = path,
                                imageView = binding.imvImage,
                                cornerRadius = 44,
                                onDismissLoading = {
                                    binding.sflShimmer.stopShimmer()
                                    binding.sflShimmer.gone()
                                }
                            )

                        } else{
                            binding.sflShimmer.gone()

                            loadImage(this@ViewActivity, path, binding.imvImage)
                        }
                    }
                }
                launch {
                    viewModel.typeUI.collect { type ->
                        if (type != -1) {
                            when (type) {
                                ValueKey.TYPE_VIEW -> setUpViewUI()
                                else -> setUpSuccessUI()
                            }
                        }
                    }
                }
            }

        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnActionBarLeft.tap { handleBack() }
                btnActionBarRight.tap(500) { handleActionBarRight() }
                //btnActionBarNextRight.tap { handleEditClick(viewModel.pathInternal.value) }
                btnShare.tap(1000) { viewModel.shareFiles(this@ViewActivity) }
            }

            // Access buttons from included layout_bottom
            includeLayoutBottom.btnWhatsapp.tap(3390) { handleBottomBarLeft() }
            includeLayoutBottom.btnTelegram.tap(500) { handleBottomBarRight() }
        }
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
        }
    }

    fun pxToDpInt(context: Context, px: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    private fun setUpViewUI() {
        binding.apply {

              nativeAds.gone()
              flNativeCollab.visible()

            actionBar.apply {
                // setImageActionBar(btnActionBarRight, R.drawable.ic_delete_view)
                // setImageActionBar(btnActionBarNextToRight, R.drawable.ic_edit_2)
                setTextActionBar(tvCenter, getString(R.string.my_creation))


                setImageActionBar(btnActionBarNextRight, R.drawable.ic_edit_view)

                // Hide delete icon when coming from design section
                if (viewModel.statusFrom == ValueKey.MY_DESIGN_TYPE || viewModel.statusFrom == ValueKey.AVATAR_TYPE) {
                    btnActionBarNextRight.invisible()

                }

                setImageActionBar(btnActionBarRight, R.drawable.ic_delete)
                // Hide btnShare in view mode
                btnShare.gone()

            }
//            cvImage.apply {
//                radius = 16f
//                strokeWidth = 2
//                strokeColor = getColor(R.color.red_BA)
//            }

            val params = cvImage.layoutParams as ConstraintLayout.LayoutParams
            cvImage.layoutParams = params



            tvSuccess.gone()

            if (viewModel.statusFrom == ValueKey.MY_DESIGN_TYPE) {
                includeLayoutBottom.tvWhatsapp.text = strings(R.string.share)
            } else {
                val params = binding.imvImage.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(20.dpToPx(), 20.dpToPx(), 20.dpToPx(), 20.dpToPx())
                binding.imvImage.layoutParams = params
                includeLayoutBottom.tvWhatsapp.text = strings(R.string.edit)

            }

            includeLayoutBottom.tvWhatsapp.select()

            includeLayoutBottom.tvTelegram.text = strings(R.string.download)
            includeLayoutBottom.tvTelegram.select()
        }
    }


    private fun setUpSuccessUI() {
        binding.apply {
            nativeAds.visible()
            flNativeCollab.invisible()
            actionBar.apply {
                // Hide center text and imgCenter
                tvCenter.gone()
                tvCenter.setText(getString(R.string.successfully))
                imgCenter.gone()

                // Hide left and next right buttons
                btnActionBarLeft.visible()
                btnActionBarNextRight.gone()

                binding.imvImage.apply {

                    translationY = -50f // đơn vị px


                }
                btnActionBarCenter.visible()
                // Show and configure btnShare as home button
                btnShare.apply {
                    visible()
                    setImageResource(R.drawable.ic_share_ss)
                    tap(1000) {
                        viewModel.shareFiles(this@ViewActivity)
                    }
                }
                btnActionBarCenter.setImageResource(R.drawable.ic_home_ss)
                btnActionBarCenter.tap {
                    showInterAll {
                        startIntentWithClearTop(HomeActivity::class.java)
                    }
                }
            }

            val params = cvImage.layoutParams as ConstraintLayout.LayoutParams
            cvImage.layoutParams = params

            tvSuccess.visible()
            tvSuccess.select()

            includeLayoutBottom.tvWhatsapp.text = strings(R.string.go_to_creation)
            includeLayoutBottom.tvWhatsapp.select()
            includeLayoutBottom.tvWhatsapp.setTextColor(Color.parseColor("#2993FF"))


            includeLayoutBottom.tvTelegram.text = strings(R.string.download)
            includeLayoutBottom.tvTelegram.select()
            includeLayoutBottom.tvTelegram.setTextColor(Color.parseColor("#2993FF"))

        }
    }

    override fun initAds() {
        initNativeCollab()
    }

    fun initNativeCollab() {
        if (viewModel.typeUI.value == ValueKey.TYPE_VIEW) {
            loadNativeCollabAds(R.string.native_cl_detail, binding.flNativeCollab)
        } else{
            Admob.getInstance().loadNativeAd(
                this,
                getString(R.string.native_success),
                binding.nativeAds,
                R.layout.ads_native_big_btn_top
            )
        }
    }

    private fun handleActionBarRight() {
        when (viewModel.typeUI.value) {
            ValueKey.TYPE_VIEW -> {
                handleDelete()
            }

            else -> {
                showInterAll { startIntentWithClearTop(HomeActivity::class.java) }
            }
        }
    }

    private fun handleBottomBarLeft() {
        when (viewModel.typeUI.value) {
            ValueKey.TYPE_VIEW -> {
                if (viewModel.statusFrom == ValueKey.MY_DESIGN_TYPE) {

                    viewModel.shareFiles(this@ViewActivity)
                } else {
                   showInterAll {   handleEditClick(viewModel.pathInternal.value) }
                }
            }

            else -> {
                showInterAll { startIntentRightToLeft(MyCreationActivity::class.java, true) }
            }
        }
    }

    private fun handleBottomBarRight() {
        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleDownload()
        } else {
            val perms = permissionViewModel.getStoragePermissions()
            if (checkPermissions(perms)) {
                handleDownload()
            } else if (permissionViewModel.needGoToSettings(sharePreference, true)) {
                goToSettings()
            } else {
                requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
            }
        }
    }

    private fun handleDownload() {
        lifecycleScope.launch {
            viewModel.downloadFiles(this@ViewActivity).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading()
                        showToast(R.string.download_success)
                    }

                    else -> {
                        dismissLoading()
                        showToast(R.string.download_failed_please_try_again_later)
                    }

                }
            }
        }

    }

    private fun handleDelete() {
        val dialog =
            YesNoDialog(this, R.string.delete, R.string.are_you_sure_want_to_delete_this_item)
        LanguageHelper.setLocale(this)
        dialog.show()
        dialog.binding.btnYes.setText(R.string.delete)

        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.onYesClick = {
            dialog.dismiss()
            lifecycleScope.launch {
                viewModel.deleteFile(this@ViewActivity, viewModel.pathInternal.value)
                    .collect { state ->
                        when (state) {
                            HandleState.LOADING -> showLoading()
                            HandleState.SUCCESS -> {
                                dismissLoading()
                                resetMyCreationSelectionMode()

                                // ✅ Trả kết quả về màn trước (MyAvatarFragment/MyCreationActivity)
                                setResult(Activity.RESULT_OK, Intent().apply {
                                    putExtra("DELETED_PATH", viewModel.pathInternal.value)
                                })
                                finish()
                            }

                            else -> {
                                dismissLoading()
                                showToast(R.string.delete_failed_please_try_again)
                            }
                        }
                    }
            }
        }
    }

    private fun handleBack() {
        if (viewModel.typeUI.value == ValueKey.TYPE_VIEW) {
            resetMyCreationSelectionMode()
            showInterAll{handleBackLeftToRight()}

        }
        else{
        handleBackLeftToRight()
    }
    }

    private fun resetMyCreationSelectionMode() {
        // Reset selection mode in MyCreationActivity before going back
        val myCreationActivity = MyCreationActivity.getInstance()
        if (myCreationActivity != null) {
            android.util.Log.d("ViewActivity", "Resetting selection mode in MyCreationActivity")

            // Reset the fragment's selection state first
            val designFragment =
                myCreationActivity.supportFragmentManager.findFragmentByTag("MyDesignFragment")
            if (designFragment is com.oc.maker.cat.emoji.ui.my_creation.fragment.MyDesignFragment) {
                designFragment.resetSelectionMode()
            }

            val avatarFragment =
                myCreationActivity.supportFragmentManager.findFragmentByTag("MyAvatarFragment")
            if (avatarFragment is MyAvatarFragment) {
                avatarFragment.resetSelectionMode()
            }

            // Exit selection mode in activity
            myCreationActivity.exitSelectionMode()
        } else {
            android.util.Log.w(
                "ViewActivity",
                "MyCreationActivity instance not found - unable to reset selection mode"
            )
        }
    }

    private fun handleEditClick(pathInternal: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            showLoading()
            myAvatarViewModel.editItem(this@ViewActivity, pathInternal, dataViewModel.allData.value)

            withContext(Dispatchers.Main) {
                dismissLoading()

                myAvatarViewModel.checkDataInternet(this@ViewActivity) {
                    val intent =
                        Intent(this@ViewActivity, CustomizeCharacterActivity::class.java).apply {
                            putExtra(IntentKey.INTENT_KEY, myAvatarViewModel.positionCharacter)
                            putExtra(IntentKey.STATUS_FROM_KEY, ValueKey.EDIT)
                        }

                    // ✅ Chỉ launch 1 lần
                    editLauncher.launch(intent)

                    // ✅ Apply animation (nếu muốn)
                    overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right)
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionViewModel.updateStorageGranted(sharePreference, true)
                handleDownload()
            } else {
                permissionViewModel.updateStorageGranted(sharePreference, false)
            }
        }
    }

    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBack()
    }

}
