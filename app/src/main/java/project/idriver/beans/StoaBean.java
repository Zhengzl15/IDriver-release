package project.idriver.beans;

import java.nio.channels.FileChannel;

/**
 * Created by ryan_wu on 16/1/25.
 */
public class StoaBean {
    /**
     * message from server to android bean
     */
    private StatusBean status = null;
    private LocalmapBean localmap = null;
    private GlobalmapBean globalmap = null;
    private MapBean map = null;

    public void setStatus(StatusBean status) {
        this.status = status;
    }

    public StatusBean getStatus() {
        return this.status;
    }

    public void setLocalmap(LocalmapBean localmap) {
        this.localmap = localmap;
    }
    public LocalmapBean getLocalmap() {
        return this.localmap;
    }

    public void setGlobalmap(GlobalmapBean globalmap) {
        this.globalmap = globalmap;
    }
    public GlobalmapBean getGlobalmap() {
        return this.globalmap;
    }

    public void setMap(MapBean map) {
        this.map = map;
    }
    public MapBean getMap() {
        return this.map;
    }
}
