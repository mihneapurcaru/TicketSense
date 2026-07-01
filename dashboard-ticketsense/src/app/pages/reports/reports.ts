import { Component, signal, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface TeamReportDto {
  queueName: string;
  icon: string;
  solved: number;
  slaSuccessPercentage: number;
  avgResolutionMinutes: number;
}

@Component({
  selector: 'app-reports',
  imports: [],
  templateUrl: './reports.html',
  styleUrl: './reports.css'
})
export class Reports implements OnInit {
  readonly teams = signal<TeamReportDto[]>([]);

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<TeamReportDto[]>('/api/reports/teams', { withCredentials: true })
      .subscribe(data => this.teams.set(data));
  }

  downloadPdf(queueName: string) {
    this.http.get(`/api/reports/teams/${queueName}/pdf`, {
      responseType: 'blob',
      withCredentials: true
    }).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${queueName}-report.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  formatMinutes(minutes: number): string {
    if (!minutes) return '—';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h === 0) return `${m}m`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}m`;
  }

  slaColor(pct: number): string {
    if (pct >= 95) return 'text-emerald-600';
    if (pct >= 88) return 'text-amber-500';
    return 'text-error';
  }
}
