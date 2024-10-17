package com.kdvideo_downloader.video_downloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


import android.net.ConnectivityManager
import androidx.core.app.NotificationCompat


/** Author : https://devdeeds.com
 *  Project : Sample Project - Internet status checking
 *  Date : 24 Feb 2018*/

class Connection : BroadcastReceiver() {

    override fun onReceive(context: Context, arg1: Intent) {

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context))
        }
    }

    private fun isConnectedOrConnecting(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }
}
