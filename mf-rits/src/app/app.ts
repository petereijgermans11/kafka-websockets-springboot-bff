import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { InfraViewerComponent } from './infra-viewer/infra-viewer.component';

@Component({
  selector: 'app-root',
  imports: [CommonModule, InfraViewerComponent],
  template: `
    <div class="standalone-shell" *ngIf="authChecked && user">
      <app-infra-viewer />
    </div>
    <div class="auth-loading" *ngIf="!authChecked">Authenticeren...</div>
  `,
  styles: [`
    .standalone-shell { min-height: 100vh; color: #e2e8f0; padding: 24px; }
    .auth-loading { color: #64748b; padding: 48px; text-align: center; }
  `],
})
export class App implements OnInit {
  user: { name: string } | null = null;
  authChecked = false;

  constructor(
    private readonly cdr: ChangeDetectorRef,
    private readonly http: HttpClient
  ) {}

  ngOnInit(): void {
    this.http
      .get<{ name: string }>('/api/user', { withCredentials: true })
      .subscribe({
        next: u => {
          this.user = u;
          this.authChecked = true;
          this.cdr.markForCheck();
        },
        error: () => {
          this.authChecked = true;
          window.location.href = '/login';
        },
      });
  }
}
