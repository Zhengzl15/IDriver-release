package project.idriver.beans;

/**
 * @Authon Zhilong Zheng
 * @Email zhengzl0715@163.com
 * @Date 00:30 16/1/26
 */
public class MapBean {
    private String version;
    private String is_update;
    private String ack_update;
    private String data;

    public void setVersion(String version) {
        this.version = version;
    }
    public String getVersion() {
        return this.version;
    }

    public void setIs_update(String is_update) {
        this.is_update = is_update;
    }
    public String getIs_update() {
        return this.is_update;
    }

    public void setAck_update(String ack_update) {
        this.ack_update = ack_update;
    }
    public String getAck_update() {
        return this.ack_update;
    }

    public void setData(String data) {
        this.data = data;
    }
    public String getData() {
        return this.data;
    }
}
