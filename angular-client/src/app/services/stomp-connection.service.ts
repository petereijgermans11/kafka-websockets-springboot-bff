import { Injectable } from '@angular/core';
import { RxStomp, RxStompState } from '@stomp/rx-stomp';
import { map, Observable } from 'rxjs';

// Relatieve WebSocket URL — dev proxy proxiet /ws naar nginx BFF gateway.
// Cookies (sessie) worden automatisch meegestuurd via dezelfde origin.
const WS_URL = 'ws://localhost:4300/ws';

/**
 * Maakt één WebSocket/STOMP-verbinding en geeft de RxStomp-instantie door.
 * Conform het patroon van spoor-viewer StompConnectionService.
 * Gebruikt native WebSocket (geen SockJS) — de Spring Boot server ondersteunt beide.
 */
@Injectable({ providedIn: 'root' })
export class StompConnectionService {
  private readonly rxStomp: RxStomp = new RxStomp();

  connect(): void {
    if (this.rxStomp.connected()) return;

    this.rxStomp.configure({
      heartbeatIncoming: 60000,
      heartbeatOutgoing: 60000,
      reconnectDelay: 5000,
      webSocketFactory: () => new WebSocket(WS_URL),
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
