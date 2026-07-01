import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface TopPerformer {
  firstName: string;
  lastName: string;
  teamName: string;
  resolvedCount: number;
}

interface WorkloadEntry {
  queueName: string;
  openCount: number;
}

interface StatisticsData {
  averageResolutionHours: number;
  totalClosedTickets: number;
  criticalPriorityTickets: number;
  volumeTrend: number[];
  resolvedTrend: number[];
  volumeTrendStartDate: string;
  volumeTrendEndDate: string;
  volumeTrendStartDateISO: string;
  openTickets: number;
  inProgressTickets: number;
  closedOrResolvedTickets: number;
  topPerformers: TopPerformer[];
  workload: WorkloadEntry[];
}

@Component({
  selector: 'app-statistics',
  imports: [CommonModule],
  templateUrl: './statistics.html',
  styleUrl: './statistics.css'
})
export class Statistics implements OnInit {
  readonly averageResolutionHours = signal<number>(0);
  readonly totalClosedTickets = signal<number>(0);
  readonly criticalPriorityTickets = signal<number>(0);
  readonly volumeTrend = signal<number[]>([]);
  readonly resolvedTrend = signal<number[]>([]);
  readonly volumeTrendStartDate = signal<string>('');
  readonly volumeTrendEndDate = signal<string>('');
  readonly volumeTrendStartDateISO = signal<string>('');
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly hoveredBarIndex = signal<number | null>(null);
  readonly openTickets = signal<number>(0);
  readonly inProgressTickets = signal<number>(0);
  readonly closedOrResolvedTickets = signal<number>(0);
  readonly topPerformers = signal<TopPerformer[]>([]);
  readonly workload = signal<WorkloadEntry[]>([]);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchStatistics();
  }

  barHeight(value: number): string {
    const max = Math.max(...this.volumeTrend(), 1);
    return `${Math.round((value / max) * 100)}%`;
  }

  workloadBarWidth(count: number): string {
    const max = Math.max(...this.workload().map(w => w.openCount), 1);
    return `${Math.round((count / max) * 100)}%`;
  }

  initials(p: TopPerformer): string {
    return (p.firstName[0] + p.lastName[0]).toUpperCase();
  }

  totalTickets(): number {
    return (this.openTickets() ?? 0) + (this.inProgressTickets() ?? 0) + (this.closedOrResolvedTickets() ?? 0);
  }

  statusPercent(count: number): number {
    const total = this.totalTickets();
    return total === 0 ? 0 : Math.round(((count ?? 0) / total) * 100);
  }

  resolvedAt(index: number): number {
    const trend = this.resolvedTrend();
    return trend?.[index] ?? 0;
  }

  getDateForIndex(index: number): string {
    const isoDate = this.volumeTrendStartDateISO();
    if (!isoDate) return '';
    const startDate = new Date(isoDate);
    const targetDate = new Date(startDate);
    targetDate.setDate(targetDate.getDate() + index);
    return targetDate.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  private fetchStatistics() {
    this.loading.set(true);
    this.error.set(null);

    this.http.get<StatisticsData>('/api/statistics/resolution-time').subscribe({
      next: (data) => {
        this.averageResolutionHours.set(Math.round(data.averageResolutionHours * 10) / 10);
        this.totalClosedTickets.set(data.totalClosedTickets);
        this.criticalPriorityTickets.set(data.criticalPriorityTickets);
        this.volumeTrend.set(data.volumeTrend);
        this.resolvedTrend.set(data.resolvedTrend);
        this.volumeTrendStartDate.set(data.volumeTrendStartDate);
        this.volumeTrendEndDate.set(data.volumeTrendEndDate);
        this.volumeTrendStartDateISO.set(data.volumeTrendStartDateISO);
        this.openTickets.set(data.openTickets);
        this.inProgressTickets.set(data.inProgressTickets);
        this.closedOrResolvedTickets.set(data.closedOrResolvedTickets);
        this.topPerformers.set(data.topPerformers ?? []);
        this.workload.set(data.workload ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to fetch statistics:', err);
        this.error.set('Failed to load statistics');
        this.loading.set(false);
      }
    });
  }
}
