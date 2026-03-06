package com.toe.tictactoe.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class BluetoothDeviceDomain(
    val name: String?,
    val address: String,
    val type:String?=null,
    val isPairing: Boolean = false
) : Parcelable