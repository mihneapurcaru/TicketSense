import { Component, signal, computed, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface SummaryDto {
  compliancePercentage: number;
  activeBreaches: number;
  nearBreachCount: number;
  avgResolutionMinutes: number;
}

interface WatchlistItemDto {
  id: number;
  priority: string;
  queueName: string;
  status: string;
  remainingMinutes: number;
}

interface BreachByQueueDto {
  queueName: string;
  breachCount: number;
  percentage: number;
}

interface TrendPointDto {
  date: string;
  compliancePercentage: number;
  activeBreaches: number;
}

@Component({
  selector: 'app-sla-monitoring',
  imports: [RouterLink],
  templateUrl: './sla-monitoring.html',
  styleUrl: './sla-monitoring.css'
})
export class SlaMonitoring implements OnInit {
  readonly summary = signal<SummaryDto | null>(null);
  readonly watchlist = signal<WatchlistItemDto[]>([]);
  readonly breachesByQueue = signal<BreachByQueueDto[]>([]);
  readonly trendData = signal<TrendPointDto[]>([]);

  readonly trendPath = computed(() => {
    const data = this.trendData();
    if (data.length < 2) return null;

    const points = data.map((p, i) => {
      const x = (i / (data.length - 1)) * 100;

      const y = 90 - (p.compliancePercentage / 100) * 80;
      return { x, y };
    });

    const linePath = points.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x},${p.y}`).join(' ');
    const areaPath = linePath + ` L100,100 L0,100 Z`;
    return { linePath, areaPath, points };
  });

  readonly hoveredPoint = signal<{ x: number; y: number; date: string; compliance: number } | null>(null);

  readonly trendDateRange = computed(() => {
    const data = this.trendData();
    if (data.length === 0) return { start: '', end: '' };
    return {
      start: this.formatShortDate(data[0].date),
      end: this.formatShortDate(data[data.length - 1].date)
    };
  });

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<SummaryDto>('/api/sla/summary', { withCredentials: true })
      .subscribe(s => this.summary.set(s));

    this.http.get<WatchlistItemDto[]>('/api/sla/watchlist', { withCredentials: true })
      .subscribe(w => this.watchlist.set(w));

    this.http.get<BreachByQueueDto[]>('/api/sla/breaches-by-queue', { withCredentials: true })
      .subscribe(b => this.breachesByQueue.set(b));

    this.http.get<TrendPointDto[]>('/api/sla/trend', { withCredentials: true })
      .subscribe(t => this.trendData.set(t));
  }

  formatAvgResolution(minutes: number): string {
    if (!minutes) return '—';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h === 0) return `${m}m`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}m`;
  }

  formatRemaining(minutes: number): string {
    if (minutes < 0) {
      const h = Math.floor(Math.abs(minutes) / 60);
      const m = Math.abs(minutes) % 60;
      return h > 0 ? `${h}h ${m}m overdue` : `${m}m overdue`;
    }
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h === 0) return `${m}m`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}m`;
  }

  formatShortDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
  }

  remainingColor(minutes: number): string {
    if (minutes < 0) return 'text-error';
    if (minutes < 30) return 'text-error';
    if (minutes < 60) return 'text-amber-600';
    return 'text-indigo-500';
  }

  remainingBarColor(minutes: number): string {
    if (minutes < 0) return 'bg-error';
    if (minutes < 30) return 'bg-error';
    if (minutes < 60) return 'bg-amber-500';
    return 'bg-indigo-500';
  }

  remainingBarWidth(minutes: number, total: number): string {
    if (!total || minutes < 0) return '100%';
    const pct = Math.round((1 - minutes / total) * 100);
    return `${Math.min(100, Math.max(0, pct))}%`;
  }

  priorityBadge(priority: string): string {
    switch (priority) {
      case 'CRITICAL': return 'bg-error/10 text-error';
      case 'HIGH': return 'bg-amber-100 text-amber-600';
      case 'MEDIUM': return 'bg-blue-100 text-blue-600';
      default: return 'bg-slate-100 text-slate-500';
    }
  }

  statusDot(status: string): string {
    return status === 'IN_PROGRESS' ? 'bg-indigo-500' : 'bg-slate-400';
  }

  queueColor(index: number): string {
    const colors = ['bg-indigo-500', 'bg-tertiary-container', 'bg-primary-container', 'bg-secondary'];
    return colors[index % colors.length];
  }

  hoverPoint(index: number) {
    const data = this.trendData();
    const pts = this.trendPath()?.points;
    if (!pts || !data[index]) return;
    this.hoveredPoint.set({
      x: pts[index].x,
      y: pts[index].y,
      date: this.formatShortDate(data[index].date),
      compliance: data[index].compliancePercentage
    });
  }

  clearHover() {
    this.hoveredPoint.set(null);
  }

  readonly sumBreaches = (acc: number, item: BreachByQueueDto) => acc + item.breachCount;
}
