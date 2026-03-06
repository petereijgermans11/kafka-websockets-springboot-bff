package nl.prorail.tps.producer.data;

import nl.prorail.tps.producer.model.InfraToestandBericht;
import nl.prorail.tps.producer.model.InfraToestandEntry;

import java.util.List;

/**
 * Gesimuleerde RITS brondata — vergelijkbaar met wat de RITS broadcast verstuurt.
 * gebiedNaam = het gebied/emplacement waarop de data betrekking heeft.
 * InfraObjectTypes: WISSEL, SEIN, SECTIE, KRUISING
 */
public class InfraDataset {

    private InfraDataset() {}

    public static final List<InfraToestandBericht> RECORDS = List.of(
        bericht("BKP", List.of(
            entry("1013",  "WISSEL",  false, false, true,  true),
            entry("1013T", "SECTIE",  false, false, true,  true),
            entry("S101",  "SEIN",    false, false, true,  false)
        )),
        bericht("BKP", List.of(
            entry("1013",  "WISSEL",  false, false, true,  false),
            entry("1015",  "WISSEL",  true,  false, true,  true),
            entry("K001",  "KRUISING", false, false, true,  false)
        )),
        bericht("ASD", List.of(
            entry("201A",  "SECTIE",  true,  false, true,  true),
            entry("S201",  "SEIN",    false, false, true,  false),
            entry("W201",  "WISSEL",  false, false, true,  true)
        )),
        bericht("ASD", List.of(
            entry("201A",  "SECTIE",  false, false, true,  false),
            entry("203B",  "SECTIE",  true,  true,  false, true),
            entry("S201",  "SEIN",    false, false, true,  true)
        )),
        bericht("UT", List.of(
            entry("401",   "WISSEL",  false, false, true,  true),
            entry("S401",  "SEIN",    false, false, true,  false)
        )),
        bericht("UT", List.of(
            entry("401",   "WISSEL",  true,  false, true,  true),
            entry("401T",  "SECTIE",  true,  false, true,  true),
            entry("S402",  "SEIN",    false, true,  false, false)
        )),
        bericht("RTD", List.of(
            entry("W601",  "WISSEL",  false, false, true,  true),
            entry("601A",  "SECTIE",  false, false, true,  false),
            entry("S601",  "SEIN",    false, false, true,  true)
        )),
        bericht("RTD", List.of(
            entry("W601",  "WISSEL",  true,  false, true,  true),
            entry("K601",  "KRUISING", false, true,  false, false)
        )),
        bericht("EHV", List.of(
            entry("W801",  "WISSEL",  false, false, true,  true),
            entry("801A",  "SECTIE",  true,  false, true,  true),
            entry("S801",  "SEIN",    false, false, true,  false)
        )),
        bericht("EHV", List.of(
            entry("W801",  "WISSEL",  false, true,  false, true),
            entry("801A",  "SECTIE",  false, false, true,  false)
        ))
    );

    private static InfraToestandBericht bericht(String gebiedNaam, List<InfraToestandEntry> entries) {
        return new InfraToestandBericht("OK", gebiedNaam, "UPDATE", entries, null);
    }

    private static InfraToestandEntry entry(String naam, String type,
                                             boolean bezet, boolean gestoord,
                                             boolean betrouwbaar, boolean ligtInRijweg) {
        return new InfraToestandEntry(naam, type, bezet, gestoord, betrouwbaar, ligtInRijweg);
    }
}

