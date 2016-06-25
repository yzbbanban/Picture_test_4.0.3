package com.wangban.yzbbanban.picture_test_40.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.wangban.yzbbanban.picture_test_40.R;
import com.wangban.yzbbanban.picture_test_40.activity.DetialActivity;
import com.wangban.yzbbanban.picture_test_40.adapter.MainImageAdapter;
import com.wangban.yzbbanban.picture_test_40.contast.Contast;
import com.wangban.yzbbanban.picture_test_40.dao.MainPictureDao;
import com.wangban.yzbbanban.picture_test_40.entity.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZBbanban on 16/6/5.
 */
public class FragmentPaper extends Fragment implements Contast,AdapterView.OnItemClickListener,SwipeRefreshLayout.OnRefreshListener {
    private GridView gvPaperImage;
    private MainImageAdapter mainImageAdapter;
    private List<Image> imgs;
    private SwipeRefreshLayout mSwipeLayout;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case REFRESH_COMPLETE:
                    MainPictureDao pic = new MainPictureDao();

                    pic.findImageGridView(new MainPictureDao.Callback() {

                        @Override
                        public void onImageDownload(List<Image> imgs) {
                            setAdapter(imgs);
                        }
                    }, PAPER);
                    mSwipeLayout.setRefreshing(false);
                    break;
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paper, container, false);
        initView(view);
        setListener();
        return view;
    }

    private void setListener() {
        gvPaperImage.setOnItemClickListener(this);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

    }

    private void initView(View view) {

        gvPaperImage = (GridView) view.findViewById(R.id.gv_paper);
        mSwipeLayout= (SwipeRefreshLayout) view.findViewById(R.id.id_swipe_paper);
        MainPictureDao pic = new MainPictureDao();

        pic.findImageGridView(new MainPictureDao.Callback() {

            @Override
            public void onImageDownload(List<Image> imgs) {
                setAdapter(imgs);
            }
        },PAPER);

    }

    private void setAdapter(List<Image> images) {
        imgs=images;
        mainImageAdapter = new MainImageAdapter(getActivity(), (ArrayList<Image>) images,gvPaperImage);
        gvPaperImage.setAdapter(mainImageAdapter);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), DetialActivity.class);
        intent.putExtra(EXTRA_PATH, imgs.get(position).getSkipPagePath());
        Log.i(TAG, "onItemClick: path: " + imgs.get(position).getSkipPagePath());
        startActivity(intent);
        setEnterTransition(getExitTransition());
    }

    @Override
    public void onRefresh() {
        handler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 3000);
    }
}
