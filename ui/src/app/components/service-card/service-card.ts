import { Component, input, output } from '@angular/core';
import { Card } from 'primeng/card';

@Component({
  selector: 'app-service-card',
  imports: [Card],
  templateUrl: './service-card.html',
  styleUrl: './service-card.css',
})
export class ServiceCard {
  title = input('');
  description = input('');
  icon = input('');
  iconBg = input('');
  cardClick = output<void>();
}
