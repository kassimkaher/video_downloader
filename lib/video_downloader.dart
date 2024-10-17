import 'dart:async';

import 'package:flutter/services.dart';

class FileDownloader {
  static const MethodChannel _channel =
      MethodChannel('flutter_downloader_progress');

  static Future<void> initialized() async {
    await _channel.invokeMethod('ini');
  }

  static Future<void> pause(int id) async {
    await _channel.invokeMethod('pause', {"taskId": id});
  }

  static Future<void> resume(int id) async {
    await _channel.invokeMethod('resume', {"taskId": id});
  }

  static Future<void> redownload(int id) async {
    await _channel.invokeMethod('redownload', {"taskId": id});
  }

  static Future<void> remove(int id) async {
    await _channel.invokeMethod('remove', {"taskId": id});
  }

  static Future<dynamic> getTask() async {
    final dynamic tasks = await _channel.invokeMethod('getTask');
    return tasks;
  }

  static Future<void> cancelAll() async {
    await _channel.invokeMethod('cancelAll');
  }

  static Future<String> download(
      {required String url,
      required String path,
      required String title}) async {
    final String? version = await _channel.invokeMethod(
        'download_file', {"url": url, "path": path, "title": title});
    return version!;
  }

  static void listener(
      {required Future<dynamic> Function(MethodCall call) handlar}) async {
    _channel.setMethodCallHandler(handlar);
  }
}
