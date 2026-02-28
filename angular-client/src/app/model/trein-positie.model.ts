export interface TreinPositieEntry {
  treinNummer: string;
  sectionId: string;
  betrouwbaar: boolean;
  internRitNummer: string;
}

export interface TreinPositieBericht {
  wsViewStatus: string;
  sbNaam: string;
  state: string;
  treinNummerVensterBezettingList: TreinPositieEntry[];
  timestamp: string;
}
