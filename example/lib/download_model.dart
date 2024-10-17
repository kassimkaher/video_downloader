class DownloadModel {
  late int taskid;
  late int status;
  late int progress;
  late String msg;
  late String path;

  DownloadModel(
      {required this.taskid,
      required this.status,
      required this.progress,
      required this.msg,
      required this.path});

  DownloadModel.fromJson(Map<String, dynamic> json) {
    taskid = json['taskid'];
    status = json['status'];
    progress = json['progress'];
    msg = json['msg'];
    path = json['path'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['taskid'] = this.taskid;
    data['status'] = this.status;
    data['progress'] = this.progress;
    data['msg'] = this.msg;
    data['path'] = this.path;
    return data;
  }
}
