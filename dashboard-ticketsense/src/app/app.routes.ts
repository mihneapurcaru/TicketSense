import { Routes } from '@angular/router';
import { Shell } from './layout/shell/shell';
import { authGuard } from './auth/auth.guard';
import { itSupportGuard } from './auth/it-support.guard';
import { normalUserGuard } from './auth/normal-user.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login').then(m => m.Login)
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'ticket-queue', pathMatch: 'full' },
      {
        path: 'ticket-queue',
        canActivate: [itSupportGuard],
        loadComponent: () => import('./pages/ticket-queue/ticket-queue').then(m => m.TicketQueue)
      },
      {
        path: 'ticket/:id',
        loadComponent: () => import('./pages/ticket-detail/ticket-detail').then(m => m.TicketDetail)
      },
      {
        path: 'sla-monitoring',
        canActivate: [itSupportGuard],
        loadComponent: () => import('./pages/sla-monitoring/sla-monitoring').then(m => m.SlaMonitoring)
      },
      {
        path: 'reports',
        canActivate: [itSupportGuard],
        loadComponent: () => import('./pages/reports/reports').then(m => m.Reports)
      },
      {
        path: 'statistics',
        canActivate: [itSupportGuard],
        loadComponent: () => import('./pages/statistics/statistics').then(m => m.Statistics)
      },
      {
        path: 'create-ticket',
        canActivate: [normalUserGuard],
        loadComponent: () => import('./pages/create-ticket/create-ticket').then(m => m.CreateTicket)
      },
      {
        path: 'my-tickets',
        canActivate: [normalUserGuard],
        loadComponent: () => import('./pages/my-tickets/my-tickets').then(m => m.MyTickets)
      }
    ]
  }
];
