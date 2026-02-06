import { Component, signal, computed, OnInit, OnDestroy, effect } from '@angular/core';
import { Subscription } from 'rxjs';
import { Chart, registerables } from 'chart.js';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { UIChart } from 'primeng/chart';
import { CustomerService } from '../../services/customer';
import { ThemeService } from '../../services/theme';
import { SessionMetrics } from '../../models/session-metrics';

Chart.register(...registerables);

@Component({
  selector: 'app-dashboard',
  imports: [Card, UIChart],
  templateUrl: './dashboard.html',
  styles: ``,
})
export class Dashboard implements OnInit, OnDestroy {
  metrics = signal<SessionMetrics | null>(null);
  private metricsSub?: Subscription;

  doughnutData = computed(() => {
    const m = this.metrics();
    if (!m) return null;
    return {
      labels: ['Pendentes', 'Em Andamento', 'Concluídas', 'Canceladas'],
      datasets: [
        {
          data: [m.pendingCount, m.inProgressCount, m.completedCount, m.canceledCount],
          backgroundColor: ['#f59e0b', '#3b82f6', '#22c55e', '#ef4444'],
          hoverBackgroundColor: ['#d97706', '#2563eb', '#16a34a', '#dc2626'],
        },
      ],
    };
  });

  barData = computed(() => {
    const m = this.metrics();
    if (!m) return null;
    return {
      labels: ['Taxa de Conclusão', 'Taxa de Cancelamento'],
      datasets: [
        {
          label: '%',
          data: [m.completionRate, m.cancellationRate],
          backgroundColor: ['#22c55e', '#ef4444'],
          borderRadius: 4,
        },
      ],
    };
  });

  // Computed options that react to theme changes
  doughnutOptions = computed(() => {
    const textColor = this.theme.darkMode() ? '#ebedef' : '#495057';
    return {
      plugins: {
        legend: {
          position: 'bottom',
          labels: { color: textColor }
        }
      }
    };
  });

  barOptions = computed(() => {
    const textColor = this.theme.darkMode() ? '#ebedef' : '#495057';
    const gridColor = this.theme.darkMode() ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.1)';
    
    return {
      plugins: {
        legend: { display: false }
      },
      scales: {
        x: {
          ticks: { color: textColor },
          grid: { color: gridColor, drawBorder: false }
        },
        y: {
          beginAtZero: true,
          max: 100,
          ticks: { color: textColor, callback: (v: number) => v + '%' },
          grid: { color: gridColor, drawBorder: false }
        },
      },
    };
  });

  constructor(
    private customerService: CustomerService,
    protected theme: ThemeService
  ) {}

  ngOnInit() {
    this.metricsSub = this.customerService.getMetrics().subscribe({
      next: (data) => this.metrics.set(data),
      error: (err) => console.error('Erro no stream de métricas:', err),
    });
  }

  ngOnDestroy() {
    this.metricsSub?.unsubscribe();
  }

  formatDuration(seconds: number): string {
    // Ensure seconds is positive
    const totalSeconds = Math.max(0, Math.abs(seconds));
    
    const mins = Math.floor(totalSeconds / 60);
    const secs = Math.round(totalSeconds % 60);
    return `${mins}m ${secs}s`;
  }
}
