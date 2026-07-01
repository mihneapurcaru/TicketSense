import { Component, signal, computed, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface QueueDto {
  id: number;
  name: string;
  icon: string;
  description: string;
}

interface TicketDto {
  id: number;
  summary: string;
  description: string;
  reporterId: number;
  reporterFirstName: string;
  reporterLastName: string;
  assignedToId: number | null;
  assignedToFirstName: string | null;
  assignedToLastName: string | null;
  queueId: number;
  priority: string;
  status: string;
  createdAt: string;
}

const PAGE_SIZE = 10;

@Component({
  selector: 'app-ticket-queue',
  imports: [RouterLink],
  templateUrl: './ticket-queue.html',
  styleUrl: './ticket-queue.css'
})
export class TicketQueue implements OnInit {
  readonly queues = signal<QueueDto[]>([]);
  readonly activeQueueId = signal<number | null>(null);
  readonly allTickets = signal<TicketDto[]>([]);

  readonly filterStatus = signal('');
  readonly filterPriority = signal('');
  readonly filterAssignee = signal('');

  readonly sortBy = signal<'age_desc' | 'age_asc' | 'priority_desc' | 'priority_asc'>('age_desc');

  private readonly priorityOrder: Record<string, number> = {
    CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1
  };

  readonly currentPage = signal(1);

  readonly statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
  readonly priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

  readonly filteredTickets = computed(() => {
    const status = this.filterStatus();
    const priority = this.filterPriority();
    const assignee = this.filterAssignee().toLowerCase();
    const sort = this.sortBy();

    const filtered = this.allTickets().filter(t => {
      const statusMatch = !status || t.status === status;
      const priorityMatch = !priority || t.priority === priority;
      const assigneeMatch = !assignee ||
        `${t.assignedToFirstName ?? ''} ${t.assignedToLastName ?? ''}`.toLowerCase()
          .includes(assignee);
      return statusMatch && priorityMatch && assigneeMatch;
    });

    return filtered.sort((a, b) => {
      if (sort === 'age_desc') return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      if (sort === 'age_asc')  return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      if (sort === 'priority_desc') return (this.priorityOrder[b.priority] ?? 0) - (this.priorityOrder[a.priority] ?? 0);
      if (sort === 'priority_asc')  return (this.priorityOrder[a.priority] ?? 0) - (this.priorityOrder[b.priority] ?? 0);
      return 0;
    });
  });

  readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.filteredTickets().length / PAGE_SIZE))
  );

  readonly pagedTickets = computed(() => {
    const start = (this.currentPage() - 1) * PAGE_SIZE;
    return this.filteredTickets().slice(start, start + PAGE_SIZE);
  });

  readonly pages = computed(() =>
    Array.from({ length: this.totalPages() }, (_, i) => i + 1)
  );

  readonly hasActiveFilters = computed(() =>
    !!this.filterStatus() || !!this.filterPriority() || !!this.filterAssignee()
  );

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<QueueDto[]>('/api/queues', { withCredentials: true }).subscribe(queues => {
      this.queues.set(queues);
      if (queues.length > 0) {
        this.selectQueue(queues[0].id);
      }
    });
  }

  selectQueue(id: number) {
    this.activeQueueId.set(id);
    this.resetFilters();
    this.http.get<TicketDto[]>(`/api/tickets?queueId=${id}`, { withCredentials: true }).subscribe(tickets => {
      this.allTickets.set(tickets);
    });
  }

  setFilterStatus(value: string) {
    this.filterStatus.set(value);
    this.currentPage.set(1);
  }

  setFilterPriority(value: string) {
    this.filterPriority.set(value);
    this.currentPage.set(1);
  }

  setFilterAssignee(value: string) {
    this.filterAssignee.set(value);
    this.currentPage.set(1);
  }

  setSort(value: string) {
    this.sortBy.set(value as any);
    this.currentPage.set(1);
  }

  resetFilters() {
    this.filterStatus.set('');
    this.filterPriority.set('');
    this.filterAssignee.set('');
    this.sortBy.set('age_desc');
    this.currentPage.set(1);
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  getInitials(firstName: string | null, lastName: string | null): string {
    return (firstName?.charAt(0) ?? '') + (lastName?.charAt(0) ?? '');
  }

  getAge(createdAt: string): string {
    const utc = createdAt.endsWith('Z') ? createdAt : createdAt + 'Z';
    const diff = Date.now() - new Date(utc).getTime();
    const minutes = Math.floor(diff / 60000);
    if (minutes < 1) return 'just now';
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    return `${days}d ago`;
  }
}
