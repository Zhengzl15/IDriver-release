package project.idriver.beans;

/**
 * Created by ryan_wu on 16/1/25.
 */
public class LocalmapBean {
    /**
     * local map information bean
     */
    private TargetBean target;
    private Line1Bean line1;
    private Line2Bean line2;
    private String trafficsign;
    private String trafficlight;
    private Text1Bean text1;
    private Text2Bean text2;

    public void setTarget(TargetBean target) {
        this.target = target;
    }
    public TargetBean getTarget() {
        return this.target;
    }

    public void setLine1(Line1Bean line1) {
        this.line1 = line1;
    }
    public Line1Bean getLine1() {
        return this.line1;
    }

    public void setLine2(Line2Bean line2) {
        this.line2 = line2;
    }
    public Line2Bean getLine2() {
        return this.line2;
    }

    public void setTrafficsign(String trafficsign) {
        this.trafficsign = trafficsign;
    }
    public String getTrafficsign() {
        return this.trafficsign;
    }

    public void setTrafficlight(String trafficlight) {
        this.trafficlight = trafficlight;
    }
    public String getTrafficlight() {
        return this.trafficlight;
    }

    public void setText1(Text1Bean text1) {
        this.text1 =text1;
    }
    public Text1Bean getText1() {
        return this.text1;
    }

    public void setText2(Text2Bean text2) {
        this.text2 = text2;
    }
    public Text2Bean getText2() {
        return this.text2;
    }
}
