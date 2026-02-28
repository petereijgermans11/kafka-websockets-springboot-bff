import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { StompConnectionService } from '../services/stomp-connection.service';
import { TpsDataService } from '../services/tps-data.service';
import { AuthService, UserInfo } from '../services/auth.service';
import { TreinPositieBericht } from '../model/trein-positie.model';

@Component({
  selector: 'app-tps-viewer',
  standalone: false,
  templateUrl: './tps-viewer.component.html',
  styleUrl: './tps-viewer.component.css',
})
export class TpsViewerComponent implements OnInit, OnDestroy {
  readonly sbNamen = ['ASD', 'UT', 'RTD', 'EHV', 'LDN'];
  readonly berichten = new Map<string, TreinPositieBericht>();
  connected = false;
  user: UserInfo | null = null;
  authChecked = false;

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly stompConnection: StompConnectionService,
    private readonly tpsDataService: TpsDataService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.authService.getUser().subscribe(user => {
      this.authChecked = true;
      if (!user) {
        this.authService.login();
        return;
      }
      this.user = user;
      this.initWebSocket();
    });
  }

  private initWebSocket(): void {
    this.stompConnection.connect();

    this.stompConnection.getStompStateOpen$()
      .pipe(takeUntil(this.destroy$))
      .subscribe(open => this.connected = open);

    this.sbNamen.forEach(sbNaam => {
      this.tpsDataService.getTreinpositieStream$(sbNaam)
        .pipe(takeUntil(this.destroy$))
        .subscribe(bericht => this.berichten.set(sbNaam, bericht));
    });
  }

  logout(): void {
    this.authService.logout();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
