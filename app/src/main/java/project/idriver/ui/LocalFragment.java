package project.idriver.ui;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import project.idriver.R;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.Line1Bean;
import project.idriver.beans.Line2Bean;
import project.idriver.beans.LocalmapBean;
import project.idriver.beans.MessageBean;
import project.idriver.beans.StatusBean;
import project.idriver.beans.StoaBean;
import project.idriver.beans.TargetBean;
import project.idriver.beans.Text1Bean;
import project.idriver.beans.Text2Bean;
import project.idriver.nets.NetConfig;
import project.idriver.nets.ZmqService;


/**
 * Created by apple on 16-1-18.
 */
public class LocalFragment extends Fragment{
    private ZmqService zmqService;

    private ImageView sign;
    private ImageView light;
    private TextView text1;
    private TextView text2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.local_fragment, container);
        sign = (ImageView)view.findViewById(R.id.traffic_sign);
        light = (ImageView)view.findViewById(R.id.traffic_light);
        text1 = (TextView)view.findViewById(R.id.text_area_1);
        text2 = (TextView)view.findViewById(R.id.text_area_2);
        localView = (LocalView) view.findViewById(R.id.localView);
        return view;
    }

    private LocalView localView = null;
    private Intent intent = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zmqService = ZmqService.getInstance(mZmqHandler);
        getActivity().setContentView(R.layout.local_fragment);

        intent = getActivity().getIntent();
        if(intent != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(localView != null){
            localView.destroy();
            localView = null;
        }
    }

    private final Handler mZmqHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NetConfig.ZMQ_ID:
                    String content = (String) msg.obj;
                    MessageBean messageBean = GsonUtil.fromJson(content, MessageBean.class);
                    StoaBean stoa = messageBean.getStoa();
                    if (stoa != null) {
                        LocalmapBean localmap = stoa.getLocalmap();
                        if (localmap != null) {
                            String trafficsign = localmap.getTrafficsign();
                            if (trafficsign != null) {
                                //char[] signChars = trafficsign.toCharArray();
                                Log.i("traffic", "data " + trafficsign);
                                if (trafficsign.equals("1")) {
                                    sign.setImageResource(R.drawable.stright_96x96);
                                    sign.setRotation(0);
                                } else if (trafficsign.equals("2")) {
                                    Log.i("trafic", "t: 2");
                                    sign.setImageResource(R.drawable.left_up_96x96);
                                    sign.setRotation(45);
                                } else if (trafficsign.equals("3")) {
                                    sign.setImageResource(R.drawable.right_up_96x96);
                                    sign.setRotation(-45);
                                } else if (trafficsign.equals("4")) {
                                    sign.setImageResource(R.drawable.turn_left_96x96);
                                    sign.setRotation(0);
                                } else if (trafficsign.equals("5")) {
                                    sign.setImageResource(R.drawable.turn_right_96x96);
                                    sign.setRotation(0);
                                } else if (trafficsign.equals("6")) {
                                    sign.setImageResource(R.drawable.turn_around_96x96);
                                    sign.setRotation(0);
                                } else {// if (signChars[0]=='7') {
                                    sign.setImageResource(R.drawable.stop_96x96);
                                    sign.setRotation(0);
                                }
                            }

                            String trafficlight = localmap.getTrafficlight();
                            if (trafficlight != null) {
                                Log.i("light", "light: " + trafficlight);
                                if (trafficlight.equals("1")) {
                                    light.setImageResource(R.drawable.traffic_light_96x96_red);
                                } else if (trafficlight.equals("2")) {
                                    light.setImageResource(R.drawable.traffic_light_96x96_yellow);
                                } else if (trafficlight.equals("3")) {
                                    light.setImageResource(R.drawable.traffic_light_96x96_green);
                                }
                            }

                            Text1Bean text1bean = localmap.getText1();
                            if (text1bean != null) {
                                Log.i("text", "text: " + text1bean.getContent());
                                String text1Chars = text1bean.getContent();
                                if (text1Chars != null) {
                                    String[] strs1 = text1Chars.split(";");
                                    String output = "";
                                    for (String s : strs1) {
                                        String[] strs2 = s.split(":");
                                        output = output + strs2[0] + ":" + "\n" + strs2[1] + "\n";
                                    }
                                    text1.setText(output);
                                }
                            }

                            Text2Bean text2bean = localmap.getText2();
                            if (text2bean != null) {
                                String text2Chars = text2bean.getContent();
                                if (text2Chars != null) {
                                    String[] strs1 = text2Chars.split(";");
                                    String output = "";
                                    for (String s : strs1) {
                                        output = output + s + "\n";
                                    }
                                    text2.setText(output);
                                }
                            }

                            // 绘制局部地图中的 target
                            TargetBean target = localmap.getTarget();
                            if (target != null) {
                                String targetPosition = localmap.getTarget().getPosition();
                                String targetType = localmap.getTarget().getType();
                                if (targetPosition != null && targetType != null) {
                                    localView.setTargetContent(targetPosition);
                                    localView.setTargetType(targetType);
                                /*
                                    localView.setX(-18);
                                    localView.setY(25);
                                    localView.setT(1);
                                    char[] typeChars = targetType.toCharArray();
                                    String[] tp_strs = targetPosition.split(";");
                                    for (int i = 0; i < tp_strs.length; i++) {
                                        String[] xy = tp_strs[i].split(",");
                                        float x = Float.parseFloat(xy[0]);
                                        float y = Float.parseFloat(xy[1]);
                                        int t = 0;
                                        if (typeChars[i] == '0') {
                                            t = 0;
                                        } else if (typeChars[i] == '1') {
                                            t = 1;
                                        } else { // typeChars[i] == '2'
                                            t = 2;
                                        }

                                        // 每一个target对应于一组x,y,t，这里需要调用下绘制命令
                                        // 或者直接还是用LocalView.java里面的OnDraw自动刷新
                                        // 但是估计这里等自动刷新会有问题
                                        //localView.postInvalidate();
                                    }
                                }*/
                                }else {
                                    localView.setTargetContent(null);
                                    localView.setTargetType(null);
                                }
                            } else {
                                localView.setTargetContent(null);
                                localView.setTargetType(null);
                            }

                            // 绘制局部地图中的 曲线
                            Line1Bean l1 = localmap.getLine1();
                            if (l1 != null) {
                                String line1 = l1.getPosition();
                                if (line1 != null) {
                                    localView.setLine1(line1);
                                    /*
                                    Log.i("Line", "line: " + line1);
                                    String[] line1_positions = line1.split(";");
                                    for (int i = 0; i < line1_positions.length; i++) {
                                        String[] xy = line1_positions[i].split(",");
                                        float x = Float.parseFloat(xy[0]);
                                        float y = Float.parseFloat(xy[1]);
                                        localView.setX(x);
                                        localView.setY(y);
                                        localView.setT(3); // t==3, red little circle to build line1.
                                        //localView.postInvalidate();
                                    } */
                                } else {
                                    localView.setLine1(null);
                                }
                            } else {
                                localView.setLine1(null);
                            }

                            Line2Bean l2 = localmap.getLine2();
                            if (l2 != null) {
                                String line2 = l2.getPosition();
                                if (line2 != null) {
                                    localView.setLine2(line2);
                                    /*
                                    String[] line2_positions = line2.split(";");
                                    for (int i = 0; i < line2_positions.length; i++) {
                                        String[] xy = line2_positions[i].split(",");
                                        float x = Float.parseFloat(xy[0]);
                                        float y = Float.parseFloat(xy[1]);
                                        localView.setX(x);
                                        localView.setY(y);
                                        localView.setT(4); // t==4, green little circle to build line2.
                                        //startActivity(intent); // 直接调用这句重绘
                                    } */
                                } else {
                                    localView.setLine2(null);
                                }
                            } else {
                                localView.setLine2(null);
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

}
