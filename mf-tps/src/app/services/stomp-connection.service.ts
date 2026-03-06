import { Injectable } from '@angular/core';
import { RxStomp, RxStompState } from '@stomp/rx-stomp';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class StompConnectionService {
  private readonly rxStomp: RxStomp = new RxStomp();

  connect(): void {
    if (this.rxStomp.connected()) return;

    const wsUrl = `ws://${window.location.host}/ws`;

    this.rxStomp.configure({
      heartbeatIncoming: 60000,
      heartbeatOutgoing: 60000,
      reconnectDelay: 5000,
      webSocketFactory: () => new WebSocket(wsUrl),
    });
    this.rxStomp.activate();
  }

  getRxStompInstance(): RxStomp {
    return this.rxStomp;
  }

  getStompStateOpen$(): Observable<boolean> {
    return this.rxStomp.connectionState$.pipe(
      map(state => state === RxStompState.OPEN)
    );
  }
}
