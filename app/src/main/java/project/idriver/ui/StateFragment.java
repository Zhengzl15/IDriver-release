package project.idriver.ui;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import project.idriver.R;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.MessageBean;
import project.idriver.beans.StatusBean;
import project.idriver.beans.StoaBean;
import project.idriver.nets.NetConfig;
import project.idriver.nets.ZmqService;


/**
 * Created by apple on 16-1-18.
 */
public class StateFragment extends Fragment {
    private ZmqService zmqService;
    ImageView autoDriving;
    ImageView gps;
    ImageView radar;
    ImageView lidar;
    ImageView camera;
    ImageView eps;
    ImageView evb;
    ImageView steer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        zmqService = ZmqService.getInstance(mHandler);
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NetConfig.ZMQ_ID:
                    String content = (String)msg.obj;
                    MessageBean messageBean = GsonUtil.fromJson(content, MessageBean.class);
                    StoaBean stoa = messageBean.getStoa();
                    if (stoa != null) {
                        StatusBean status = stoa.getStatus();
                        if (status != null) {
                            String indicator = status.getIndicator();
                            //Log.i("indicator", indicator);
                            if (indicator != null) {
                                char[] chars = indicator.toCharArray();

                                if (chars[0] == '1') {
                                    autoDriving.setBackground(getResources().getDrawable(R.drawable.light_green_48));
                                } else {
                                    autoDriving.setBackground(getResources().getDrawable(R.drawable.light_grey_48));
                                }

                                if (chars[1] == '1') {
                                    gps.setBackground(getResources().getDrawable(R.drawable.light_green_48));
                                } else {
                                    gps.setBackground(getResources().getDrawable(R.drawable.light_grey_48));
                                }

                                if (chars[2] == '1') {
                                    radar.setBackground(getResources().getDrawable(R.drawable.light_green_48));
                                } else {
                                    radar.setBackground(getResources().getDrawable(R.drawable.light_grey_48));
                                }

                                if (chars[3] == '1') {
                                    lidar.setBackground(getResources().getDrawable(R.drawable.light_green_48));
                                } else {
                                    lidar.setBackground(getResources().getDrawable(R.drawable.light_grey_48));
                                }

                                if (chars[4] == '1') {
                                    camera.setBackground(getResources().getDrawable(R.drawable.light_green_48));
                                } else {
                                    camera.setBackground(getResources().getDrawable(R.drawable.light_grey_48));
                                }

                                if (chars[5] == '1') {
                                    eps.setBackground(getResources().getDrawable(R.drawable.light_green_48));
                                } else {
                                    eps.setBackground(getResources().getDrawable(R.drawable.light_grey_48));
                                }

                                if (chars[6] == '1') {
                                    evb.setBackground(getResources().getDrawable(R.drawable.light_green_48));
                                } else {
                                    evb.setBackground(getResources().getDrawable(R.drawable.light_grey_48));
                                }
                            }
                        }
                    }
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.state_fragment, container);
        autoDriving = (ImageView)view.findViewById(R.id.autoDriving);
        gps = (ImageView)view.findViewById(R.id.gps);
        radar = (ImageView)view.findViewById(R.id.radar);
        lidar = (ImageView)view.findViewById(R.id.lidar);
        camera = (ImageView)view.findViewById(R.id.camera);
        eps = (ImageView)view.findViewById(R.id.eps);
        evb = (ImageView)view.findViewById(R.id.evb);

        steer = (ImageView)view.findViewById(R.id.steering_wheel_btn);

        return view;
    }

    public void setSteerListener(View.OnClickListener listener) {
        steer.setOnClickListener(listener);
    }
}