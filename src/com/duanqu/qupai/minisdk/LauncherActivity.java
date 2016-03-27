package com.duanqu.qupai.minisdk;

import java.io.File;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.duanqu.qupai.android.app.QupaiDraftManager;
import com.duanqu.qupai.android.app.QupaiServiceImpl;
import com.duanqu.qupai.bean.QupaiUploadTask;
import com.duanqu.qupai.editor.EditorResult;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.minisdk.common.Contant;
import com.duanqu.qupai.minisdk.common.FileUtils;
import com.duanqu.qupai.recorder.EditorCreateInfo;
import com.duanqu.qupai.sdk.R;
import com.duanqu.qupai.upload.AuthService;
import com.duanqu.qupai.upload.QupaiAuthListener;
import com.duanqu.qupai.upload.QupaiUploadListener;
import com.duanqu.qupai.upload.UploadService;

public class LauncherActivity extends Activity {
    private static final String TAG = "Upload";
    private static final String AUTHTAG = "QupaiAuth";
    private static int QUPAI_RECORD_REQUEST = 1;
    EditText edit_min_time;
    EditText edit_max_time;
    EditText edit_max_rate;
    EditText beauty_skin_progress;

    private Switch beauty_skin_on;
    private Switch camera_font_on;

    private Switch flashlight_view_on;
    private Switch beauty_skin_view_on;
    private Switch timeline_indicator_on;

    private int beautySkinProgress;
    private EditText edit_output_video_width;
    private EditText edit_output_video_height;

    private EditText capture_height;
    private Button mini_upload;
    private Button btn_open_video;

