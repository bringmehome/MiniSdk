package com.duanqu.qupai.minisdk.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.duanqu.qupai.bean.QupaiUploadTask;
import com.duanqu.qupai.sdk.R;

public class UploadAdapter extends BaseAdapter{

    private CommonAdapterHelper mCommonAdapterHelper;
    Context context;
    private Callback mCallback;

    public UploadAdapter(Context context, CommonAdapterHelper commonAdapterHelper,Callback callback) {
        super();
        this.context =context;
        this.mCommonAdapterHelper = commonAdapterHelper;
        this.mCallback = callback;
    }

    @Override
    public int getCount() {
        return  ((ArrayList<Object>)mCommonAdapterHelper.getItemList()).size();
    }

    @Override
    public Object getItem(int position) {
        return ((ArrayList<Object>)mCommonAdapterHelper.getItemList()).get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        QupaiUploadTask fileData = (QupaiUploadTask)getItem(position);
        ViewHolder viewHolder = null;
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(
                    R.layout.minisdj_upload_list_item, null);
            viewHolder =new ViewHolder();
            viewHolder.videoFile = (TextView)convertView.findViewById(R.id.tv_video);
            viewHolder.btnUpload = (Button)convertView.findViewById(R.id.btn_uplaod);
            viewHolder.tv_upload_process= (TextView) convertView.findViewById(R.id.tv_upload_process);
            viewHolder.btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.uploadOnClick(v,position);
                }
            });
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.videoFile.setText(fileData.getVideoFile());
        viewHolder.tv_upload_process.setText("" + fileData.getProgress() + "%");
        if(fileData.isUploadStatus()!=null){
            switch (fileData.isUploadStatus()){
                case UNUPLOAD:
                    viewHolder.btnUpload.setText(convertView.getResources().getString(R.string.upload));
                    break;
                case UPLOADING:
                    viewHolder.btnUpload.setText(convertView.getResources().getString(R.string.uploading));
                    break;
                case UPLOADED:
                    viewHolder.btnUpload.setText(convertView.getResources().getString(R.string.upload_complete));
                    break;
            }
        }

        return convertView;
    }

    public class ViewHolder{
        TextView videoFile;
        Button btnUpload;
        TextView tv_upload_process;
    }

   public interface Callback {
        void uploadOnClick(View v,int position);
   }

}
