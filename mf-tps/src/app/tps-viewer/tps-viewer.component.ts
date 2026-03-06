import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { StompConnectionService } from '../services/stomp-connection.service';
import { TpsDataService } from '../services/tps-data.service';
import { TreinPositieBericht } from '../model/trein-positie.model';

@Component({
  selector: 'app-tps-viewer',
  imports: [CommonModule],
  templateUrl: './tps-viewer.component.html',
  styleUrl: './tps-viewer.component.css',
})
export class TpsViewerComponent implements OnInit, OnDestroy {
  readonly sbNamen = ['ASD', 'UT', 'RTD', 'EHV', 'LDN'];
  readonly berichten = new Map<string, TreinPositieBericht>();
  connected = false;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly cdr: ChangeDetectorRef,
    private readonly stompConnection: StompConnectionService,
    private readonly tpsDataService: TpsDataService,
  ) {}

  ngOnInit(): void {
    this.stompConnection.connect();

    this.stompConnection.getStompStateOpen$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(open => {
        this.connected = open;
        this.cdr.markForCheck();
      });

    this.sbNamen.forEach(sbNaam => {
      this.tpsDataService.getTreinpositieStream$(sbNaam)
        .pipe(takeUntil(this.destroy$))
        .subscribe(bericht => {
          this.berichten.set(sbNaam, bericht);
          this.cdr.markForCheck();
        });
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
