package com.kdvideo_downloader.video_downloader


import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

import android.os.Bundle;
import androidx.core.content.ContextCompat.getSystemService
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.Func

import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import kotlin.concurrent.thread


class VideoDownloaderPlugin: FlutterPlugin, MethodCallHandler ,
  ActivityAware{
  private lateinit var channel : MethodChannel
  private  lateinit var ctx: Context
  private  lateinit var activity: Activity
  lateinit var downloade: DownloadManager
  var updateUIReciver: BroadcastReceiver? = null
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_downloader_progress")
    channel.setMethodCallHandler(this)
    ctx = flutterPluginBinding.applicationContext
    downloade= DownloadManager(flutterPluginBinding.applicationContext, channel)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "ini") {
      DownloadService.channel=this.channel
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
      // downloade.ini()
      ini()

      channel.invokeMethod("listener","ini done")


    }
    else if (call.method == "download_file_another") {

      var url: String? = call.argument("url");
      var path: String? = call.argument("path");
      var title: String? = call.argument("title");

      var taskid=  downloade.download(url!!,path!!,title!!)
      result.success(taskid.toString())
    }
    else if (call.method == "download_file") {
      if(!isMyServiceRunning(DownloadService.javaClass)) {

        DownloadService.startService(ctx, "Foreground Service is running...",channel)
      }else{

      }
      var url: String? = call.argument("url");
      var path: String? = call.argument("path");
      var title: String? = call.argument("title");

      var taskid=  downloade.download(url!!,path!!,title!!)
      result.success(taskid.toString())
    }

    else if (call.method == "cancelAll") {

      DownloadManager.downloadController?.removeAll()
      Toast.makeText(ctx,"test",Toast.LENGTH_SHORT) .show()
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }
    else if (call.method == "getTask") {
      var resultd :String?="["

      DownloadManager.downloadController!!.getDownloads(Func<List<Download>> { data ->
        if(data.size>0) {

          data.forEach {
//
            if (it.status == Status.COMPLETED) {
              resultd += "{" +
                      " \"taskid\": ${it.id}, " +
                      " \"status\": 3 ," +
                      " \"progress\": ${it.progress} , " +
                      " \"msg\": \" get task \", " +
                      " \"path\": \"${it.file} \" " +

                      "},"

            } else if (it.status == Status.PAUSED) {
              resultd += "{" +
                      " \"taskid\": ${it.id} ," +
                      " \"status\": 6, " +
                      " \"progress\": ${it.progress}  ," +
                      " \"msg\": \" get task \", " +
                      " \"path\": \"${it.file} \" " +

                      "},"
            } else if (it.status == Status.FAILED) {
              resultd += "{" +
                      " \"taskid\": ${it.id} , " +
                      " \"status\": 4, " +
                      " \"progress\": ${it.progress} , " +
                      " \"msg\": \" get task \", " +
                      " \"path\": \"${it.file} \" " +

                      "},"
            } else if (it.status == Status.DOWNLOADING) {
              resultd += "{" +
                      " \"taskid\": ${it.id}  ," +
                      " \"status\": 2," +
                      " \"progress\": ${it.progress}  ," +
                      " \"msg\": \" get task \", " +
                      " \"path\": \"${it.file} \" " +

                      "},"
            } else if (it.status == Status.QUEUED || it.status == Status.ADDED) {
              resultd += "{" +
                      " \"taskid\": ${it.id} , " +
                      " \"status\": 1 ," +
                      " \"progress\": ${it.progress}  ," +
                      " \"msg\": \" get task \" ," +
                      " \"path\": \"${it.file} \" " +

                      "},"
            }
          }

          resultd =resultd?.substring(0,resultd?.length!!-1)

          resultd +="]"
        }else{

          resultd = null
        }


        result.success(resultd)
      })

    }
    else if (call.method == "pause") {
      var taskId: Int? = call.argument("taskId");
      DownloadManager.downloadController!!.pause(taskId!!)

      result.success("pause $taskId")
    }
    else if (call.method == "resume") {

      var taskId: Int? = call.argument("taskId");
      DownloadManager.downloadController!!.resume(taskId!!)

      result.success("resume $taskId")
    }
    else if (call.method == "redownload") {

      var taskId: Int? = call.argument("taskId");
      DownloadManager.downloadController!!.retry(taskId!!)

      result.success("redownload $taskId")
    }
    else if (call.method == "remove") {

      var taskId: Int? = call.argument("taskId");
      DownloadManager.downloadController!!.delete(taskId!!)
      // DownloadManager.downloadController!!.remove(taskId!!)

      result.success("pause $taskId")
    }
    else if (call.method == "disconnect") {

      Toast.makeText(ctx,"disconnect connected",Toast.LENGTH_SHORT) .show()
      result.success("disconnect ${android.os.Build.VERSION.RELEASE}")
    }

    else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }


  override fun onAttachedToActivity(@NonNull p0: ActivityPluginBinding) {
    this.activity = p0.activity
  }

  override fun onDetachedFromActivityForConfigChanges() {
    // TODO: the Activity your plugin was attached to was
    // destroyed to change configuration.
    // This call will be followed by onReattachedToActivityForConfigChanges().
  }

  override fun onReattachedToActivityForConfigChanges(@NonNull p0: ActivityPluginBinding) {
    // TODO: your plugin is now attached to a new Activity
    // after a configuration change.
  }

  override  fun onDetachedFromActivity() {
    // TODO: your plugin is no longer associated with an Activity.
    // Clean up references.
  }
  fun ini(){

    if(!isMyServiceRunning(DownloadService.javaClass)) {
      DownloadService.startService(ctx, "Foreground Service is running...",channel)
    }
    ctx. registerReceiver(Connection(),
      IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
//        val filter = IntentFilter()
//        filter.addAction("service.to.activity.transfer")
//        updateUIReciver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                //UI update here
//                if (intent != null)
//                    channel.invokeMethod("downloadStream", intent.getStringExtra("data").toString())
//            }
//        }
//        try {
//          ctx.unregisterReceiver(updateUIReciver as BroadcastReceiver)
//        } catch (e: Exception) {
//
//        }
//        try {
//
//
//          ctx. registerReceiver(updateUIReciver as BroadcastReceiver, filter)
//        } catch (e: Exception) {
//            var m = 0
//        }
  }
  private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {

    val manager = ctx.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {

      if (serviceClass.name.split("$")[0] == service.service.className) {
        return true
      }
    }
    return false
  }

}
