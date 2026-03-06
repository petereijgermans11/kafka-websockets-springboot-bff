package nl.prorail.tps.producer.model;

public class InfraToestandEntry {

    private String infraObjectNaam;
    private String infraObjectType;
    private boolean bezet;
    private boolean gestoord;
    private boolean betrouwbaar;
    private boolean ligtInRijweg;

    public InfraToestandEntry() {}

    public InfraToestandEntry(String infraObjectNaam, String infraObjectType,
                               boolean bezet, boolean gestoord,
                               boolean betrouwbaar, boolean ligtInRijweg) {
        this.infraObjectNaam = infraObjectNaam;
        this.infraObjectType = infraObjectType;
        this.bezet = bezet;
        this.gestoord = gestoord;
        this.betrouwbaar = betrouwbaar;
        this.ligtInRijweg = ligtInRijweg;
    }

    public String getInfraObjectNaam() { return infraObjectNaam; }
    public void setInfraObjectNaam(String infraObjectNaam) { this.infraObjectNaam = infraObjectNaam; }

    public String getInfraObjectType() { return infraObjectType; }
    public void setInfraObjectType(String infraObjectType) { this.infraObjectType = infraObjectType; }

    public boolean isBezet() { return bezet; }
    public void setBezet(boolean bezet) { this.bezet = bezet; }

    public boolean isGestoord() { return gestoord; }
    public void setGestoord(boolean gestoord) { this.gestoord = gestoord; }

    public boolean isBetrouwbaar() { return betrouwbaar; }
    public void setBetrouwbaar(boolean betrouwbaar) { this.betrouwbaar = betrouwbaar; }

    public boolean isLigtInRijweg() { return ligtInRijweg; }
    public void setLigtInRijweg(boolean ligtInRijweg) { this.ligtInRijweg = ligtInRijweg; }
}
