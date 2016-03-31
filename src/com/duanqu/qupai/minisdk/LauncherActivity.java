package com.duanqu.qupai.minisdk;

import java.io.File;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

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
	private static final String TAG = "---LauncherActivity---";
	private static int QUPAI_RECORD_REQUEST = 1;

	private int beautySkinProgress;

	private Button mini_upload;
	private Button btn_open_video;

	Button button;
	Button initmyca;
	private ProgressBar progress;
	private String videoUrl;
	private final EditorCreateInfo _CreateInfo = new EditorCreateInfo();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.minisdj_main);

		init();
		button = (Button) findViewById(R.id.qupai_mini_record);
		initmyca = (Button) findViewById(R.id.initmyca);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecordActivity();
			}
		});
		initmyca.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				initAuth(LauncherActivity.this, Contant.appkey,
						Contant.appsecret, Contant.space);
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

	}

	private void startRecordActivity() {
		// 美颜参数:1-100.这里不设指定为80,这个值只在第一次设置，之后在录制界面滑动美颜参数之后系统会记住上一次滑动的状态
		beautySkinProgress = 80;

		/**
		 * 压缩参数，可以自由调节
		 */
		MovieExportOptions movie_options = new MovieExportOptions.Builder()
				.setVideoProfile("high")
				.setVideoBitrate(Contant.DEFAULT_BITRATE)
				// 码率
				.setVideoPreset(Contant.DEFAULT_VIDEO_Preset)
				.setVideoRateCRF(Contant.DEFAULT_VIDEO_RATE_CRF)
				.setOutputVideoLevel(Contant.DEFAULT_VIDEO_LEVEL)
				.setOutputVideoTune(Contant.DEFAULT_VIDEO_TUNE)
				.configureMuxer(Contant.DEFAULT_VIDEO_MOV_FLAGS_KEY,
						Contant.DEFAULT_VIDEO_MOV_FLAGS_VALUE).build();

		/**
		 * 界面参数
		 */
		VideoSessionCreateInfo create_info = new VideoSessionCreateInfo.Builder()
				.setOutputDurationLimit(Contant.DEFAULT_DURATION_MAX_LIMIT)
				// 最大时长
				.setOutputDurationMin(Contant.DEFAULT_DURATION_LIMIT_MIN)
				// 最短时长
				.setMovieExportOptions(movie_options)
				.setWaterMarkPath(Contant.WATER_MARK_PATH)// 水印路径
				.setWaterMarkPosition(1)// 水印位置
				.setBeautyProgress(beautySkinProgress)// 美颜参数
				.setBeautySkinOn(true)// 是否开启美颜
				.setCameraFacing(0)// 1 开启 前摄像头0开启后摄像头
				.setVideoSize(480, 480)// 输出视频的尺寸–建议320*240 480*480 360*640
				.setCaptureHeight(
						getResources().getDimension(
								R.dimen.qupai_recorder_capture_height_size))// 拍摄布局
				.setBeautySkinViewOn(true)// 美颜是否显示
				.setFlashLightOn(true)// 闪光灯是否显示
				.setTimelineTimeIndicator(true)// 时间提示是否显示
				.build();

		_CreateInfo.setSessionCreateInfo(create_info);
		_CreateInfo.setNextIntent(null);
		_CreateInfo.setOutputThumbnailSize(480, 480);// 输出图片宽高
		videoPath = Contant.VIDEOPATH;
		_CreateInfo.setOutputVideoPath(videoPath);// 输出视频路径
		_CreateInfo.setOutputThumbnailPath(Contant.THUMBPATH);// 输出图片路径

		QupaiServiceImpl qupaiService = new QupaiServiceImpl.Builder()
				.setEditorCreateInfo(_CreateInfo).build();
		qupaiService.showRecordPage(this, QUPAI_RECORD_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			EditorResult result = new EditorResult(data);
			// 得到视频path，和缩略图path的数组，返回十张缩略图,和视频时长
			Log.d(TAG, result.getPath());
			Log.d(TAG, result.getThumbnail().toString());
			Log.d(TAG, result.getDuration().toString());

			FileUtils.folderScan(LauncherActivity.this, Contant.FILEPATH);

			// 开始上传，上传前请务必确认已调用授权接口
			startUpload();

			// 删除草稿
			QupaiDraftManager draftManager = new QupaiDraftManager();
			draftManager.deleteDraft(data);
		}
	}

	private String videoPath;

	/**
	 * 开始鉴权。我们建议只鉴权一次 在application里面做。保存accessToken
	 * 
	 * @param context
	 * @param appKey
	 *            应用app key
	 * @param appsecret
	 *            应用 app secret
	 * @param space
	 *            存储空间名 最长32位 SDK需提供生成策略
	 */
	private void initAuth(Context context, String appKey, String appsecret,
			String space) {
		AuthService service = AuthService.getInstance();
		service.setQupaiAuthListener(new QupaiAuthListener() {
			@Override
			public void onAuthError(int errorCode, String message) {
				Log.e(TAG, "ErrorCode " + errorCode + " message " + message);
			}

			@Override
			public void onAuthComplte(int responseCode, String responseMessage) {
				Contant.accessToken = responseMessage;
				Log.e(TAG, "responseCode " + responseCode + " message " + responseMessage);
			}
		});
		service.startAuth(context, appKey, appsecret, space);
	}

	/**
	 * 创建一个上传任务
	 * 
	 * @param context
	 * @param uuid
	 *            随机生成的UUID
	 * @param _VideoFile
	 *            完整视频文件
	 * @param _Thumbnail
	 *            缩略图
	 * @param accessToken
	 *            通过调用鉴权得到token
	 * @param space
	 *            开发者生成的Quid，必须要和token保持一致
	 * @param share
	 *            是否公开 0公开分享 1私有(default) 公开类视频不需要AccessToken授权
	 * @param tags
	 *            标签 多个标签用 "," 分隔符
	 * @param description
	 *            视频描述
	 * @return
	 */
	private QupaiUploadTask createUploadTask(Context context, String uuid,
			File _VideoFile, File _Thumbnail, String accessToken, String space,
			int share, String tags, String description) {
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
			public void onUploadProgress(String uuid, long uploadedBytes,
					long totalBytes) {
				int percentsProgress = (int) (uploadedBytes * 100 / totalBytes);
				Log.e(TAG, "uuid:" + uuid + "data:onUploadProgress"
						+ percentsProgress);
				progress.setProgress(percentsProgress);
			}

			@Override
			public void onUploadError(String uuid, int errorCode, String message) {
				Log.e(TAG, "uuid:" + uuid + "onUploadError" + errorCode
						+ message);
			}

			@Override
			public void onUploadComplte(String uuid, int responseCode,
					String responseMessage) {
				// http://{DOMAIN}/v/{UUID}.mp4?token={ACCESS-TOKEN}
				progress.setVisibility(View.GONE);
				btn_open_video.setVisibility(View.VISIBLE);

				// 这里返回的uuid是你创建上传任务时生成的uuid.开发者可以使用其他作为标识
				// videoUrl返回的是上传成功的video地址
				videoUrl = Contant.domain + "/v/" + responseMessage + ".mp4"
						+ "?token=" + Contant.accessToken;
				Log.i(TAG, "data:onUploadComplte" + "uuid:" + uuid
						+ Contant.domain + "/v/" + responseMessage + ".jpg"
						+ "?token=" + Contant.accessToken);
				Log.i(TAG, "data:onUploadComplte" + "uuid:" + uuid
						+ Contant.domain + "/v/" + responseMessage + ".mp4"
						+ "?token=" + Contant.accessToken);
			}
		});
		String uuid = UUID.randomUUID().toString();
		startUpload(createUploadTask(this, uuid, new File(videoPath), new File(
				Contant.THUMBPATH), Contant.accessToken, Contant.space,
				Contant.shareType, Contant.tags, Contant.description));
	}

	/**
	 * 开始上传
	 * 
	 * @param data
	 *            上传任务的task
	 */
	private void startUpload(QupaiUploadTask data) {
		try {
			UploadService uploadService = UploadService.getInstance();
			uploadService.startUpload(data);
		} catch (IllegalArgumentException exc) {
			Log.d(TAG, "Missing some arguments. " + exc.getMessage());
		}
	}
}