    Button button;
    private ProgressBar progress;
    private String videoUrl;
    private final EditorCreateInfo _CreateInfo = new EditorCreateInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.minisdj_main);

        init();
        button = (Button) findViewById(R.id.qupai_mini_record);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecordActivity();
            }
        });

        mini_upload = (Button) findViewById(R.id.qupai_mini_upload);
        mini_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(LauncherActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
        progress = (ProgressBar) findViewById(R.id.tv_upload_process);
        btn_open_video = (Button) findViewById(R.id.btn_open_video);
        progress.setVisibility(View.GONE);
        btn_open_video.setVisibility(View.GONE);
        btn_open_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(videoUrl);
                intent.setData(content_url);
                startActivity(intent);
            }
        });
    }

    private void init() {
        edit_min_time = (EditText) findViewById(R.id.edit_min_time);
        edit_max_time = (EditText) findViewById(R.id.edit_max_time);
        edit_max_rate = (EditText) findViewById(R.id.edit_max_rate);

        beauty_skin_progress = (EditText) findViewById(R.id.beauty_skin_progress);
        beauty_skin_on = (Switch) findViewById(R.id.beauty_skin_on);
        camera_font_on = (Switch) findViewById(R.id.camera_font_on);
        flashlight_view_on = (Switch) findViewById(R.id.flashlight_view_on);
        beauty_skin_view_on = (Switch) findViewById(R.id.beauty_skin_view_on);
        timeline_indicator_on = (Switch) findViewById(R.id.timeline_indicator_on);

        edit_output_video_width = (EditText) findViewById(R.id.edit_output_video_width);
        edit_output_video_height = (EditText) findViewById(R.id.edit_output_video_height);

        edit_output_video_width.setText("" + 360);
        edit_output_video_height.setText("" + 640);

        capture_height = (EditText) findViewById(R.id.capture_height);
    }

    private void startRecordActivity() {
        //美颜参数:1-100.这里不设指定为80,这个值只在第一次设置，之后在录制界面滑动美颜参数之后系统会记住上一次滑动的状态
        beautySkinProgress = Integer.valueOf(TextUtils.isEmpty(beauty_skin_progress.getText()) ? "80" : beauty_skin_progress.getText().toString());

        /**
         * 压缩参数，可以自由调节
         */
        MovieExportOptions movie_options = new MovieExportOptions.Builder()
                .setVideoProfile("high")
                .setVideoBitrate(TextUtils.isEmpty(edit_max_rate.getText()) ? Contant.DEFAULT_BITRATE : Integer.valueOf(edit_max_rate.getText().toString()))
                .setVideoPreset(Contant.DEFAULT_VIDEO_Preset).setVideoRateCRF(Contant.DEFAULT_VIDEO_RATE_CRF)
                .setOutputVideoLevel(Contant.DEFAULT_VIDEO_LEVEL)
                .setOutputVideoTune(Contant.DEFAULT_VIDEO_TUNE)
                .configureMuxer(Contant.DEFAULT_VIDEO_MOV_FLAGS_KEY, Contant.DEFAULT_VIDEO_MOV_FLAGS_VALUE)
                .build();

        /**
         * 界面参数
         */
        VideoSessionCreateInfo create_info = new VideoSessionCreateInfo.Builder()
                .setOutputDurationLimit(TextUtils.isEmpty(edit_max_time.getText()) ? Contant.DEFAULT_DURATION_MAX_LIMIT : Integer.valueOf(edit_max_time.getText().toString()))
                .setOutputDurationMin(TextUtils.isEmpty(edit_min_time.getText()) ? Contant.DEFAULT_DURATION_LIMIT_MIN : Integer.valueOf(edit_min_time.getText().toString()))
                .setMovieExportOptions(movie_options)
                .setWaterMarkPath(Contant.WATER_MARK_PATH)
                .setWaterMarkPosition(1)
                .setBeautyProgress(beautySkinProgress)
                .setBeautySkinOn(beauty_skin_on.isChecked())
                .setCameraFacing(camera_font_on.isChecked() ? Camera.CameraInfo.CAMERA_FACING_FRONT :
                        Camera.CameraInfo.CAMERA_FACING_BACK)
                .setVideoSize(TextUtils.isEmpty(edit_output_video_width.getText().toString()) ? 480 : Integer.valueOf(edit_output_video_width.getText().toString()),
                        TextUtils.isEmpty(edit_output_video_height.getText().toString()) ? 480 : Integer.valueOf(edit_output_video_height.getText().toString()))
                .setCaptureHeight(TextUtils.isEmpty(capture_height.getText().toString()) ? getResources().getDimension(R.dimen.qupai_recorder_capture_height_size) : Float.valueOf(capture_height.getText().toString()))
                .setBeautySkinViewOn(beauty_skin_view_on.isChecked() ? true : false)
                .setFlashLightOn(flashlight_view_on.isChecked() ? true : false)
                .setTimelineTimeIndicator(timeline_indicator_on.isChecked() ? true : false)
                .build();

        _CreateInfo.setSessionCreateInfo(create_info);
        _CreateInfo.setNextIntent(null);
        _CreateInfo.setOutputThumbnailSize(360, 640);//输出图片宽高
        videoPath = FileUtils.newOutgoingFilePath(this);
        _CreateInfo.setOutputVideoPath(videoPath);//输出视频路径
        _CreateInfo.setOutputThumbnailPath(videoPath + ".png");//输出图片路径

        QupaiServiceImpl qupaiService= new QupaiServiceImpl.Builder()
                .setEditorCreateInfo(_CreateInfo).build();
        qupaiService.showRecordPage(this,QUPAI_RECORD_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            EditorResult result = new EditorResult(data);
            //得到视频path，和缩略图path的数组，返回十张缩略图,和视频时长
            result.getPath();
            result.getThumbnail();
            result.getDuration();

            //开始上传，上传前请务必确认已调用授权接口
            startUpload();

            //删除草稿
            QupaiDraftManager draftManager =new QupaiDraftManager();
            draftManager.deleteDraft(data);
        }
    }

    private String videoPath;

    /**
     * 开始鉴权。我们建议只鉴权一次 在application里面做。保存accessToken
     * @param context
     * @param appKey 应用app key
     * @param appsecret 应用 app secret
     * @param space 存储空间名 最长32位 SDK需提供生成策略
     */
    private void startAuth(Context context,String appKey, String appsecret,String space) {
        AuthService service = AuthService.getInstance();
        service.setQupaiAuthListener(new QupaiAuthListener() {
            @Override
            public void onAuthError(int errorCode, String message) {
                Log.e(AUTHTAG, "ErrorCode" + errorCode + "message" + message);
            }

            @Override
            public void onAuthComplte(int responseCode, String responseMessage) {
                Contant.accessToken = responseMessage;
            }
        });
        service.startAuth(context,appKey, appsecret, space);
    }


    /**
     * 创建一个上传任务
     * @param context
     * @param uuid        随机生成的UUID
     * @param _VideoFile  完整视频文件
     * @param _Thumbnail  缩略图
     * @param accessToken 通过调用鉴权得到token
     * @param space        开发者生成的Quid，必须要和token保持一致
     * @param share       是否公开 0公开分享 1私有(default) 公开类视频不需要AccessToken授权
     * @param tags        标签 多个标签用 "," 分隔符
     * @param description 视频描述
     * @return
     */
    private QupaiUploadTask createUploadTask(Context context, String uuid, File _VideoFile, File _Thumbnail, String accessToken,
                                             String space, int share, String tags, String description) {
        UploadService uploadService = UploadService.getInstance();
        return uploadService.createTask(context, uuid, _VideoFile, _Thumbnail,
                accessToken, space, share, tags, description);
    }

    /**
     * 开始上传
     */
    private void startUpload() {
        progress.setVisibility(View.VISIBLE);
        UploadService uploadService = UploadService.getInstance();
        uploadService.setQupaiUploadListener(new QupaiUploadListener() {
            @Override
            public void onUploadProgress(String uuid, long uploadedBytes, long totalBytes) {
                int percentsProgress = (int) (uploadedBytes * 100 / totalBytes);
                Log.e(TAG, "uuid:" + uuid + "data:onUploadProgress" + percentsProgress);
                progress.setProgress(percentsProgress);
            }

            @Override
            public void onUploadError(String uuid, int errorCode, String message) {
                Log.e(TAG, "uuid:" + uuid + "onUploadError" + errorCode + message);
            }

            @Override
            public void onUploadComplte(String uuid, int responseCode, String responseMessage) {
                //http://{DOMAIN}/v/{UUID}.mp4?token={ACCESS-TOKEN}
                progress.setVisibility(View.GONE);
                btn_open_video.setVisibility(View.VISIBLE);

                //这里返回的uuid是你创建上传任务时生成的uuid.开发者可以使用其他作为标识
                //videoUrl返回的是上传成功的video地址
                videoUrl = Contant.domain + "/v/" + responseMessage + ".mp4" + "?token=" + Contant.accessToken;
                Log.i("TAG", "data:onUploadComplte" + "uuid:" + uuid + Contant.domain +"/v/"+ responseMessage + ".jpg" + "?token=" + Contant.accessToken);
                Log.i("TAG", "data:onUploadComplte" + "uuid:" + uuid + Contant.domain +"/v/"+ responseMessage + ".mp4" + "?token=" + Contant.accessToken);
            }
        });
        String uuid = UUID.randomUUID().toString();
        startUpload(createUploadTask(this, uuid, new File(videoPath), new File(videoPath + ".png"),
                Contant.accessToken, Contant.space, Contant.shareType, Contant.tags, Contant.description));
    }

    /**
     * 开始上传
     * @param data 上传任务的task
     */
    private void startUpload(QupaiUploadTask data) {
        try {
            UploadService uploadService = UploadService.getInstance();
            uploadService.startUpload(data);
        } catch (IllegalArgumentException exc) {
            Log.d("upload", "Missing some arguments. " + exc.getMessage());
        }
    }

}
