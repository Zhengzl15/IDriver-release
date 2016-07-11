package project.idriver.beans;

/**
 * Created by ryan_wu on 16/1/25.
 */

public class AtosBean {
    /**
     * the android to server message bean
     */
    private CommandBean command = null;
    private GlobalmapBean globalmap = null;
    private MapBean map = null;

    public void setCommand(CommandBean command) {
        this.command = command;
    }
    public CommandBean getCommand() {
        return this.command;
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
