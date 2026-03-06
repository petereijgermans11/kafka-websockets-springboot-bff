import { Injectable, OnDestroy } from '@angular/core';
import { merge, Observable, Subject } from 'rxjs';
import { map, take, takeUntil } from 'rxjs/operators';
import { IMessage } from '@stomp/rx-stomp';
import { SharedStompConnectionService } from 'shared-websocket';
import { TreinPositieBericht } from '../model/trein-positie.model';

const TOPIC_PREFIX = '/topic/v1/treinpositie/';
const APP_PREFIX = '/app/v1/treinpositie/';

@Injectable({ providedIn: 'root' })
export class TpsDataService implements OnDestroy {
  private readonly destroy$ = new Subject<void>();

  constructor(private readonly stompConnection: SharedStompConnectionService) {}

  getTreinpositieStream$(sbNaam: string): Observable<TreinPositieBericht> {
    const rxStomp = this.stompConnection.getRxStompInstance();

    const snapshot$ = rxStomp.watch(APP_PREFIX + sbNaam).pipe(
      take(1),
      map((msg: IMessage) => JSON.parse(msg.body) as TreinPositieBericht)
    );

    const liveUpdates$ = rxStomp.watch(TOPIC_PREFIX + sbNaam).pipe(
      map((msg: IMessage) => JSON.parse(msg.body) as TreinPositieBericht)
    );

    return merge(snapshot$, liveUpdates$).pipe(takeUntil(this.destroy$));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
