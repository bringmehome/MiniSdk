package com.duanqu.qupai.minisdk.common;

import java.io.File;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

public class FileUtils {

    /**
     * 保存路径的文件夹名称
     */
    public static  String DIR_NAME = "qupai_video_test";

    /**
     * 给指定的文件名按照时间命名
     */
    private static  SimpleDateFormat OUTGOING_DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");


//    /**
//     * 得到指定的Video保存路径
//     * @return
//     */
//    public static File getDoneVideoPath(Context context) {
//        String str = OUTGOING_DATE_FORMAT.format(new Date());
//        File dir = new File(context.getFilesDir()
//                + File.separator + DIR_NAME + "/" + str);
//
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//
//        return dir;
//    }
//
//    private static File getStorageDir(Context context) {
//        // Get the directory for the app's private pictures directory.
//        File file = new File(context.getExternalFilesDir(
//                Environment.MEDIA_MOUNTED), "qupaiVideo");
//        if (!file.exists()) {
//            if (!file.mkdirs()) {
//                Log.e("TAG", "Directory not created");
//            }
//        }
//
//        return file;
//    }

    /**
     * 得到输出的Video保存路径
     * @return
     */
//    public static String newOutgoingFilePath(Context context) {
//        String str = OUTGOING_DATE_FORMAT.format(new Date());
//        String fileName = getStorageDir(context).getPath()+ "/" + File.separator + str + ".mp4";
//        return fileName;
//    }
    
    
	public static String newOutgoingFilePath() {
		// Save to SD card.
		String sdState = Environment.getExternalStorageState();
		String path = Environment.getExternalStorageDirectory().toString();
		File file = null;
		if (sdState.equals(Environment.MEDIA_MOUNTED)) {
			// Get sd card path.
			file = new File(path + "/Sins");
			if (!file.exists()) {
				file.mkdirs();
			}
		}
		return file.toString();
	}
	

	public static void fileScan(Context context,String file) {
		Uri data = Uri.parse("file://" + file);
//		Log.d("---QupaiAPI---", "file:" + file);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
	}

	public static void folderScan(Context context, String path) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] array = file.listFiles();
			for (int i = 0; i < array.length; i++) {
				File f = array[i];
				if (f.isFile()) {
					String name = f.getName();
					if (name.contains(Contant.FILENAMEHEAD)) {
						fileScan(context, f.getAbsolutePath());
					}
				} else {
					folderScan(context, f.getAbsolutePath());
				}
			}
		}
	}
}
