package com.kdvideo_downloader.video_downloader
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.channels.FileChannel
import io.flutter.plugin.common.MethodChannel
import android.content.pm.PackageManager

import android.content.pm.ApplicationInfo




class DownloadService : Service()  ,FetchListener,Connection.ConnectivityReceiverListener{

    private val CHANNEL_ID = "downloader_progress_plugin"
    lateinit var  notf:NotificationCompat.Builder
    private lateinit var  builderManager: NotificationManager
    var updateUIReciver: BroadcastReceiver? = null




     companion object {
        lateinit  var channel:MethodChannel
        fun startService(context: Context, message: String,channel1:MethodChannel) {

            val startIntent = Intent(context, DownloadService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, DownloadService::class.java)
            context.stopService(stopIntent)
        }
         fun  download(url:String,path:String,title:String):String{

             val request = Request(url.toString(), path.toString())
             request.priority = Priority.HIGH
             request.networkType = NetworkType.ALL
             request.tag=title
             DownloadManager.downloadController?.enqueue(request)




             return request.id.toString()

         }
    }

    @SuppressLint("ServiceCast")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Connection.connectivityReceiverListener=this

        createNotificationChannel()

        builderManager=applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(this)
            .setDownloadConcurrentLimit(10)
            .build()

        DownloadManager.downloadController = Fetch.Impl.getInstance(fetchConfiguration)

createNotificationChannel()
        val applicationInfo = applicationContext.packageManager.getApplicationInfo(
            applicationContext.packageName, PackageManager.GET_META_DATA
        )
        val appIconResId = applicationInfo.icon
        notf = NotificationCompat.Builder (this, CHANNEL_ID)
            .setContentTitle("app runinng in background")
            // .setContentText("app runinng in background")

            .setSmallIcon(appIconResId)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        startForeground(1, notf.build())
//        builderManager.notify(1, notf.build())
        notf.setPriority(Notification.PRIORITY_LOW)


        DownloadManager.downloadController!! .addListener(this)

checkselfe()


        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW)
            serviceChannel.setSound(null, null);
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }


    override fun onAdded(download: Download) {
        sendBrodcast(download.id,download.progress,"onAdd",-1,download.file,download.tag)
    }

    override fun onCancelled(download: Download) {

        sendBrodcast(download.id,download.progress,"canceld task",5,download.file,download.tag)

    }

    override fun onCompleted(download: Download) {

        sendBrodcast(download.id,download.progress,"complete download",3,download.file,download.tag)
        notf .setSound(null)

        notf.setProgress(0,0,false)
        notf.setContentText("complete")
        notf .setContentTitle(download.tag)
        notf .setOngoing(false)
        notf.color = Color.parseColor("#00ff00")
        builderManager.notify(download.id, notf.build())


      checkselfe()


    }

    override fun onDeleted(download: Download) {
        builderManager.cancel(download.id)
        checkselfe()
        sendBrodcast(download.id,download.progress,"deletet task",8,download.file,download.tag)
    }

    override fun onDownloadBlockUpdated(
        download: Download,
        downloadBlock: DownloadBlock,
        totalBlocks: Int
    ) {

    }

    override fun onError(
        download: Download,
        error: com.tonyodev.fetch2.Error,
        throwable: Throwable?
    ) {
        sendBrodcast(download.id,download.progress,"error ${error.name}",-1,download.file,download.tag)
    }

    override fun onPaused(download: Download) {

        sendBrodcast(download.id,download.progress,"paused",6,download.file,download.tag)
        notf .setSound(null)

        notf.setProgress(100,1110,false)
        notf.setContentText("Paused")
        notf .setContentTitle(download.tag)
        notf .setOngoing(false)
        notf.color = Color.parseColor("#000000")
      builderManager.notify(download.id, notf.build())


    }

    override fun onProgress(
        download: Download,
        etaInMilliSeconds: Long,
        downloadedBytesPerSecond: Long
    ) {

        sendBrodcast(download.id,download.progress,"running task",2,download.file,download.tag)
        notf .setSound(null)

        notf.setProgress(100,download.progress,false)
        notf.setContentText("${download.progress} %")
        notf .setContentTitle(download.tag)
        notf.color = Color.parseColor("#ff0000")
       builderManager.notify(download.id, notf.build())


    }

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {

        sendBrodcast(download.id,download.progress,"in queue",1,download.file,download.tag)
        notf .setSound(null)
        notf .setOngoing(false)
        notf.setProgress(0,0,true)
        notf.setContentText("${download.progress} %")
        notf .setContentTitle(download.tag)
        notf.color = Color.parseColor("#ff0000")
        builderManager.notify(download.id, notf.build())
    }

    override fun onRemoved(download: Download) {

        sendBrodcast(download.id,download.progress,"task removed",9,download.file,download.tag)

    }

    override fun onResumed(download: Download) {
        sendBrodcast(download.id,download.progress,"resume task",2,download.file,download.tag)
    }

    override fun onStarted(
        download: Download,
        downloadBlocks: List<DownloadBlock>,
        totalBlocks: Int
    ) {
        sendBrodcast(download.id,download.progress,"started",1,download.file,download.tag)
    }

    override fun onWaitingNetwork(download: Download) {

        sendBrodcast(download.id,download.progress,"wait connection",10,download.file,download.tag)
        notf .setSound(null)
        notf .setOngoing(false)
        notf.setProgress(0,0,false)
        notf.setContentText("${download.progress} %")
        notf .setContentTitle(download.tag)
        notf.color = Color.parseColor("#ff0000")
        builderManager.notify(download.id, notf.build())
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {

       if (isConnected){
           reDownload()
       }
    }
    private fun reDownload(){

        DownloadManager.downloadController!!.getDownloads(Func<List<Download>> { data ->
//            Toast.makeText(applicationContext,"D == ${data.size}",Toast.LENGTH_SHORT).show()
            data.forEach {
//                Toast.makeText(applicationContext,"${it.status.name}",Toast.LENGTH_SHORT).show()
                if( it.status != Status.COMPLETED && it.status != Status.PAUSED ){
                   if(it.status== Status.FAILED){
                       DownloadManager.downloadController!!.retry(it.id)
                   }else{
                       DownloadManager.downloadController!!.resume(it.id)
                   }
                }
            }

        })
    }

    private  fun sendBrodcast(
        taskId:Int,
        progress:Int,
        mess:String,
        status:Int,
        path:String, title: String?
    ){
       try {
           channel?.invokeMethod("listener","{" +
                   " \"taskid\": $taskId , " +
                   " \"status\": $status  ," +
                   " \"progress\": $progress ," +
                   " \"msg\": \"$mess \" ," +
                   " \"path\": \"$path \" " +

                   "}")

       }catch (e:Exception){  }

    }
fun checkselfe(){
    DownloadManager.downloadController!!.getDownloads(Func<List<Download>> { data ->
        if(data.isNotEmpty()){
            if(data.any { it.status != Status.COMPLETED }){


            }else{
                stopSelf()
            }}else{
            stopSelf()
        }

    })

}
}


