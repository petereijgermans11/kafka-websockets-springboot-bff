package nl.prorail.tps.producer.model;

import java.util.List;

public class InfraToestandBericht {

    private String wsViewStatus;
    private String gebiedNaam;
    private String state;
    private List<InfraToestandEntry> infraToestandList;
    private String timestamp;

    public InfraToestandBericht() {}

    public InfraToestandBericht(String wsViewStatus, String gebiedNaam, String state,
                                List<InfraToestandEntry> infraToestandList,
                                String timestamp) {
        this.wsViewStatus = wsViewStatus;
        this.gebiedNaam = gebiedNaam;
        this.state = state;
        this.infraToestandList = infraToestandList;
        this.timestamp = timestamp;
    }

    public String getWsViewStatus() { return wsViewStatus; }
    public void setWsViewStatus(String wsViewStatus) { this.wsViewStatus = wsViewStatus; }

    public String getGebiedNaam() { return gebiedNaam; }
    public void setGebiedNaam(String gebiedNaam) { this.gebiedNaam = gebiedNaam; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public List<InfraToestandEntry> getInfraToestandList() { return infraToestandList; }
    public void setInfraToestandList(List<InfraToestandEntry> infraToestandList) { this.infraToestandList = infraToestandList; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
