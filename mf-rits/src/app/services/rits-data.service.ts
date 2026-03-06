import { Injectable, OnDestroy } from '@angular/core';
import { merge, Observable, Subject } from 'rxjs';
import { map, take, takeUntil } from 'rxjs/operators';
import { IMessage } from '@stomp/rx-stomp';
import { SharedStompConnectionService } from 'shared-websocket';
import { InfraToestandBericht } from '../model/infra-toestand.model';

const TOPIC_PREFIX = '/topic/v1/infratoestand/';
const APP_PREFIX = '/app/v1/infratoestand/';

@Injectable({ providedIn: 'root' })
export class InfraDataService implements OnDestroy {
  private readonly destroy$ = new Subject<void>();

  constructor(private readonly stompConnection: SharedStompConnectionService) {}

  getInfraToestandStream$(gebiedNaam: string): Observable<InfraToestandBericht> {
    const rxStomp = this.stompConnection.getRxStompInstance();

    const snapshot$ = rxStomp.watch(APP_PREFIX + gebiedNaam).pipe(
      take(1),
      map((msg: IMessage) => JSON.parse(msg.body) as InfraToestandBericht)
    );

    const liveUpdates$ = rxStomp.watch(TOPIC_PREFIX + gebiedNaam).pipe(
      map((msg: IMessage) => JSON.parse(msg.body) as InfraToestandBericht)
    );

    return merge(snapshot$, liveUpdates$).pipe(takeUntil(this.destroy$));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
