package com.toe.tictactoe

import android.app.Application
import com.anythink.core.api.ATSDK
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.games.PlayGamesSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TicTacToeApp : Application() {
    //    <!--    Tic Tac Toe-->
//    <!--    facebook_app_id:1666706900985978-->
//    <!--    facebook_client_token:948dc2081354410f3171a291308680a7-->
//    <!--    APP_ID:h69bbb3fe25009-->
//    <!--    APP_KEY:a631419ae054d74114d3359f8dcac8d91-->
//    <!--    Email:z6i4ofm6@163.com-->
    val APP_ID = "h69bbb3fe25009";
    val APP_KEY = "a631419ae054d74114d3359f8dcac8d91";
    override fun onCreate() {
        super.onCreate()
        PlayGamesSdk.initialize(this);
        MobileAds.initialize(this)
        AudienceNetworkAds.initialize(this);

        ATSDK.init(this, APP_ID, APP_KEY);
    }
}
