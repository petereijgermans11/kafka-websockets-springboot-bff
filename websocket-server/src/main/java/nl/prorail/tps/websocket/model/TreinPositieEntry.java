package nl.prorail.tps.websocket.model;

public class TreinPositieEntry {

    private String treinNummer;
    private String sectionId;
    private boolean betrouwbaar;
    private String internRitNummer;

    public TreinPositieEntry() {}

    public String getTreinNummer() { return treinNummer; }
    public void setTreinNummer(String treinNummer) { this.treinNummer = treinNummer; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public boolean isBetrouwbaar() { return betrouwbaar; }
    public void setBetrouwbaar(boolean betrouwbaar) { this.betrouwbaar = betrouwbaar; }

    public String getInternRitNummer() { return internRitNummer; }
    public void setInternRitNummer(String internRitNummer) { this.internRitNummer = internRitNummer; }
}
