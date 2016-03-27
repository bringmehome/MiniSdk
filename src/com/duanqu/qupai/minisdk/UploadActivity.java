package com.duanqu.qupai.minisdk;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.duanqu.qupai.VideoActivity;
import com.duanqu.qupai.bean.QupaiUploadTask;
import com.duanqu.qupai.editor.EditorResult;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.SessionClientActivityModule;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.minisdk.adapter.CommonAdapterHelper;
import com.duanqu.qupai.minisdk.adapter.UploadAdapter;
import com.duanqu.qupai.minisdk.common.Contant;
import com.duanqu.qupai.minisdk.common.FileUtils;
import com.duanqu.qupai.recorder.EditorCreateInfo;
import com.duanqu.qupai.sdk.R;
import com.duanqu.qupai.upload.AuthService;
import com.duanqu.qupai.upload.QupaiAuthListener;
import com.duanqu.qupai.upload.QupaiUploadListener;
import com.duanqu.qupai.upload.UploadService;

public class UploadActivity extends Activity implements UploadAdapter.Callback, CommonAdapterHelper,View.OnClickListener, QupaiAuthListener {
    private static final String USER_AGENT = "UploadService";
    private static final String TAG = "UploadServiceDemo";
    private ListView listView;
    private Button recordMultiBtn;
    private Button uploadTaskClearBtn;
    private Button btnAuth;
    private UploadAdapter uploadAdapter;

