package nl.prorail.tps.producer.model;

import java.util.List;

public class TreinPositieBericht {

    private String wsViewStatus;
    private String sbNaam;
    private String state;
    private List<TreinPositieEntry> treinNummerVensterBezettingList;
    private String timestamp;

    public TreinPositieBericht() {}

    public TreinPositieBericht(String wsViewStatus, String sbNaam, String state,
                                List<TreinPositieEntry> treinNummerVensterBezettingList,
                                String timestamp) {
        this.wsViewStatus = wsViewStatus;
        this.sbNaam = sbNaam;
        this.state = state;
        this.treinNummerVensterBezettingList = treinNummerVensterBezettingList;
        this.timestamp = timestamp;
    }

    public String getWsViewStatus() { return wsViewStatus; }
    public void setWsViewStatus(String wsViewStatus) { this.wsViewStatus = wsViewStatus; }

    public String getSbNaam() { return sbNaam; }
    public void setSbNaam(String sbNaam) { this.sbNaam = sbNaam; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public List<TreinPositieEntry> getTreinNummerVensterBezettingList() { return treinNummerVensterBezettingList; }
    public void setTreinNummerVensterBezettingList(List<TreinPositieEntry> list) { this.treinNummerVensterBezettingList = list; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
