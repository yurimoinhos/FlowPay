import { Component, computed, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { ServiceType } from '../../models/service-type';
import { CustomerService } from '../../services/customer';
import { ThemeService } from '../../services/theme';

interface Message {
  text: string;
  time: string;
  sender: 'user' | 'agent';
}

const SERVICE_TYPE_LABELS: Record<string, string> = {
  [ServiceType.CARD_PROBLEMS]: 'Problemas com Cartão',
  [ServiceType.LOANS]: 'Empréstimos',
  [ServiceType.OTHER]: 'Outros Assuntos',
};

@Component({
  selector: 'app-chat',
  imports: [FormsModule, Button, InputText],
  templateUrl: './chat.html',
  styleUrl: './chat.css',
})
export class Chat implements OnInit {
  name = signal('');
  email = signal('');
  serviceType = signal<ServiceType | null>(null);
  messages = signal<Message[]>([]);
  newMessage = '';
  closing = signal(false);

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

      const now = new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
      this.messages.set([
        { text: `Olá ${this.name()}! Bem-vindo ao atendimento FlowPay.`, time: now, sender: 'agent' },
        { text: `Um atendente está disponível agora. Como posso ajudá-lo com "${this.serviceTypeLabel()}"?`, time: now, sender: 'agent' },
      ]);
    });
  }

  sendMessage() {
    const text = this.newMessage.trim();
    if (!text) return;

    const now = new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    this.messages.update((msgs) => [...msgs, { text, time: now, sender: 'user' }]);
    this.newMessage = '';

    setTimeout(() => {
      const replyTime = new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
      this.messages.update((msgs) => [
        ...msgs,
        { text: 'Obrigado pela sua mensagem. Um atendente irá responder em breve.', time: replyTime, sender: 'agent' },
      ]);
    }, 1000);
  }

  closeChat() {
    this.closing.set(true);
    this.customerService.finishSession(this.email()).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => this.router.navigate(['/']),
    });
  }

  goBack() {
    this.router.navigate(['/']);
  }
}