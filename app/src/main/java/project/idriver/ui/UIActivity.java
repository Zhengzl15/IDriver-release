package project.idriver.ui;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import project.idriver.R;
import project.idriver.beans.AtosBean;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.MapBean;
import project.idriver.beans.MessageBean;
import project.idriver.beans.StoaBean;
import project.idriver.nets.MapZmq;
import project.idriver.nets.NetConfig;
import project.idriver.nets.ZmqService;
import project.idriver.steering.SteeringDialog;


public class UIActivity extends FragmentActivity {
    private final String TAG = "UIActivity";
    private GlobalFragment mGlobalFragment;

    private ViewFlipper allFlipper;

    //zmq service;
    private ZmqService mZmqService;
    private MapZmq mapZmq;

    //map file
    File mapFile = null;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1:
                    //Check the version of custom map compared to server
                    File file = null;
                    File versionFile = null;
                    try {
                        String sdPath = "";
                        boolean sdCardExist = Environment.getExternalStorageState()
                                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
                        if (sdCardExist) {
                            sdPath = Environment.getExternalStorageDirectory().toString();
                        }
                        Log.i(TAG, sdPath);
                        //Check path
                        file = new File(sdPath + UiConfig.DATA_PATH);
                        Log.i(TAG, "File path : " + sdPath + UiConfig.DATA_PATH);
                        if (!file.exists()) {
                            Log.i(TAG, "Mk dir");
                            file.mkdirs();
                        }

                        //check version file
                        versionFile = new File(sdPath + UiConfig.DATA_PATH + UiConfig.VERSION_DATA);
                        if (!versionFile.exists()) {
                            versionFile.createNewFile();
                        }

                        //Check version
                        BufferedReader buf = new BufferedReader(new FileReader(versionFile));
                        String version = buf.readLine();
                        //First time to use
                        if (version == null) {
                            version = "0";
                        }
                        MapBean map = new MapBean();
                        map.setVersion(version);
                        AtosBean atos = new AtosBean();
                        atos.setMap(map);
                        MessageBean sending = new MessageBean();
                        sending.setAtos(atos);
                        String jsonStr = GsonUtil.toJson(sending);
                        Log.i(TAG, jsonStr);
                        //System.out.println(GsonUtil.toJson(stoa));
                        mZmqService.publishMsg(jsonStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //切换到主页面
                    allFlipper.setDisplayedChild(1);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui);
        allFlipper = (ViewFlipper) findViewById(R.id.allFlipper);

        mZmqService = ZmqService.getInstance(zmqServerHandler);
        mapZmq = MapZmq.getInstance(mapZmqHandler);
        //mZmqService.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1); //给UI主线程发送消息
            }
        }, 4000); //启动等待2秒钟
        setDefaultGlobalFragment();
    }

    private void setDefaultGlobalFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mGlobalFragment = new GlobalFragment();
        transaction.add(R.id.id_navi_framelayout, mGlobalFragment, "navi_map");
        transaction.commit();

        ControlFragment cFragment = (ControlFragment) fm.findFragmentById(R.id.control_fragment);
        cFragment.setMapChooseListener(mGlobalFragment);
        StateFragment stateFragment = (StateFragment)fm.findFragmentById(R.id.state_fragment);
        stateFragment.setSteerListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SteeringDialog timePickerDialog = SteeringDialog.newInstance(getApplicationContext());

                timePickerDialog.show(getSupportFragmentManager(), "f");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destory");
        //mZmqService.stop();
        //mapZmq.stop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK && event.getRepeatCount() == 0) {
            showTips();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showTips() {

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("提示").setMessage("是否退出程序")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "exit");
                        System.exit(0);
                    }

                }).setNegativeButton("取消",

                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).create(); // 创建对话框
        alertDialog.show(); // 显示对话框
    }


    private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    Log.i(TAG, "Yes");
                    MapBean map = new MapBean();
                    map.setAck_update("1");
                    AtosBean atos = new AtosBean();
                    atos.setMap(map);
                    MessageBean sending = new MessageBean();
                    sending.setAtos(atos);
                    String jsonStr = GsonUtil.toJson(sending);
                    mZmqService.publishMsg(jsonStr);
                    Toast.makeText(getApplicationContext(), "开始下载数据包...", Toast.LENGTH_SHORT).show();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    Log.i(TAG, "No");
                    //mZmqService.publishMsg("0");
                    break;
            }
        }
    };

    private final Handler zmqServerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    //Handle the message from ZMQ service
    private final Handler mapZmqHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NetConfig.ZMQ_ID:
                    String content = (String)msg.obj;
                    MessageBean messageBean = GsonUtil.fromJson(content, MessageBean.class);
                    StoaBean stoa = messageBean.getStoa();
                    if (stoa != null) {
                        MapBean mapBean = stoa.getMap();
                        if (mapBean != null) {
                            if (mapBean.getIs_update() != null) {
                                Log.i(TAG, "Is update : " + mapBean.getIs_update());
                                //get version
                                String newVersion = mapBean.getIs_update();
                                String sdPath = "";
                                boolean sdCardExist = Environment.getExternalStorageState()
                                        .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
                                if (sdCardExist) {
                                    sdPath = Environment.getExternalStorageDirectory().toString();
                                }
                                try {
                                    File file = new File(sdPath + UiConfig.DATA_PATH + UiConfig.VERSION_DATA);
                                    if(!file.exists()) {
                                        file.createNewFile();
                                    }
                                    FileOutputStream outFile = new FileOutputStream(file);
                                    outFile.write(newVersion.getBytes());
                                    outFile.close();
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                                AlertDialog updateDialog = new AlertDialog.Builder(UIActivity.this)
                                        .setTitle("提示")
                                        .setMessage("定制地图数据包有更新,是否更新?" )
                                        .setPositiveButton("是" ,  listener)
                                        .setNegativeButton("否" , listener)
                                        .show();

                            } else if (mapBean.getData() != null) {
                                String data = mapBean.getData();
                                //Start to transfer map data
                                if (mapFile == null) {
                                    String sdPath = "";
                                    boolean sdCardExist = Environment.getExternalStorageState()
                                            .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
                                    if (sdCardExist) {
                                        sdPath = Environment.getExternalStorageDirectory().toString();
                                    }
                                    mapFile = new File(sdPath + UiConfig.DATA_PATH + UiConfig.MAP_DATA);
                                    if (!mapFile.exists()) {
                                        try {
                                            mapFile.createNewFile();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                if (data.equals("start")) {
                                    Log.i(TAG, "map start");
                                    //clear data
                                    try {
                                        FileOutputStream outFile = new FileOutputStream(mapFile);
                                        outFile.write("".getBytes());
                                        outFile.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                } else if (data.equals("end")) {
                                    //no-op
                                    Toast.makeText(getApplicationContext(), "地图数据下载完成", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.i(TAG, "map data");
                                    //write data
                                    BufferedWriter outWriter = null;
                                    try {
                                        outWriter = new BufferedWriter(new OutputStreamWriter(
                                                new FileOutputStream(mapFile, true)));
                                        Log.i(TAG, "data: " + data);
                                        outWriter.write(data);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            if (outWriter != null) {
                                                outWriter.close();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                        }
                    }
                    break;
                default:
                    //no-op
                    break;
            }

        }
    };

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_ui);
//        final ToggleButton myTogBtn = (ToggleButton) findViewById(R.id.myTogBtn);
//
//        myTogBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
////                Toast tst = new Toast(getBaseContext());
//                if (isChecked) {
////                    tst.setText("hi, yes");
//                } else {
////                    tst.setText("hello, no");
//                }
////                tst.show();
//            }
//        });
//    }



/*
    public static class PlaceholderFragment extends Fragment implements
            OnToggleStateChangedListener {

        private SlideButton slidebutton;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.activity_ui, container,
                    false);
            slidebutton = (SlideButton) rootView.findViewById(R.id.map_choose_btn);
            // 设置一下开关的状态
            slidebutton.setToggleState(true); // 设置开关的状态为打开
            slidebutton.setmChangedListener(this);

            return rootView;
        }

        @Override
        public void onToggleStateChanged(boolean state) {
            // TODO Auto-generated method stub
            FragmentActivity activity = (FragmentActivity) getActivity();
            if (state) {
                Toast.makeText(activity, "开关打开", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "开关关闭", Toast.LENGTH_SHORT).show();
            }
        }
    } */

}
