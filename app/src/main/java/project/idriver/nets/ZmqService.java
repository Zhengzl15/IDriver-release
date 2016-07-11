package project.idriver.nets;

/**
 * @Authon Zhilong Zheng
 * @Email zhengzl0715@163.com
 * @Date 21:22 16/1/21
 */

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import otherpackages.org.zeromq.ZMQ;
import otherpackages.org.zeromq.ZMQ.Socket;

public class ZmqService {
    // Debugging
    private static final String TAG = "ZmqService";

    // Member fields
    private static ArrayList<Handler> mHandlers = new ArrayList<Handler>();
    private SubscriberThread mSubscriber;
    private PublishThread mPublish;
    private static ZmqService zmqService;

    private ZmqService() {
        //Log.i(TAG, "Get a service signal");
        //mHandlerhandler;
        start();
    }

    public static ZmqService getInstance(Handler handler) {
        mHandlers.add(handler);
        if (zmqService == null) {
            zmqService = new ZmqService();
        }
        return zmqService;
    }

    protected synchronized void start() {
        Log.d(TAG, "start");

        if (mSubscriber != null) {
            mSubscriber.cancel();
            mSubscriber = null;
        }

        if (mPublish != null) {
            mPublish.cancel();
            mPublish = null;
        }

        if (mSubscriber == null) {
            mSubscriber = new SubscriberThread();
            mSubscriber.start();
            Log.d(TAG, "Subscriber start");
        }
        if (mPublish == null) {
            mPublish = new PublishThread();
            mPublish.start();
            Log.d(TAG, "Publish start");
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mSubscriber != null) {
            mSubscriber.cancel();
            mSubscriber = null;
        }

        if (mPublish != null) {
            mPublish.cancel();
            mPublish = null;
        }
    }

    public void publishMsg(String msg) {
        // Create temporary object
        PublishThread r;
        // Synchronize a copy of the PublishThread
        synchronized (this) {
            if (mPublish == null) return;
            r = mPublish;
        }
        // Perform the write unsynchronized
        r.write(msg);
    }


    //Subscriber (Receiver)
    private class SubscriberThread extends Thread {
        private ZMQ.Context context;
        private Socket subscriber;
        private boolean isStop = false;

        public void run() {
            context = ZMQ.context(1);
            subscriber = context.socket(ZMQ.SUB);
            subscriber.connect(NetConfig.SERVER_URL);
            subscriber.subscribe("".getBytes());
            Log.i(TAG, "Subscriber connected");

            while (true) {
                String content = subscriber.recvStr(0);

                for (Handler handler: mHandlers) {
                    handler.obtainMessage(NetConfig.ZMQ_ID, -1, -1, content)
                            .sendToTarget();
                }

                Log.i(TAG, "Received : " + content);
                if (isStop) {
                    subscriber.close();
                    context.term();
                }
            }


        }

        public void cancel() {
            isStop = true;
        }
    }


    //Publish (Sender)
    private class PublishThread extends Thread {
        private ZMQ.Context context;
        private Socket publisher;
        private boolean stateWrite = false;
        private String msg = "";
        private boolean isStop = false;
        public void run() {
            context = ZMQ.context(1);
            publisher = context.socket(ZMQ.PUB);
            publisher.bind(NetConfig.APP_URL);

            while ((!Thread.currentThread ().isInterrupted ())) {
                if (stateWrite) {
                    publisher.send(msg, 0);
                    stateWrite = false;
                }
                if (isStop) {
                    publisher.close();
                    //context.term();
                }
            }
            publisher.close();
            context.term ();

        }

        public void write(String msg) {
            stateWrite = true;
            this.msg = msg;
        }

        public void cancel() {
            isStop = true;
        }
    }
}

