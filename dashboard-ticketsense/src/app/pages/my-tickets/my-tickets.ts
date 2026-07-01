import { Component, signal, computed, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../auth/auth.service';

interface TicketDto {
  id: number;
  summary: string;
  priority: string;
  status: string;
  assignedToId: number | null;
  assignedToFirstName: string | null;
  assignedToLastName: string | null;
  createdAt: string;
}

@Component({
  selector: 'app-my-tickets',
  imports: [RouterLink],
  templateUrl: './my-tickets.html',
  styleUrl: './my-tickets.css'
})
export class MyTickets implements OnInit {
  readonly tickets = signal<TicketDto[]>([]);
  readonly showClosed = signal(false);

  readonly activeTickets = computed(() =>
    this.tickets().filter(t => t.status !== 'CLOSED' && t.status !== 'RESOLVED')
  );

  readonly closedTickets = computed(() =>
    this.tickets().filter(t => t.status === 'CLOSED' || t.status === 'RESOLVED')
  );

  readonly displayedTickets = computed(() =>
    this.showClosed() ? this.closedTickets() : this.activeTickets()
  );

  readonly urgentCount = computed(() =>
    this.activeTickets().filter(t => t.priority === 'HIGH' || t.priority === 'CRITICAL').length
  );

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit() {
    const user = this.auth.user();
    if (!user) return;
    this.http.get<TicketDto[]>(`/api/tickets/my?reporterId=${user.id}`, { withCredentials: true }).subscribe(tickets => {
      this.tickets.set(tickets);
    });
  }

  getInitials(first: string | null, last: string | null): string {
    return (first?.charAt(0) ?? '') + (last?.charAt(0) ?? '');
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  getAge(createdAt: string): string {
    const diff = Date.now() - new Date(createdAt).getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    return `${Math.floor(hours / 24)}d ago`;
  }

  priorityColor(priority: string): string {
    switch (priority) {
      case 'CRITICAL': return 'bg-red-800';
      case 'HIGH': return 'bg-red-500';
      case 'MEDIUM': return 'bg-amber-500';
      default: return 'bg-slate-400';
    }
  }

  statusBadge(status: string): string {
    switch (status) {
      case 'IN_PROGRESS': return 'bg-blue-100 text-blue-700';
      case 'OPEN': return 'bg-slate-200 text-slate-600';
      case 'RESOLVED': return 'bg-emerald-100 text-emerald-700';
      case 'CLOSED': return 'bg-slate-100 text-slate-400';
      default: return 'bg-slate-200 text-slate-600';
    }
  }
}
