package com.wangban.yzbbanban.picture_test_40.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.wangban.yzbbanban.picture_test_40.R;
import com.wangban.yzbbanban.picture_test_40.app.ImgApplication;
import com.wangban.yzbbanban.picture_test_40.contast.Contast;
import com.wangban.yzbbanban.picture_test_40.entity.DetialImage;
import com.wangban.yzbbanban.picture_test_40.util.BitmapUtil;
import com.wangban.yzbbanban.picture_test_40.util.HttpUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ImageActivity extends Activity implements Contast, View.OnTouchListener {
    //init View class
    private ImageView ivSingle;
    private String imagePath;
    private Bitmap bm;
    private ProgressBar pb;
    private ProgressBar pbSave;
    private MyAsyncTask mTask;
    private ArrayList<DetialImage> images = new ArrayList<>();
    private ImgApplication app;
    //page next previous
    private int currentPosition;

    private String url;

    private Thread thread;
    //touch event
    private long mLastTime, mCurTime;
    private int count;
    private float mLastMotionX, mLastMotionY;
    private boolean isMoved;
    private long time, time2;
    private int moveStyle;

    //useless
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_SAVE_START:
                    pbSave.setVisibility(View.VISIBLE);
                    break;
                case HANDLER_SAVE_IMGER_TO_SD:
                    pbSave.setVisibility(View.GONE);
                    Toast.makeText(ImageActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);
        Log.w(TAG, "onCreate:hello ");
        initView();
        initData();
        // setView();
        setListener();
    }

    private void setListener() {
        ivSingle.setOnTouchListener(this);
    }

    private void setView() {

        //Log.i(TAG, "setView: " + imagePath);
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    InputStream is = HttpUtil.get(imagePath);
                    bm = BitmapFactory.decodeStream(is);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivSingle.setImageBitmap(bm);
                            Log.i(TAG, "run: " + bm);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        thread.start();

    }

    private void initData() {


        Intent intent = getIntent();
        //imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH);
        currentPosition = intent.getIntExtra(EXTRA_IMAGE_POSITION, 0);
        //Log.w(TAG, "initData: "+currentPosition );
        //Log.w(TAG, "initData:bbbbbbbbbbbb " + images.get(currentPosition).getHrefPath());

        asyncTaskStart(currentPosition);


    }

    private void asyncTaskStart(int position) {
        url = images.get(position).getHrefPath();
        mTask = new MyAsyncTask();
        mTask.execute(url);
    }

    private void initView() {
        app = (ImgApplication) getApplication();
        images = app.getImageData();
        currentPosition = app.getPosition();
        ivSingle = (ImageView) findViewById(R.id.iv_single);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pbSave= (ProgressBar) findViewById(R.id.pb_save);


    }

    @Override
    protected void onDestroy() {
        thread = null;
        super.onDestroy();
    }

    /**
     * touch event
     *
     * @param v
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                doubleClick();
                mLastMotionX = event.getX();
                mLastMotionY = event.getY();
                time = System.currentTimeMillis();
                // Log.e(TAG, "onTouch: 22222 " + time);
                isMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //移动超过阈值，则表示移动了
                if (isMoved) break;
                if (Math.abs(mLastMotionX - event.getX()) > TOUCH_STOP
                        || Math.abs(mLastMotionY - event.getY()) > TOUCH_STOP) {
                    time2 = System.currentTimeMillis();
                    if (mLastMotionX - event.getX()>TOUCH_MOVE) {
                        moveStyle = MOVE_TO_RIGHT;
                    } else if ( event.getX()-mLastMotionX>TOUCH_MOVE) {
                        moveStyle = MOVE_TO_LEFT;

                    }
                    isMoved = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                time2 = System.currentTimeMillis();
                Log.e(TAG, "onTouch: " + time2);
                if (time2 - time > 800) {
                    if (!isMoved) {
                        // Log.e(TAG, "onTouch: 长按");
                        //保存内容
                        dialogMenu();
                    }

                } else {
                    //Log.i(TAG, "onTouch: 滑动");
                    if (moveStyle == MOVE_TO_RIGHT) {
                        // Log.w(TAG, "onTouch: 下一张");
                        imageNext();
                    } else if (moveStyle == MOVE_TO_LEFT) {
                        //Log.w(TAG, "onTouch: 上一张");
                        imagePrevious();
                    }
                }
                break;
        }
        return true;
    }

    private void dialogMenu(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.icon_menu2).setTitle("保存").setMessage("您要保存吗？");
        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.sendEmptyMessage(HANDLER_SAVE_START);
                thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            saveImage2();
                             //Log.w(TAG, "run: 保存");
                            handler.sendEmptyMessage(HANDLER_SAVE_IMGER_TO_SD);
                        } catch (IOException e) {
                            // Toast.makeText(ImageActivity.this,"无法链接网络！", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    /**
     * previous image
     */

    private void imagePrevious() {
        currentPosition--;
        if (currentPosition <= 0) {
            currentPosition = images.size() - 1;
        }
        asyncTaskStart(currentPosition);
    }

    /**
     * next image
     */
    private void imageNext() {
        currentPosition++;
        if (currentPosition > images.size() - 1) {
            currentPosition = 0;
        }
        asyncTaskStart(currentPosition);
    }

    private void doubleClick() {
        mLastTime = mCurTime;
        mCurTime = System.currentTimeMillis();
        if (mCurTime - mLastTime < 600) {
            //Toast.makeText(this, "双击事件", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "doubleClick: 双击");
            finish();
            overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
        }
    }

    /**
     * useless
     *
     * @throws IOException
     */
    private void saveImageToSd() throws IOException {
        InputStream is = HttpUtil.get(imagePath);

        Bitmap bitmap = BitmapUtil.loadBitmap(is, 3, 683, 1024);

        String fileName = System.currentTimeMillis() + ".jpg";

        File dirFile = new File(BEAUTY_PATH);
        Log.w(TAG, "saveImageToSd: " + dirFile.getAbsolutePath());
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(BEAUTY_PATH + fileName);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        bos.flush();
        bos.close();
        //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + BEAUTY_PATH)));
    }

    /**
     * system suggest method
     *
     * @throws IOException
     */
    private void saveImage2() throws IOException {
        InputStream is = HttpUtil.get(images.get(currentPosition).getHrefPath());
        Bitmap bitmap = BitmapUtil.loadBitmap(is, 3, 683, 1024);
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", "description");
    }

    /**
     * bitmap async load
     */
    class MyAsyncTask extends AsyncTask<String, Integer, Bitmap> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pb.setVisibility(View.VISIBLE);

        }


        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                String url = params[0];
                InputStream is = HttpUtil.get(url);
                //Thread.sleep(2000);
                bm = BitmapFactory.decodeStream(is);
                return bm;
            } catch (IOException e) {
                e.printStackTrace();
            }
//            catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            for (int i = 0; i < 100; i++) {
                if (isCancelled()) {
                    break;
                }
                publishProgress(i);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            pb.setVisibility(View.GONE);
            ivSingle.setImageBitmap(bitmap);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (isCancelled()) {
                return;
            }
            pb.setProgress(values[0]);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
            //标记为 cancle 状态，不是取消执行
            mTask.cancel(true);
        }
    }


}
