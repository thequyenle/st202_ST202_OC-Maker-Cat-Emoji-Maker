package com.oc.maker.cat.emoji.ui.random_character

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import kotlinx.coroutines.awaitAll
import android.view.ViewOutlineProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.oc.maker.cat.emoji.core.base.BaseAdapter
import com.oc.maker.cat.emoji.core.utils.key.ValueKey
import com.oc.maker.cat.emoji.data.model.custom.SuggestionModel
import com.oc.maker.cat.emoji.databinding.ItemRandomCharacterBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.oc.maker.cat.emoji.core.extensions.gone
import com.oc.maker.cat.emoji.core.extensions.invisible
import com.oc.maker.cat.emoji.core.extensions.tap
import com.oc.maker.cat.emoji.core.extensions.visible
import com.oc.maker.cat.emoji.core.helper.MediaHelper
import com.oc.maker.cat.emoji.core.utils.state.SaveState


class RandomCharacterAdapter(val context: Context) :
    BaseAdapter<SuggestionModel, ItemRandomCharacterBinding>(ItemRandomCharacterBinding::inflate) {
    var onItemClick: ((SuggestionModel) -> Unit) = {}

    // ✅ Map to track active jobs per position to cancel them when recycled
    private val activeJobs = mutableMapOf<Int, kotlinx.coroutines.Job>()

    override fun onBind(binding: ItemRandomCharacterBinding, item: SuggestionModel, position: Int) {
        binding.apply {
            Log.d("RandomAdapter", "========================================")
            Log.d("RandomAdapter", "onBind position: $position")
            Log.d("RandomAdapter", "Avatar path: ${item.avatarPath}")
            Log.d("RandomAdapter", "Selected paths count: ${item.pathSelectedList.size}")
            Log.d("RandomAdapter", "Internal random path: ${item.pathInternalRandom}")

            // ✅ Setup rounded corners for both ImageView and Shimmer
            setupRoundedImageView(imvImage, 24)
            setupRoundedImageView(sflShimmer, 24)

            // ✅ Cancel any existing job for this position
            activeJobs[position]?.cancel()

            // ✅ OPTIMIZATION: If already processed, just load the cached image
            if (item.pathInternalRandom.isNotEmpty()) {
                val cacheLoadStartTime = System.currentTimeMillis()
                Log.d(
                    "RandomAdapter",
                    "⚡ CACHED - Position $position - Loading from: ${item.pathInternalRandom}"
                )
                sflShimmer.gone()
                sflShimmer.stopShimmer()
                imvImage.visible()
                Glide.with(root)
                    .load(item.pathInternalRandom)
                    .transform(RoundedCorners(24))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable?>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            val cacheLoadTime = System.currentTimeMillis() - cacheLoadStartTime
                            Log.d(
                                "RandomAdapter",
                                "⏱️ CACHED - Position $position - Load time: ${cacheLoadTime}ms"
                            )
                            return false
                        }
                    })
                    .into(imvImage)
                root.tap { onItemClick.invoke(item) }
                return@apply
            }

            sflShimmer.visible()
            sflShimmer.startShimmer()
            imvImage.invisible()

            var width = ValueKey.WIDTH_BITMAP
            var height = ValueKey.HEIGHT_BITMAP

            val listBitmap: ArrayList<Bitmap> = arrayListOf()
            val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
                Log.e("RandomAdapter", "✗ ERROR at position $position: ${throwable.message}")
                throwable.printStackTrace()
            }
            // ✅ Store the job so we can cancel it if the view is recycled
            val job =
                CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
                    val itemTotalStartTime = System.currentTimeMillis()
                    Log.d(
                        "RandomAdapter",
                        "⏱️ ========== Position $position - Processing Started =========="
                    )

                    val job1 = async {
                        // STEP 1: Load first layer to get size
                        val firstLayerStartTime = System.currentTimeMillis()
                        Log.d(
                            "RandomAdapter",
                            "Loading first layer: ${item.pathSelectedList.first()}"
                        )
                        val bitmapDefault =
                            Glide.with(context).asBitmap().load(item.pathSelectedList.first())
                                .submit().get()
                        width = bitmapDefault.width / 2 ?: ValueKey.WIDTH_BITMAP
                        height = bitmapDefault.height / 2 ?: ValueKey.HEIGHT_BITMAP
                        val firstLayerTime = System.currentTimeMillis() - firstLayerStartTime
                        Log.d(
                            "RandomAdapter",
                            "⏱️ Position $position - First layer load time: ${firstLayerTime}ms"
                        )
                        Log.d("RandomAdapter", "Bitmap size: ${width}x${height}")

                        if (items[position].pathInternalRandom == "") {
                            // STEP 2: Load all layers
                            val allLayersStartTime = System.currentTimeMillis()
                            Log.d(
                                "RandomAdapter",
                                "Loading ${item.pathSelectedList.size} layers..."
                            )
                           val deferredBitmaps = item.pathSelectedList.mapIndexed { idx, path ->
                               async {
                                   val layerStartTime = System.currentTimeMillis()
                                   Log.d("RandomAdapter", "Loading layer $idx: $path")
                                   val bitmap =
                                       Glide.with(context)
                                           .asBitmap().
                                           load(path)
                                           .diskCacheStrategy(DiskCacheStrategy.ALL)
                                           .submit(width, height)
                                           .get()

                                   val layerTime = System.currentTimeMillis() - layerStartTime
                                   Log.d(
                                       "RandomAdapter",
                                       "⏱️ Position $position - Layer $idx load time: ${layerTime}ms"
                                   )
                                   bitmap
                               }
                           }
                            listBitmap.addAll(deferredBitmaps.awaitAll())

                            val allLayersTime = System.currentTimeMillis() - allLayersStartTime
                            Log.d(
                                "RandomAdapter",
                                "⏱️ Position $position - All ${item.pathSelectedList.size} layers load time: ${allLayersTime}ms"
                            )
                            Log.d(
                                "RandomAdapter",
                                "⏱️ Position $position - Average per layer: ${allLayersTime / item.pathSelectedList.size}ms"
                            )
                            Log.d("RandomAdapter", "✓ All layers loaded successfully")
                        }


                            return@async true
                    }


                    if (job1.await()) {

                        var savedPath = items[position].pathInternalRandom
                        if (savedPath == "") {
                            // STEP 3: Combine bitmaps

                                val combineStartTime = System.currentTimeMillis()
                                Log.d("RandomAdapter", "Creating combined bitmap...")
                                val combinedBitmap =  withContext(Dispatchers.Default)
                                { val bitmap = createBitmap(width, height)
                                val canvas = Canvas(bitmap)

                                for (i in 0 until listBitmap.size) {
                                    val bmp = listBitmap[i]
                                    val left = (width - bmp.width) / 2f
                                    val top = (height - bmp.height) / 2f
                                    canvas.drawBitmap(bmp, left, top, null)
                                }
                                val combineTime = System.currentTimeMillis() - combineStartTime
                                Log.d(
                                    "RandomAdapter",
                                    "⏱️ Position $position - Bitmap combine time: ${combineTime}ms"
                                )
                                     bitmap
                            }
                            withContext(Dispatchers.IO)
                            {// STEP 4: Save to internal storage
                                val saveStartTime = System.currentTimeMillis()
                                MediaHelper.saveBitmapToInternalStorage(
                                    context,
                                    ValueKey.RANDOM_TEMP_ALBUM,
                                    combinedBitmap
                                ).collect { state ->
                                    when (state) {
                                        is SaveState.Loading -> {
                                            Log.d(
                                                "RandomAdapter",
                                                "Saving bitmap to internal storage..."
                                            )
                                        }

                                        is SaveState.Error -> {
                                            Log.e(
                                                "RandomAdapter",
                                                "✗ Failed to save bitmap: ${state.exception.message}"
                                            )
                                        }

                                        is SaveState.Success -> {
                                            items[position].pathInternalRandom = state.path
                                            val saveTime =
                                                System.currentTimeMillis() - saveStartTime
                                            Log.d(
                                                "RandomAdapter",
                                                "⏱️ Position $position - Save to storage time: ${saveTime}ms"
                                            )
                                            Log.d("RandomAdapter", "✓ Bitmap saved: ${state.path}")
                                        }
                                    }
                                }
                            }
                        }
                        // STEP 5: Load final image into ImageView
                        withContext(Dispatchers.Main) {

                            val glideLoadStartTime = System.currentTimeMillis()
                            Log.d(
                                "RandomAdapter",
                                "Loading final image from: ${items[position].pathInternalRandom}"
                            )
                            Glide.with(root)
                                .load(items[position].pathInternalRandom)

                                .transform(RoundedCorners(24))
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                        e: GlideException?,
                                        model: Any?,
                                        target: Target<Drawable?>,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        Log.e(
                                            "RandomAdapter",
                                            "✗ Glide load FAILED at position $position: ${e?.message}"
                                        )
                                        e?.logRootCauses("RandomAdapter")
                                        sflShimmer.stopShimmer()
                                        sflShimmer.gone()
                                        return false
                                    }

                                    override fun onResourceReady(
                                        resource: Drawable,
                                        model: Any,
                                        target: Target<Drawable?>?,
                                        dataSource: DataSource,
                                        isFirstResource: Boolean
                                    ): Boolean {
                                        val glideLoadTime =
                                            System.currentTimeMillis() - glideLoadStartTime
                                        val itemTotalTime =
                                            System.currentTimeMillis() - itemTotalStartTime
                                        Log.d(
                                            "RandomAdapter",
                                            "⏱️ Position $position - Glide final load time: ${glideLoadTime}ms"
                                        )
                                        Log.d(
                                            "RandomAdapter",
                                            "⏱️ Position $position - TOTAL ITEM PROCESSING TIME: ${itemTotalTime}ms"
                                        )
                                        Log.d(
                                            "RandomAdapter",
                                            "⏱️ ========== Position $position - Processing Completed =========="
                                        )
                                        Log.d(
                                            "RandomAdapter",
                                            "✓ Image loaded successfully at position $position"
                                        )
                                        sflShimmer.stopShimmer()
                                        sflShimmer.gone()
                                        imvImage.visible()
                                        return false
                                    }
                                }).into(imvImage)
                        }
                    }

                }

            // ✅ Save the job reference
            activeJobs[position] = job

            root.tap { onItemClick.invoke(item) }
        }
    }

    // ✅ Clean up when adapter is destroyed
    fun cancelAllJobs() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
    }

    // Helper function to setup rounded corners for any View (ImageView, ShimmerFrameLayout, etc.)
    private fun setupRoundedImageView(view: View, cornerRadiusDp: Int) {
        view.apply {
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(v: View, outline: Outline) {
                    outline.setRoundRect(0, 0, v.width, v.height, cornerRadiusDp.toFloat().dpToPx())
                }
            }
        }
    }

    // Helper extension function
    private fun Float.dpToPx(): Float {
        return this * Resources.getSystem().displayMetrics.density
    }
}