package nl.prorail.tps.producer.data;

import nl.prorail.tps.producer.model.TreinPositieBericht;
import nl.prorail.tps.producer.model.TreinPositieEntry;

import java.util.List;

/**
 * Gesimuleerde TPS brondata — vergelijkbaar met wat de TPS EMS broadcast verstuurt.
 * sbNaam = signaleringsbeeldnaam (het 'kanaal' waarop de data gepubliceerd wordt).
 */
public class TpsDataset {

    private TpsDataset() {}

    public static final List<TreinPositieBericht> RECORDS = List.of(
        bericht("ASD", List.of(
            entry("1234", "ASD_001", true,  "R001"),
            entry("5678", "ASD_003", true,  "R002")
        )),
        bericht("ASD", List.of(
            entry("1234", "ASD_002", true,  "R001"),
            entry("9101", "ASD_004", false, "R003")
        )),
        bericht("UT", List.of(
            entry("2020", "UT_001",  true,  "R010")
        )),
        bericht("UT", List.of(
            entry("2020", "UT_002",  true,  "R010"),
            entry("3030", "UT_003",  true,  "R011")
        )),
        bericht("RTD", List.of(
            entry("4444", "RTD_001", true,  "R020"),
            entry("5555", "RTD_002", true,  "R021")
        )),
        bericht("RTD", List.of(
            entry("4444", "RTD_003", false, "R020")
        )),
        bericht("EHV", List.of(
            entry("7777", "EHV_001", true,  "R030")
        )),
        bericht("EHV", List.of(
            entry("7777", "EHV_002", true,  "R030"),
            entry("8888", "EHV_003", true,  "R031")
        )),
        bericht("LDN", List.of(
            entry("6001", "LDN_001", true,  "R040"),
            entry("6002", "LDN_002", true,  "R041")
        )),
        bericht("LDN", List.of(
            entry("6001", "LDN_002", true,  "R040")
        ))
    );

    private static TreinPositieBericht bericht(String sbNaam, List<TreinPositieEntry> entries) {
        return new TreinPositieBericht("OK", sbNaam, "UPDATE", entries, null);
    }

    private static TreinPositieEntry entry(String treinNummer, String sectionId,
                                            boolean betrouwbaar, String internRitNummer) {
        return new TreinPositieEntry(treinNummer, sectionId, betrouwbaar, internRitNummer);
    }
}
