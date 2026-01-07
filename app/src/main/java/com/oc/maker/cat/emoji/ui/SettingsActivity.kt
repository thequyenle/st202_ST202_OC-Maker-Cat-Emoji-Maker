package com.oc.maker.cat.emoji.ui

import android.view.LayoutInflater
import com.oc.maker.cat.emoji.R
import com.oc.maker.cat.emoji.core.base.BaseActivity
import com.oc.maker.cat.emoji.core.extensions.gone
import com.oc.maker.cat.emoji.core.extensions.handleBackLeftToRight
import com.oc.maker.cat.emoji.core.extensions.policy
import com.oc.maker.cat.emoji.core.extensions.select
import com.oc.maker.cat.emoji.core.extensions.setImageActionBar
import com.oc.maker.cat.emoji.core.extensions.setTextActionBar
import com.oc.maker.cat.emoji.core.extensions.shareApp
import com.oc.maker.cat.emoji.core.extensions.startIntentRightToLeft
import com.oc.maker.cat.emoji.core.extensions.visible
import com.oc.maker.cat.emoji.core.utils.key.IntentKey
import com.oc.maker.cat.emoji.core.utils.state.RateState
import com.oc.maker.cat.emoji.databinding.ActivitySettingsBinding
import com.oc.maker.cat.emoji.ui.language.LanguageActivity
import com.oc.maker.cat.emoji.core.extensions.tap
import com.oc.maker.cat.emoji.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.actionBar.tvCenter.visible()
        initRate()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { handleBackLeftToRight() }
            btnLang.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShareApp.tap(1500) { shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(this@SettingsActivity, sharePreference){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { policy() }
        }
    }

    override fun initText() {
        binding.tvPrivacy.select()
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.settings))
        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}