package com.kdvideo_downloader.video_downloader

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import kotlin.random.Random
import io.flutter.plugin.common.MethodChannel
class DownloadManager(ctx1:Context,channel1:MethodChannel) : FetchListener{
    var ctx:Context=ctx1
    var channel :MethodChannel=channel1

    companion object {
        var downloadController:Fetch? = null
    }
    fun ini(){
        Toast.makeText(ctx,"begin class", Toast.LENGTH_SHORT) .show()

        val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(ctx)
            .setDownloadConcurrentLimit(10)
            .build()

        downloadController = Fetch.Impl.getInstance(fetchConfiguration)
       // downloadController?.addListener(this)
    }
    fun  download(url:String,path:String,title:String):String{

        val request = Request(url.toString(), path)
        request.priority = Priority.HIGH
        request.networkType = NetworkType.ALL
        request.tag=title
        downloadController?.enqueue(request)




return request.id.toString()

    }

    override fun onAdded(download: Download) {
        channel.invokeMethod("listener","download add ${download.id} == ${download.tag}")
    }

    override fun onCancelled(download: Download) {
        channel.invokeMethod("listener","download canceld ${download.id} == ${download.tag}")
    }

    override fun onCompleted(download: Download) {
        channel.invokeMethod("listener","download coplete ${download.file} == ${download.tag}")
    }

    override fun onDeleted(download: Download) {
        channel.invokeMethod("listener","download deleted ${download.id} == ${download.tag}")
    }

    override fun onDownloadBlockUpdated(
        download: Download,
        downloadBlock: DownloadBlock,
        totalBlocks: Int
    ) {
        channel.invokeMethod("listener","download block ${download.id} == ${download.tag}")
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
        channel.invokeMethod("listener","download error ${download.id} == ${error.name} ==path   ${download.file}     url=   ${download.url}")
    }

    override fun onPaused(download: Download) {
        channel.invokeMethod("listener","download paused ${download.id} == ${download.tag}")
    }

    override fun onProgress(
        download: Download,
        etaInMilliSeconds: Long,
        downloadedBytesPerSecond: Long
    ) {
        channel.invokeMethod("listener","download add ${download.tag} == ${download.progress}")
    }

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
        channel.invokeMethod("listener","download queu ${download.id} == ${download.tag}")
    }

    override fun onRemoved(download: Download) {
        channel.invokeMethod("listener","download remove ${download.id} == ${download.tag}")
    }

    override fun onResumed(download: Download) {
        channel.invokeMethod("listener","download resume ${download.id} == ${download.tag}")
    }

    override fun onStarted(
        download: Download,
        downloadBlocks: List<DownloadBlock>,
        totalBlocks: Int
    ) {
      //  channel.invokeMethod("listener","download started ${download.id} == ${download.file}")
    }

    override fun onWaitingNetwork(download: Download) {
        channel.invokeMethod("listener","download wait ${download.id} == ${download.tag}")
    }


//    val randomValues =  Random.nextInt(10)
//    val local = Intent()
//    local.action = "service.to.activity.transfer1"
//
//    local.putExtra("url", url)
//    local.putExtra("dirPath", dirPath)
//    local.putExtra("fileName", "$randomValues$fileName")
//
//    applicationContext.sendBroadcast(local)
}