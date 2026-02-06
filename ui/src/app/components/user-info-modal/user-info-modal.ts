import { Component, model, output, input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Button } from 'primeng/button';

@Component({
  selector: 'app-user-info-modal',
  imports: [FormsModule, Dialog, InputText, Button],
  templateUrl: './user-info-modal.html',
  styleUrl: './user-info-modal.css',
})
export class UserInfoModal {
  visible = model(false);
  loading = input(false);
  userSubmit = output<{ name: string; email: string }>();

  name = '';
  email = '';

  onSubmit() {
    if (this.name && this.email && !this.loading()) {
      this.userSubmit.emit({ name: this.name, email: this.email });
    }
  }
}
