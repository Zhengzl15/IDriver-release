package project.idriver.nets;

/**
 * @Authon Zhilong Zheng
 * @Email zhengzl0715@163.com
 * @Date 23:32 16/1/21
 */
public class NetConfig {
    public static final String SERVER_URL = "tcp://192.168.1.33:7004";
    public static final String MAP_URL = "tcp://192.168.1.33:7006";
//public static final String SERVER_URL = "tcp://192.168.43.96:7004";
//    public static final String MAP_URL = "tcp://192.168.43.96:7006";
    public static final String APP_URL = "tcp://*:7005";

    public static final int ZMQ_ID = 33;

    //Subscriber (ie, Received message)
    public static final String DOWNLOAD_MAP = "GET_MAP";
    public static final int DOWNLOAD_MAP_TYPE = 0;

    //Publish (ie, Sent message)
    public static final String SEND_VOICE_CTRL = "VOICE_CTRL";
    public static final String MAP_VERSION = "MAP_VERSION";

}
