import { ChangeDetectorRef, Component, OnInit, Type } from '@angular/core';
import { CommonModule, NgComponentOutlet } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { loadRemoteModule } from '@angular-architects/native-federation';

interface UserInfo {
  name: string;
  username: string;
}

@Component({
  selector: 'app-root',
  imports: [CommonModule, NgComponentOutlet],
  template: `
    <div class="shell" *ngIf="authChecked">
      <header>
        <h1>Spoor Dashboard</h1>
        <div class="header-right" *ngIf="user">
          <span class="user-info">{{ user.name || user.username }}</span>
          <button class="logout-btn" (click)="logout()">Uitloggen</button>
        </div>
      </header>
      <main>
        <div class="dashboard-grid">
          <section *ngIf="tpsComponent" class="dashboard-tile">
            <h2 class="dashboard-title">TPS</h2>
            <ng-container *ngComponentOutlet="tpsComponent" />
          </section>
          <section *ngIf="infraComponent" class="dashboard-tile">
            <h2 class="dashboard-title">INFRA</h2>
            <ng-container *ngComponentOutlet="infraComponent" />
          </section>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .shell {
      min-height: 100vh;
      color: #e2e8f0;
    }
    header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 16px 24px;
      background: #1e293b;
      border-bottom: 1px solid #334155;
    }
    h1 {
      font-size: 1.4rem;
      color: #f8fafc;
    }
    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .user-info {
      font-size: 0.85rem;
      color: #94a3b8;
    }
    .logout-btn {
      background: #1e293b;
      border: 1px solid #334155;
      color: #94a3b8;
      padding: 4px 12px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.8rem;
    }
    .logout-btn:hover {
      background: #334155;
      color: #e2e8f0;
    }
    main {
      padding: 24px;
    }
    .dashboard-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(480px, 1fr));
      gap: 24px;
      align-items: flex-start;
    }
    .dashboard-tile {
      background: #020617;
      border-radius: 12px;
      border: 1px solid #1f2937;
      padding: 16px 20px;
      box-shadow: 0 10px 25px rgba(15, 23, 42, 0.7);
    }
    .dashboard-title {
      margin: 0 0 12px;
      font-size: 1.1rem;
      font-weight: 600;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: #9ca3af;
    }
  `],
})
export class App implements OnInit {
  user: UserInfo | null = null;
  authChecked = false;
  tpsComponent: Type<unknown> | null = null;
  infraComponent: Type<unknown> | null = null;

  constructor(
    private readonly cdr: ChangeDetectorRef,
    private readonly http: HttpClient,
  ) {}

  ngOnInit(): void {
    this.http
      .get<UserInfo>('/api/user', { withCredentials: true })
      .subscribe({
        next: u => {
          this.user = u;
          this.authChecked = true;
          this.cdr.markForCheck();
          this.loadMicrofrontends();
        },
        error: () => {
          window.location.href = '/login';
        },
      });
  }

  private async loadMicrofrontends(): Promise<void> {
    try {
      const tps = await loadRemoteModule('mf-tps', './Component');
      this.tpsComponent = tps.TpsViewerComponent;
      this.cdr.markForCheck();
    } catch (e) {
      console.error('Failed to load mf-tps', e);
    }

    try {
      const rits = await loadRemoteModule('mf-infra', './Component');
      this.infraComponent = rits.InfraViewerComponent;
      this.cdr.markForCheck();
    } catch (e) {
      console.error('Failed to load mf-infra', e);
    }
  }

  logout(): void {
    window.location.href = '/logout';
  }
}
