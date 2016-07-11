package project.idriver.beans;

/**
 * Created by ryan_wu on 16/1/26.
 */
public class MessageBean {
    private StoaBean stoa;
    private AtosBean atos;

    public void setStoa(StoaBean stoa) {
        this.stoa = stoa;
    }
    public StoaBean getStoa() {
        return this.stoa;
    }

    public void setAtos(AtosBean atos) {
        this.atos = atos;
    }
    public AtosBean getAtos() {
        return this.atos;
    }
}
