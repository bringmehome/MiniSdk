package com.duanqu.qupai.minisdk;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.duanqu.qupai.minisdk.common.Contant;
import com.duanqu.qupai.upload.AuthService;
import com.duanqu.qupai.upload.QupaiAuthListener;

public class MiniApplication extends Application {
    private static final String AUTHTAG = "QupaiAuth";

    @Override
    public void onCreate() {
        super.onCreate();
        initAuth(getApplicationContext(),Contant.appkey,Contant.appsecret,Contant.space);
    }

    /**
     * 鉴权 建议只调用一次,在Application调用。在demo里面为了测试调用了多次 得到accessToken
     * @param context
     * @param appKey    appkey
     * @param appsecret appsecret
     * @param space     space
     */
    private void initAuth(Context context ,String appKey,String appsecret,String space){
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

}
