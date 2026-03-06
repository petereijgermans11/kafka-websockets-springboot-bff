import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SharedStompConnectionService } from 'shared-websocket';
import { InfraDataService } from '../services/rits-data.service';
import { InfraToestandBericht } from '../model/infra-toestand.model';

@Component({
  selector: 'app-infra-viewer',
  imports: [CommonModule],
  templateUrl: './infra-viewer.component.html',
  styleUrl: './infra-viewer.component.css',
})
export class InfraViewerComponent implements OnInit, OnDestroy {
  readonly gebieden = ['BKP', 'ASD', 'UT', 'RTD', 'EHV'];
  readonly berichten = new Map<string, InfraToestandBericht>();
  connected = false;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly cdr: ChangeDetectorRef,
    private readonly stompConnection: SharedStompConnectionService,
    private readonly ritsDataService: InfraDataService,
  ) {}

  ngOnInit(): void {
    this.stompConnection.connect();

    this.stompConnection.getStompStateOpen$()
      .pipe(takeUntil(this.destroy$))
      .subscribe((open: boolean) => {
        this.connected = open;
        this.cdr.markForCheck();
      });

    this.gebieden.forEach(gebiedNaam => {
      this.ritsDataService.getInfraToestandStream$(gebiedNaam)
        .pipe(takeUntil(this.destroy$))
        .subscribe(bericht => {
          this.berichten.set(gebiedNaam, bericht);
          this.cdr.markForCheck();
        });
    });
  }

  getTypeIcon(type: string): string {
    switch (type) {
      case 'WISSEL': return 'W';
      case 'SEIN': return 'S';
      case 'SECTIE': return 'T';
      case 'KRUISING': return 'K';
      default: return '?';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

