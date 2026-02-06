import { Component, signal, computed, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { Button } from 'primeng/button';
import { ProgressSpinner } from 'primeng/progressspinner';
import { CustomerService } from '../../services/customer';
import { ThemeService } from '../../services/theme';
import { ServiceType } from '../../models/service-type';

const SERVICE_TYPE_LABELS: Record<string, string> = {
  [ServiceType.CARD_PROBLEMS]: 'Problemas com Cartão',
  [ServiceType.LOANS]: 'Empréstimos',
  [ServiceType.OTHER]: 'Outros Assuntos',
};

@Component({
  selector: 'app-queue',
  imports: [Button, ProgressSpinner],
  templateUrl: './queue.html',
  styles: ``,
})
export class Queue implements OnInit, OnDestroy {
  name = signal('');
  email = signal('');
  serviceType = signal<ServiceType | null>(null);
  position = signal<number | null>(null);
  cancelling = signal(false);

  private queueSub?: Subscription;

  serviceTypeLabel = computed(() => {
    const type = this.serviceType();
    return type ? SERVICE_TYPE_LABELS[type] ?? '' : '';
  });

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private customerService: CustomerService,
    protected theme: ThemeService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe((params) => {
      this.name.set(params['name'] ?? '');
      this.email.set(params['email'] ?? '');
      this.serviceType.set(params['serviceType'] ?? null);

      if (this.email()) {
        this.connectToQueue();
      }
    });
  }

  ngOnDestroy() {
    this.queueSub?.unsubscribe();
  }

  private connectToQueue() {
    this.queueSub = this.customerService.getQueuePosition(this.email()).subscribe({
      next: (data) => {
        this.position.set(data.position);
        if (data.position === 0 || data.status === 'IN_SERVICE') {
          this.queueSub?.unsubscribe();
          this.router.navigate(['/chat'], {
            queryParams: {
              name: this.name(),
              email: this.email(),
              serviceType: this.serviceType(),
            },
          });
        }
      },
      error: () => {
        this.position.set(null);
      },
    });
  }

  cancel() {
    this.cancelling.set(true);
    this.queueSub?.unsubscribe();
    this.customerService.finishSession(this.email()).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.router.navigate(['/']),
    });
  }
}
