export interface InfraToestandEntry {
  infraObjectNaam: string;
  infraObjectType: string;
  bezet: boolean;
  gestoord: boolean;
  betrouwbaar: boolean;
  ligtInRijweg: boolean;
}

export interface InfraToestandBericht {
  wsViewStatus: string;
  gebiedNaam: string;
  state: string;
  infraToestandList: InfraToestandEntry[];
  timestamp: string;
}
