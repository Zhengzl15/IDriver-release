package project.idriver.beans;

/**
 * Created by ryan_wu on 16/1/25.
 */
public class CommandBean {
    private String button;
    private String voice;
    private String steer;
    private String startbutton;

    public void setButton(String button) {
        this.button = button;
    }
    public String getButton() {
        return this.button;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }
    public String getVoice() {
        return this.voice;
    }

    public void setSteer(String steer) {
        this.steer = steer;
    }
    public String getSteer() {
        return this.steer;
    }

    public void setStartbutton (String startbutton) {
        this.startbutton = startbutton;
    }
    public String getStartbutton () {
        return this.startbutton;
    }
}
