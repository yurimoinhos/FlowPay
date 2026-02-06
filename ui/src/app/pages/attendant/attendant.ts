import { Component, signal, computed, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { Button } from 'primeng/button';
import { Tag } from 'primeng/tag';
import { Avatar } from 'primeng/avatar';
import { CustomerService } from '../../services/customer';
import { ThemeService } from '../../services/theme';
import { ServiceType } from '../../models/service-type';
import { InProgressSessionResponse } from '../../models/in-progress-session';

const SERVICE_TYPE_LABELS: Record<string, string> = {
  [ServiceType.CARD_PROBLEMS]: 'Problemas com Cartão',
  [ServiceType.LOANS]: 'Empréstimos',
  [ServiceType.OTHER]: 'Outros Assuntos',
};

@Component({
  selector: 'app-attendant',
  imports: [Button, Tag, Avatar],
  templateUrl: './attendant.html',
  styleUrl: './attendant.css',
})
export class Attendant implements OnInit, OnDestroy {
  serviceType = signal<ServiceType | null>(null);
  sessions = signal<InProgressSessionResponse[]>([]);

  private sseSub?: Subscription;

  serviceTypeLabel = computed(() => {
    const type = this.serviceType();
    return type ? SERVICE_TYPE_LABELS[type] ?? '' : '';
  });

  sessionCount = computed(() => this.sessions().length);

  constructor(
    private route: ActivatedRoute,
    private customerService: CustomerService,
    protected theme: ThemeService
  ) {}

  ngOnInit() {
    this.route.params.subscribe((params) => {
      const type = params['type'] as ServiceType;
      this.serviceType.set(type);
      this.connectToStream(type);
    });
  }

  ngOnDestroy() {
    this.sseSub?.unsubscribe();
  }

  private connectToStream(type: ServiceType) {
    this.sseSub?.unsubscribe();
    this.sseSub = this.customerService.getInProgressSessions(type).subscribe({
      next: (session) => {
        this.sessions.update((list) => {
          const exists = list.find((s) => s.sessionId === session.sessionId);
          if (exists) {
            return list.map((s) => (s.sessionId === session.sessionId ? session : s));
          }
          return [...list, session];
        });
      },
      error: (err) => console.error('Erro no stream:', err),
    });
  }

  finishCustomer(session: InProgressSessionResponse) {
    this.customerService.finishSession(session.customerEmail).subscribe({
      next: () => {
        this.sessions.update((list) => list.filter((s) => s.sessionId !== session.sessionId));
      },
      error: (err) => console.error('Erro ao finalizar sessão:', err),
    });
  }

  formatTime(startedAt: string): string {
    const start = new Date(startedAt);
    const now = new Date();
    const diffMs = now.getTime() - start.getTime();
    const mins = Math.floor(diffMs / 60000);
    return mins < 1 ? 'Agora' : `${mins} min`;
  }
}