    private String[] thumbnail;
    List<QupaiUploadTask> uploadFileList;
    private final EditorCreateInfo _CreateInfo = new EditorCreateInfo();
    private UploadService uploadService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.minisdj_upload);
        uploadService = UploadService.getInstance();
        uploadService.setQupaiUploadListener(_QupaiUploadListener);

        recordMultiBtn = (Button)findViewById(R.id.recordMultiBtn);
        uploadTaskClearBtn = (Button)findViewById(R.id.uploadTaskClearBtn);
        btnAuth = (Button)findViewById(R.id.btn_auth);
        listView = (ListView) findViewById(R.id.upload_list);
        uploadFileList = getUploadList();
        uploadAdapter = new UploadAdapter(this, this, this);
        listView.setAdapter(uploadAdapter);

        btnAuth.setOnClickListener(this);
        recordMultiBtn.setOnClickListener(this);
        uploadTaskClearBtn.setOnClickListener(this);
    }

    private List<QupaiUploadTask> getUploadList() {
        return uploadService.checkUploadListTask(this);
    }

    @Override
    public void uploadOnClick(View v, int position) {
        QupaiUploadTask data = (QupaiUploadTask) uploadAdapter.getItem(position);
        if (data.isUploadStatus() == null) {
            startUpload(data);
            return;
        }
        switch (data.isUploadStatus()) {
            case UPLOADED:
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(data.getVideoUrl());
                intent.setData(content_url);
                startActivity(intent);
                break;
            case UPLOADING:
                Toast.makeText(this, v.getResources().getString(R.string.uploading), Toast.LENGTH_LONG).show();
                break;
            case UNUPLOAD:
                startUpload(data);
                break;
        }
    }

    @Override
    public Object getItemList() {
        return uploadFileList;
    }

    @Override
    public void notifyChange() {
        uploadAdapter.notifyDataSetChanged();
    }

    private void startUpload(QupaiUploadTask data) {
        try {
            //新开一个不在上传的任务应该直接上传
            if (!uploadService.isUploadingTask(data)) {
                //有断点，队列由应用自己维护
                uploadService.startPartUpload(this, data);
            } else {
                //无断点
                uploadService.startUpload(data);
            }
        } catch (IllegalArgumentException exc) {
            Log.d("upload", "Missing some arguments. " + exc.getMessage());
        }
    }

    QupaiUploadListener _QupaiUploadListener = new QupaiUploadListener() {

        @Override
        public void onUploadProgress(String uuid, long uploadedBytes, long totalBytes) {
            int percentsProgress = (int) (uploadedBytes * 100 / totalBytes);
            Log.i(TAG, "uuid:" + uuid + "data:onUploadProgress" + percentsProgress);
            for (int i = 0; i < uploadFileList.size(); i++) {
                if (uploadFileList.get(i).getUuid().equals(uuid)) {
                    uploadFileList.get(i).setUploadStatus(QupaiUploadTask.UploadType.UPLOADING);
                    uploadFileList.get(i).setProgress(percentsProgress);
                }
            }
            notifyChange();
        }

        @Override
        public void onUploadError(String uuid, int errorCode, String message) {
            Toast.makeText(UploadActivity.this,"onUploadError:" + message,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onUploadComplte(String uuid, int responseCode, String responseMessage) {
            Log.i("TAG", "data:onUploadComplte" + "uuid:" + uuid + Contant.domain +"/v/"+ responseMessage + ".jpg" + "?token=" + Contant.accessToken);
            Log.i("TAG", "data:onUploadComplte" + "uuid:" + uuid + Contant.domain +"/v/"+ responseMessage + ".mp4" + "?token=" + Contant.accessToken);
            for (int i = 0; i < uploadFileList.size(); i++) {
                if (uploadFileList.get(i).getUuid().equals(uuid)) {
                    uploadFileList.get(i).setUploadStatus(QupaiUploadTask.UploadType.UPLOADED);
                    uploadFileList.get(i).setVideoUrl(Contant.domain + "/v/" + responseMessage + ".mp4" + "?token=" + Contant.accessToken);
                    uploadFileList.get(i).setThumbnailUrl(Contant.domain + "/v/" + responseMessage + ".jpg" + "?token=" + Contant.accessToken);
                }
            }
            notifyChange();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.recordMultiBtn:
                startRecordActivity();
                break;
            case R.id.uploadTaskClearBtn:
                if(uploadService.clearUploadTask(v.getContext())){
                    uploadFileList = getUploadList();
                    notifyChange();
                }
                break;
            case R.id.btn_auth:
                startAuth(v.getContext(),Contant.appkey,Contant.appsecret,Contant.space,Contant.domain);
                break;
        }
    }

    private void startAuth(Context context,String appKey,String appsecret,String space ,String domain){
        AuthService service = AuthService.getInstance();
        service.setQupaiAuthListener(this);
        service.startAuth(context ,appKey, appsecret, space);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            EditorResult result = new EditorResult(data);
            //得到视频path，和缩略图path的数组，返回十张缩略图
//            videoFile = result.getPath();
            thumbnail = result.getThumbnail();

            createTask();

            uploadFileList = getUploadList();
            notifyChange();
        }


    }

    private void createTask(){
        String uuid = UUID.randomUUID().toString();
        uploadService.createTask(this,uuid,new File(_CreateInfo.get_OutputVideoPath()),new File(thumbnail[0]),
                Contant.accessToken,Contant.space,Contant.shareType,Contant.tags,Contant.description);
    }

    private void startRecordActivity() {
        /**
         * 压缩参数，可以自由调节
         */
        MovieExportOptions movie_options = new MovieExportOptions.Builder()
                .setVideoBitrate(1024*1000)
                .setVideoPreset(Contant.DEFAULT_VIDEO_Preset).setVideoRateCRF(Contant.DEFAULT_VIDEO_RATE_CRF)
                .setOutputVideoLevel(Contant.DEFAULT_VIDEO_LEVEL)
                .setOutputVideoTune(Contant.DEFAULT_VIDEO_TUNE)
                .configureMuxer(Contant.DEFAULT_VIDEO_MOV_FLAGS_KEY, Contant.DEFAULT_VIDEO_MOV_FLAGS_VALUE)
                .build();
        /**
         * 界面参数
         */
        VideoSessionCreateInfo create_info = new VideoSessionCreateInfo.Builder()
                .setOutputDurationLimit(15)
                .setOutputDurationMin(3)
                .setMovieExportOptions(movie_options)
                .setCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK)
                .setVideoSize(360, 640)
                .setCaptureHeight(getResources().getDimension(R.dimen.qupai_recorder_capture_height_size))
                .setTimelineTimeIndicator(true)
                .build();

        _CreateInfo.setSessionCreateInfo(create_info);
        _CreateInfo.setDurationNano(TimeUnit.MILLISECONDS.toNanos(8));
        _CreateInfo.setNextIntent(null);
        videoPath = FileUtils.newOutgoingFilePath();
        _CreateInfo.setOutputVideoPath(videoPath);
        _CreateInfo.setOutputThumbnailSize(240, 427);
        _CreateInfo.setOutputThumbnailPath(videoPath+ ".png");

        Intent intent = new Intent(this, VideoActivity.class);
        intent.setData(_CreateInfo.getFileUri());
        _CreateInfo.putExtra(intent);
        intent.putExtra("IS_GUIDE_SHOW", true);
        intent = SessionClientActivityModule.apply(intent, EditorCreateInfo.SessionClientFactoryImpl.class, _CreateInfo.get_SessionCreateInfo());
        startActivityForResult(intent, 1);

    }

    private String videoPath;

    @Override
    public void onAuthError(int errorCode, String message) {

    }

    @Override
    public void onAuthComplte(int responseCode, String responseMessage) {
        Contant.accessToken = responseMessage;
        mHandler.sendEmptyMessage(AUTH_COMPLTE);
    }

    private static final int AUTH_COMPLTE = 0;
    private static final int UPLOAD_ERROR = 1;

    public Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case AUTH_COMPLTE:
                    Toast.makeText(UploadActivity.this,getResources().getString(R.string.auth_success),Toast.LENGTH_LONG).show();
                    break;
                case UPLOAD_ERROR:
                    Toast.makeText(UploadActivity.this,getResources().getString(R.string.upload_fiald),Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
}
