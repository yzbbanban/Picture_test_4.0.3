package com.wangban.yzbbanban.picture_test_40.dao;

import android.os.AsyncTask;
import android.util.Log;

import com.wangban.yzbbanban.picture_test_40.contast.Contast;
import com.wangban.yzbbanban.picture_test_40.entity.DetialImage;
import com.wangban.yzbbanban.picture_test_40.util.JsoupUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZBbanban on 16/6/6.
 */
public class DetialPictureDao implements Contast{
    public DetialPictureDao() {
    }


    public void findDetilImageGridView(final Callback callback, final String webPath) {
        AsyncTask<String, String, List<DetialImage>> task = new AsyncTask<String, String, List<DetialImage>>() {
            List<DetialImage> images = new ArrayList<DetialImage>();

            @Override
            protected List<DetialImage> doInBackground(String... params) {
                try {
                    Log.i(TAG, "doInBackground: hello" + webPath);
                    images = JsoupUtil.downDetilLoadData(webPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return images;
            }
            @Override
            protected void onPostExecute(List<DetialImage> images) {
                callback.onImageDownload(images);
            }
        };
        task.execute();
    }

    public interface Callback {
        void onImageDownload(List<DetialImage> list);
    }


}
