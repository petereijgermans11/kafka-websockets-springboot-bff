import { Injectable, OnDestroy } from '@angular/core';
import { merge, Observable, Subject } from 'rxjs';
import { map, take, takeUntil } from 'rxjs/operators';
import { StompConnectionService } from './stomp-connection.service';
import { TreinPositieBericht } from '../model/trein-positie.model';

const TOPIC_PREFIX = '/topic/v1/treinpositie/';
const APP_PREFIX = '/app/v1/treinpositie/';

/**
 * Subscribet op TPS treinpositie topics via STOMP.
 *
 * Twee kanalen per sbNaam:
 * - /app/v1/treinpositie/{sbNaam}  → snapshot (eenmalig via @SubscribeMapping)
 * - /topic/v1/treinpositie/{sbNaam} → live updates (Kafka broadcast)
 */
@Injectable({ providedIn: 'root' })
export class TpsDataService implements OnDestroy {
  private readonly destroy$ = new Subject<void>();

  constructor(private readonly stompConnection: StompConnectionService) {}

  /**
   * Geeft snapshot + daarna doorlopende live updates als één stream.
   */
  getTreinpositieStream$(sbNaam: string): Observable<TreinPositieBericht> {
    const rxStomp = this.stompConnection.getRxStompInstance();

    const snapshot$ = rxStomp.watch(APP_PREFIX + sbNaam).pipe(
      take(1),
      map(msg => JSON.parse(msg.body) as TreinPositieBericht)
    );

    const liveUpdates$ = rxStomp.watch(TOPIC_PREFIX + sbNaam).pipe(
      map(msg => JSON.parse(msg.body) as TreinPositieBericht)
    );

    return merge(snapshot$, liveUpdates$).pipe(takeUntil(this.destroy$));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
