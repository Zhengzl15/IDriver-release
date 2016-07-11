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
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.tangsci.android.tts.TtsPlayer;
import com.tangsci.tts.TtsEngine;

import java.io.IOException;
import java.io.InputStream;

import project.idriver.R;
import project.idriver.beans.AtosBean;
import project.idriver.beans.CommandBean;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.MessageBean;
import project.idriver.nets.NetConfig;
import project.idriver.nets.ZmqService;
import project.idriver.voice.VoiceService;


/**
 * Created by apple on 16-1-19.
 */
public class ControlFragment extends Fragment {

    private VoiceService voiceService;
    private ZmqService zmqService;
    //private BDTTSController mBDTTSController;
    private SlideButton mapChoose;
/*
    private Handler m_handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            ///合成线程会在播放完当前文本段后，会发一个播放完成的消息，这个函数负责接收处理
            /*
            super.handleMessage(msg);
            Bundle b = msg.getData();
            String playState = b.getString("play_state");*/
/*
        }
    }; */

    private TtsPlayer m_ttsPlayer = new TtsPlayer();

    private boolean initTtsPlay()
    {
        byte[] ttsResBytes;
        InputStream ttsResStream = getResources().openRawResource(R.raw.ttsres);
        try {
            ttsResBytes = new byte[ttsResStream.available()];
            ttsResStream.read(ttsResBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return m_ttsPlayer.initEngine(ttsResBytes);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //handle status message
            switch (msg.what) {
                case NetConfig.ZMQ_ID:
                    String content = (String) msg.obj;
                    MessageBean message = GsonUtil.fromJson(content, MessageBean.class);
                    if(message != null && message.getStoa() != null &&
                            message.getStoa().getStatus() != null && message.getStoa().getStatus().getVoice() != null) {
                        String voice = message.getStoa().getStatus().getVoice();
                        //Toast.makeText(getActivity(), voice, Toast.LENGTH_LONG).show();
                        //mBDTTSController.speek(voice);
                        Log.i("voice", voice);
                        m_ttsPlayer.playText(voice);
                    }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        voiceService = VoiceService.getInstance(getActivity());
        zmqService = ZmqService.getInstance(mHandler);
        //mBDTTSController = BDTTSController.getInstance(getActivity().getApplicationContext(), getResources().getAssets());
        initTtsPlay();
        ///该license code将于2018年1月1日到期
        m_ttsPlayer.setGlobalParam("LicenseCode", "GH4V980IOG37H0ADU6IN7HO3");

        m_ttsPlayer.setParam("Encoding", TtsEngine.ENCODING_UTF8);///输入文本是"utf8"编码
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.control_fragment, container);

        //Voice control
        final ToggleButton voiceBtn = (ToggleButton)view.findViewById(R.id.voice_btn);
        final ToggleButton controlBtn = (ToggleButton)view.findViewById(R.id.control_btn);
        final ToggleButton startBtn = (ToggleButton)view.findViewById(R.id.start_btn);
        final ToggleButton stopBtn = (ToggleButton)view.findViewById(R.id.parking_btn);
        final ImageButton speedUp = (ImageButton)view.findViewById(R.id.speedup_btn);
        final ImageButton speedDown = (ImageButton)view.findViewById(R.id.speeddown_btn);
        mapChoose = (SlideButton) view.findViewById(R.id.map_choose_btn);

        voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(voiceBtn.isChecked()) {
                    voiceService.start();
                } else {
                    voiceService.stop();
                }
            }
        });
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controlBtn.isChecked()) {
                    //startBtn.setButtonDrawable(R.drawable.start_green_100x100);
                    startBtn.setChecked(true);
                    stopBtn.setChecked(false);
                    //stopBtn.setButtonDrawable(R.drawable.parking_grey_100x100);
                    speedUp.setImageResource(R.drawable.speedup_grey_100x100);
                    speedDown.setImageResource(R.drawable.speeddown_grey_100x100);
                    startBtn.setClickable(true);
                    stopBtn.setClickable(false);
                    speedUp.setClickable(true);
                    speedDown.setClickable(true);

                    CommandBean cmdBean = new CommandBean();
                    cmdBean.setStartbutton("1");
                    AtosBean atos = new AtosBean();
                    atos.setCommand(cmdBean);
                    MessageBean msg = new MessageBean();
                    msg.setAtos(atos);
                    String jsonStr = GsonUtil.toJson(msg);
                    Log.i("ZMQ", jsonStr);
                    zmqService.publishMsg(jsonStr);
                } else {
                    //startBtn.setButtonDrawable(R.drawable.start_grey_100x100);
                    //stopBtn.setButtonDrawable(R.drawable.parking_grey_100x100);
                    //Toast.makeText(getActivity(), "close", Toast.LENGTH_LONG).show();
                    startBtn.setChecked(false);
                    stopBtn.setChecked(false);
                    speedUp.setImageResource(R.drawable.speedup_grey_100x100);
                    speedDown.setImageResource(R.drawable.speeddown_grey_100x100);
                    startBtn.setClickable(false);
                    stopBtn.setClickable(false);
                    speedUp.setClickable(false);
                    speedDown.setClickable(false);

                    CommandBean cmdBean = new CommandBean();
                    cmdBean.setStartbutton("0");
                    AtosBean atos = new AtosBean();
                    atos.setCommand(cmdBean);
                    MessageBean msg = new MessageBean();
                    msg.setAtos(atos);
                    String jsonStr = GsonUtil.toJson(msg);
                    Log.i("ZMQ", jsonStr);
                    zmqService.publishMsg(jsonStr);
                }
            }
        });
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startBtn.isChecked()) {

                } else {
                    startBtn.setClickable(false);
                    startBtn.setChecked(false);
                    stopBtn.setClickable(true);
                    stopBtn.setChecked(true);
                    speedUp.setClickable(true);
                    speedUp.setImageResource(R.drawable.speedup_btn);
                    speedDown.setClickable(true);
                    speedDown.setImageResource(R.drawable.speeddown_btn);
                    CommandBean cmdBean = new CommandBean();
                    cmdBean.setButton("0");
                    AtosBean atos = new AtosBean();
                    atos.setCommand(cmdBean);
                    MessageBean msg = new MessageBean();
                    msg.setAtos(atos);
                    String jsonStr = GsonUtil.toJson(msg);
                    zmqService.publishMsg(jsonStr);
                }
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stopBtn.isChecked()) {

                } else {
                    stopBtn.setClickable(false);
                    stopBtn.setChecked(false);
                    startBtn.setClickable(true);
                    startBtn.setChecked(true);
                    speedUp.setClickable(false);
                    speedUp.setImageResource(R.drawable.speedup_grey_100x100);
                    speedDown.setClickable(false);
                    speedDown.setImageResource(R.drawable.speeddown_grey_100x100);
                    CommandBean cmdBean = new CommandBean();
                    cmdBean.setButton("1");
                    AtosBean atos = new AtosBean();
                    atos.setCommand(cmdBean);
                    MessageBean msg = new MessageBean();
                    msg.setAtos(atos);
                    String jsonStr = GsonUtil.toJson(msg);
                    zmqService.publishMsg(jsonStr);
                }
            }
        });

        speedUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandBean cmdBean = new CommandBean();
                cmdBean.setButton("2");
                AtosBean atos = new AtosBean();
                atos.setCommand(cmdBean);
                MessageBean msg = new MessageBean();
                msg.setAtos(atos);
                String jsonStr = GsonUtil.toJson(msg);
                zmqService.publishMsg(jsonStr);
            }
        });
        speedDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommandBean cmdBean = new CommandBean();
                cmdBean.setButton("3");
                AtosBean atos = new AtosBean();
                atos.setCommand(cmdBean);
                MessageBean msg = new MessageBean();
                msg.setAtos(atos);
                String jsonStr = GsonUtil.toJson(msg);
                zmqService.publishMsg(jsonStr);
            }
        });

        startBtn.setChecked(false);
        stopBtn.setChecked(false);
        startBtn.setClickable(false);
        stopBtn.setClickable(false);
        speedUp.setClickable(false);
        speedDown.setClickable(false);

        return view;
    }

    public void setMapChooseListener(OnToggleStateChangedListener listener) {
        mapChoose.setmChangedListener(listener);
    }
}
