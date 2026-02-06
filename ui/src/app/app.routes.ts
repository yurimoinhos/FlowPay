import { Routes } from '@angular/router';
import { Landing } from './pages/landing/landing';
import { Queue } from './pages/queue/queue';
import { Chat } from './pages/chat/chat';
import { Attendant } from './pages/attendant/attendant';
import { Dashboard } from './pages/dashboard/dashboard';

export const routes: Routes = [
  { path: '', component: Landing },
  { path: 'queue', component: Queue },
  { path: 'chat', component: Chat },
  { path: 'attendant/:type', component: Attendant },
  { path: 'dashboard', component: Dashboard },
  { path: '**', redirectTo: '' }
];
