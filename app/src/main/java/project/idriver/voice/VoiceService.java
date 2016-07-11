/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package project.idriver.voice;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import project.idriver.R;
import project.idriver.beans.AtosBean;
import project.idriver.beans.CommandBean;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.MessageBean;
import project.idriver.nets.ZmqService;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class VoiceService implements RecognitionListener {

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String TAG = "VoiceService";
    private static final String COMMAND_SEARCH = "command";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "oh mighty computer";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private boolean isStop = false;
    private Context context;
    private static VoiceService voiceService = null;
    private ZmqService zmqService;

    private VoiceService(final Context context) {
        this.context = context;
        captions = new HashMap<String, Integer>();
        captions.put(COMMAND_SEARCH, R.string.idriver_cmd);
        zmqService = ZmqService.getInstance(mHandler);

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(context);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    Log.e(TAG, "Failed");
                }
            }
        }.execute();
    }

    public static VoiceService getInstance(Context context) {
        if (voiceService == null) {
            voiceService = new VoiceService(context);
        }
        return voiceService;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public void start() {
        if (recognizer != null) {
            isStop = false;
            recognizer.startListening(COMMAND_SEARCH, 2000);
        }
    }

    public void stop() {
        if (recognizer != null) {
            isStop = true;
            recognizer.stop();
        }
    }

    public void destroy() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();

        Log.i(TAG, "partial result: " + text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            String cmd = null;
            if (text.equals("启动")) {
                cmd = "启动";
            } else if (text.equals("停止")) {
                cmd = "停止";
            } else if (text.equals("加速")) {
                cmd = "加速";
            } else if (text.equals("减速")) {
                cmd = "减速";
            } else {
                //no-op
            }
            //找到命令
            if (cmd != null) {
                //向Server发送语音控制命令
                CommandBean cmdBean = new CommandBean();
                cmdBean.setVoice(cmd);
                AtosBean atos = new AtosBean();
                atos.setCommand(cmdBean);
                MessageBean msg = new MessageBean();
                msg.setAtos(atos);
                String jsonStr = GsonUtil.toJson(msg);
                Log.i(TAG, jsonStr);
                zmqService.publishMsg(jsonStr);
            }
            Log.i(TAG, "result : " + text);

        }
        if (!isStop) {
            recognizer.startListening(COMMAND_SEARCH, 2000);
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "end of speech");
        recognizer.stop();

    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "zh_broadcastnews_ptm256_8000"))
                .setDictionary(new File(assetsDir, "test.dic"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                        //.setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);

        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(COMMAND_SEARCH, menuGrammar);

    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "Error: " + error.getMessage());
    }

    @Override
    public void onTimeout() {
        //switchSearch(KWS_SEARCH);
        recognizer.stop();

    }
}
