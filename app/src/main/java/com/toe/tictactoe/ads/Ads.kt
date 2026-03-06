package com.toe.tictactoe.ads

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(modifier: Modifier = Modifier, unitId: String) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = {
            val displayMetrics = context.resources.displayMetrics
            val widthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()
            val adaptiveSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
            AdView(context).apply {
                adUnitId = unitId
                setAdSize(adaptiveSize)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

class InterstitialAdManager(private val context: Context, private val unitId: String) {
    var interstitialAd: InterstitialAd? = null

    fun load() {
        InterstitialAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    fun show(activity: Activity) {
        val ad = interstitialAd ?: return
        if (activity.isFinishing) return
        ad.show(activity)
        interstitialAd = null
        load()
    }
}
