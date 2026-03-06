import { Injectable } from '@angular/core';
import { RxStomp, RxStompState } from '@stomp/rx-stomp';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SharedStompConnectionService {
  private readonly rxStomp: RxStomp = new RxStomp();
  private activated = false;

  connect(): void {
    if (this.activated) return;
    this.activated = true;

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
