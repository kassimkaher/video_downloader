import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';
import 'package:video_downloader/video_downloader.dart';

import 'download_model.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<DownloadModel> downloadList = [];
  Future<String> getpath() async {
    try {
      var documentDirectory = await getApplicationDocumentsDirectory();
      var firstPath = "${documentDirectory.path}/download";

      await Directory(firstPath).create(recursive: true);

      return firstPath;
    } catch (e) {
      return "";
    }
  }

  downloadlistener() {
    FileDownloader.listener(handlar: (data) async {
      var js = json.decode(data.arguments.toString());

      DownloadModel d = DownloadModel.fromJson(js);

      setState(() {
        if (downloadList.isNotEmpty &&
            downloadList.where((w) => w.taskid == d.taskid).isNotEmpty) {
          int ind = downloadList.indexWhere((w) => w.taskid == d.taskid);
          if (d.status != 8) {
            downloadList[ind] = d;
          } else {
            downloadList.removeAt(ind);
          }

          // print("edit = ${d.taskid} s = ${d.status}");
        } else {
          downloadList.add(d);
          // print("add = ${d.taskid}");
        }
      });
    });
  }

  getDownloadsTask() async {
    await FileDownloader.initialized();
    downloadlistener();
    var response = (await FileDownloader.getTask());

    if (response != null) {
      List<dynamic> tasks = json.decode(response.toString());
      for (var a in tasks) {
        DownloadModel downloadItem = DownloadModel.fromJson(a);
        downloadList.add(downloadItem);
      }
      setState(() {});
    }
  }

  @override
  void initState() {
    super.initState();

    getDownloadsTask();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text("downoader"),
          actions: [
            InkWell(
                onTap: () {
                  FileDownloader.cancelAll();
                  setState(() {
                    downloadList = [];
                  });
                },
                child: const Center(
                    child: Padding(
                  padding: EdgeInsets.all(8.0),
                  child: Text("clear"),
                )))
          ],
        ),
        body: ListView(
          children: [
            Column(
              children: [
                TextButton(
                    onPressed: () async {
                      var result = await FileDownloader.download(
                          url:
                              "https://cdn.shashety.com:9191/cinema/videos/Hy7L2PKM5jyWzKAsQS30LJn9pvDPjTQxZdkzjiZr.mp4",
                          path:
                              "${await getpath()}/${DateTime.now().toIso8601String()}.mp4",
                          title: "testdownload");
                    },
                    child: const Text("download")),
              ],
            ),
            Column(
              children: downloadList
                  .map((e) => Card(
                        child: ListTile(
                          onTap: () {
                            if (e.status == 2) {
                              FileDownloader.pause(e.taskid);
                            } else {
                              FileDownloader.resume(e.taskid);
                            }
                          },
                          title: Text(e.taskid.toString()),
                          subtitle: Column(
                            children: [
                              Padding(
                                padding: const EdgeInsets.all(8.0),
                                child: LinearProgressIndicator(
                                  value: e.progress.toDouble() / 100,
                                ),
                              )
                            ],
                          ),
                          leading: IconButton(
                              onPressed: () {
                                FileDownloader.remove(e.taskid);
                              },
                              icon: const Icon(
                                Icons.delete,
                                size: 40,
                              )),
                          trailing: Text("${e.progress} %"),
                        ),
                      ))
                  .toList(),
            )
          ],
        ),
      ),
    );
  }
}
