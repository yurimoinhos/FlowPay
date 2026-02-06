import { Component, signal } from '@angular/core';
import { Router } from '@angular/router';
import { ServiceCard } from '../../components/service-card/service-card';
import { UserInfoModal } from '../../components/user-info-modal/user-info-modal';
import { CustomerService } from '../../services/customer';
import { ThemeService } from '../../services/theme';
import { ServiceType } from '../../models/service-type';

@Component({
  selector: 'app-landing',
  imports: [ServiceCard, UserInfoModal],
  templateUrl: './landing.html',
  styleUrl: './landing.css',
})
export class Landing {
  showModal = signal(false);
  submitting = signal(false);
  selectedServiceType = signal<ServiceType | null>(null);

  services = [
    {
      title: 'Problemas com Cartão',
      description: 'Bloqueio, desbloqueio, segunda via e outras questões relacionadas ao seu cartão',
      icon: 'pi pi-credit-card',
      iconBg: 'bg-blue-600',
      type: ServiceType.CARD_PROBLEMS,
    },
    {
      title: 'Empréstimos',
      description: 'Simulações, contratações e informações sobre empréstimos',
      icon: 'pi pi-money-bill',
      iconBg: 'bg-green-600',
      type: ServiceType.LOANS,
    },
    {
      title: 'Outros Assuntos',
      description: 'Dúvidas gerais, suporte técnico e demais solicitações',
      icon: 'pi pi-comments',
      iconBg: 'bg-purple-600',
      type: ServiceType.OTHER,
    },
  ];

  constructor(
    private router: Router,
    private customerService: CustomerService,
    protected theme: ThemeService
  ) {}

  selectService(type: ServiceType) {
    this.selectedServiceType.set(type);
    this.showModal.set(true);
  }

  onUserInfoSubmit(userInfo: { name: string; email: string }) {
    const serviceType = this.selectedServiceType();
    if (!serviceType || this.submitting()) return;

    this.submitting.set(true);
    this.customerService
      .createCustomer({ name: userInfo.name, email: userInfo.email, serviceType })
      .subscribe({
        next: () => {
          this.showModal.set(false);
          this.router.navigate(['/queue'], {
            queryParams: { name: userInfo.name, email: userInfo.email, serviceType },
          });
        },
        error: () => {
          this.submitting.set(false);
        },
      });
  }
}
