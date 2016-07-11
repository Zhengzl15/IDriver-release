package project.idriver.beans;

/**
 * Created by ryan_wu on 16/1/25.
 */
public class StatusBean {
    /**
     * status bean
     */
    private String indicator;
    private String voice;

    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }
    public String getIndicator() {
        return this.indicator;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }
    public String getVoice() {
        return this.voice;
    }
}
