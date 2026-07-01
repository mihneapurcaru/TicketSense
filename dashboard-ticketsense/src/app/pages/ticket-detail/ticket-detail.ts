import { Component, signal, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '../../auth/auth.service';

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
  estimatedMinutes: number | null;
  resolutionNote: string | null;
  aiSummary: string | null;
  createdAt: string;
  closedAt: string | null;
}

interface UserDto {
  id: number;
  firstName: string;
  lastName: string;
  username: string;
}

interface AttachmentDto {
  id: number;
  ticketId: number;
  fileName: string;
  fileType: string;
  fileSize: number;
  uploadedAt: string;
}

interface CommentDto {
  id: number;
  message: string;
  createdAt: string;
  ticketId: number;
  userId: number;
  userFirstName: string;
  userLastName: string;
  userRole: string;
}

@Component({
  selector: 'app-ticket-detail',
  imports: [FormsModule],
  templateUrl: './ticket-detail.html',
  styleUrl: './ticket-detail.css'
})
export class TicketDetail implements OnInit {
  readonly ticket = signal<TicketDto | null>(null);
  readonly loading = signal(true);
  readonly saveSuccess = signal(false);
  readonly itSupportUsers = signal<UserDto[]>([]);
  readonly attachments = signal<AttachmentDto[]>([]);
  readonly uploading = signal(false);
  readonly previewAttachment = signal<AttachmentDto | null>(null);
  readonly comments = signal<CommentDto[]>([]);
  readonly commentText = signal('');
  readonly postingComment = signal(false);
  readonly summarizing = signal(false);

  priority = '';
  status = '';
  estimatedMinutes: number | null = null;
  resolutionNote = '';
  assignedToId: number | null = null;

  readonly isDirty = signal(false);

  readonly priorities = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  readonly statuses = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    public auth: AuthService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    this.http.get<TicketDto>(`/api/ticket/${id}`, { withCredentials: true }).subscribe(ticket => {
      this.ticket.set(ticket);
      this.priority = ticket.priority ?? '';
      this.status = ticket.status ?? '';
      this.estimatedMinutes = ticket.estimatedMinutes ?? null;
      this.resolutionNote = ticket.resolutionNote ?? '';
      this.assignedToId = ticket.assignedToId;
      this.loading.set(false);
    });

    this.http.get<AttachmentDto[]>(`/api/ticket/${id}/attachments`, { withCredentials: true }).subscribe(a => {
      this.attachments.set(a);
    });

    this.http.get<CommentDto[]>(`/api/ticket/${id}/comments`, { withCredentials: true }).subscribe(c => {
      this.comments.set(c);
    });

    if (this.auth.user()?.role === 'ADMIN') {
      this.http.get<UserDto[]>('/api/users/it-support', { withCredentials: true }).subscribe(users => {
        this.itSupportUsers.set(users);
      });
    }
  }

  summarize() {
    const t = this.ticket();
    if (!t || this.summarizing()) return;
    this.summarizing.set(true);
    this.http.post<TicketDto>(`/api/ticket/${t.id}/summarize`, {}, { withCredentials: true }).subscribe({
      next: (updated) => {
        this.ticket.set({ ...t, aiSummary: updated.aiSummary });
        this.summarizing.set(false);
      },
      error: () => this.summarizing.set(false)
    });
  }

  onFieldChange() {
    const t = this.ticket();
    if (!t) return;
    const changed =
      this.priority !== (t.priority ?? '') ||
      this.status !== (t.status ?? '') ||
      this.estimatedMinutes !== (t.estimatedMinutes ?? null) ||
      this.resolutionNote !== (t.resolutionNote ?? '') ||
      this.assignedToId !== t.assignedToId;
    this.isDirty.set(changed);
  }

  assignToMe() {
    const user = this.auth.user();
    if (!user) return;
    this.assignedToId = user.id;
    this.onFieldChange();
  }

  saveChanges() {
    const t = this.ticket();
    if (!t) return;

    const payload: any = {
      priority: this.priority,
      status: this.status,
      estimatedMinutes: this.estimatedMinutes,
      resolutionNote: this.resolutionNote
    };

    if (this.assignedToId !== t.assignedToId) {
      payload.assignedToId = this.assignedToId;
    }

    this.http.put<TicketDto>(`/api/ticket/${t.id}`, payload, { withCredentials: true }).subscribe(updated => {
      this.ticket.set(updated);
      this.assignedToId = updated.assignedToId;
      this.isDirty.set(false);
      this.saveSuccess.set(true);
      setTimeout(() => this.saveSuccess.set(false), 3000);
    });
  }

  getInitials(first: string | null, last: string | null): string {
    return (first?.charAt(0) ?? '') + (last?.charAt(0) ?? '');
  }

  formatDate(date: string | null): string {
    if (!date) return '—';
    return new Date(date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
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

  isITSupport(): boolean {
    const role = this.auth.user()?.role;
    return role === 'IT_SUPPORT_MEMBER' || role === 'ADMIN';
  }

  isAdmin(): boolean {
    return this.auth.user()?.role === 'ADMIN';
  }

  postComment() {
    const message = this.commentText().trim();
    if (!message) return;

    const ticketId = this.ticket()?.id;
    if (!ticketId) return;

    this.postingComment.set(true);
    this.http.post<CommentDto>(`/api/ticket/${ticketId}/comments`, { message }, { withCredentials: true }).subscribe({
      next: (comment) => {
        this.comments.update(list => [...list, comment]);
        this.commentText.set('');
        this.postingComment.set(false);
      },
      error: () => this.postingComment.set(false)
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    const ticketId = this.ticket()?.id;
    if (!ticketId) return;

    const formData = new FormData();
    formData.append('file', file);

    this.uploading.set(true);
    this.http.post<AttachmentDto>(`/api/ticket/${ticketId}/attachments`, formData, { withCredentials: true }).subscribe({
      next: (attachment) => {
        this.attachments.update(list => [...list, attachment]);
        this.uploading.set(false);
        input.value = '';
      },
      error: () => this.uploading.set(false)
    });
  }

  deleteAttachment(id: number) {
    this.http.delete(`/api/attachments/${id}`, { withCredentials: true }).subscribe(() => {
      this.attachments.update(list => list.filter(a => a.id !== id));
    });
  }

  openPreview(att: AttachmentDto) {
    this.previewAttachment.set(att);
  }

  closePreview() {
    this.previewAttachment.set(null);
  }

  previewUrl(id: number): string {
    return `/api/attachments/${id}/preview`;
  }

  safePreviewUrl(id: number): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(`/api/attachments/${id}/preview`);
  }

  downloadUrl(id: number): string {
    return `/api/attachments/${id}/download`;
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  isImage(fileType: string): boolean {
    return fileType?.startsWith('image/');
  }

  formatEstimate(minutes: number | null): string {
    if (!minutes) return '—';
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    if (h === 0) return `${m}m`;
    if (m === 0) return `${h}h`;
    return `${h}h ${m}m`;
  }

  getAssigneeName(): string {
    const t = this.ticket();
    if (!t) return 'Unassigned';
    if (this.assignedToId === this.auth.user()?.id) return 'You';
    if (!t.assignedToFirstName) return 'Unassigned';
    return `${t.assignedToFirstName} ${t.assignedToLastName}`;
  }
}
