package project.idriver.beans;

/**
 * Created by ryan_wu on 16/1/25.
 */
public class GlobalmapBean {
    /**
     * navigation message bean
     */
    public String currentRoadName;
    public String nextRoadName;
    public String currentOrder;
    public String nextOrder;
    public String leftLength;
    public String limitSpeed;
    public String roadClass;
    public String roadType;
    public String roadSize;
    public String gps;
    public LineBean line;

    public void setCurrentRoadName(String currentRoadName) {
        this.currentRoadName = currentRoadName;
    }
    public String getCurrentRoadName() {
        return this.currentRoadName;
    }

    public void setNextRoadName(String nextRoadName) {
        this.nextRoadName = nextRoadName;
    }
    public String getNextRoadName() {
        return this.nextRoadName;
    }

    public void setCurrentOrder(String currentOrder) {
        this.currentOrder = currentOrder;
    }
    public String getCurrentOrder() {
        return this.currentOrder;
    }

    public void setNextOrder(String nextOrder) {
        this.nextOrder = nextOrder;
    }
    public String getNextOrder() {
        return this.nextOrder;
    }

    public void setLeftLength(String leftLength) {
        this.leftLength = leftLength;
    }
    public String getLeftLength() {
        return this.leftLength;
    }

    public void setLimitSpeed(String limitSpeed) {
        this.limitSpeed = limitSpeed;
    }
    public String getLimitSpeed() {
        return this.limitSpeed;
    }

    public void setRoadClass(String roadClass) {
        this.roadClass = roadClass;
    }
    public String getRoadClass() {
        return this.roadClass;
    }

    public void setRoadType(String roadType) {
        this.roadType = roadType;
    }
    public String getRoadType() {
        return this.roadType;
    }

    public void setRoadSize(String roadSize) {
        this.roadSize = roadSize;
    }
    public String getRoadSize() {
        return this.roadSize;
    }

    public void setGps(String gps) {
        this.gps = gps;
    }
    public String getGps() {
        return this.gps;
    }

    public void setLine(LineBean line) {
        this.line = line;
    }
    public LineBean getLine() {
        return this.line;
    }

}
