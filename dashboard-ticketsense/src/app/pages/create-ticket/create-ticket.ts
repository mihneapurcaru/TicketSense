import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-create-ticket',
  imports: [FormsModule, RouterLink],
  templateUrl: './create-ticket.html',
  styleUrl: './create-ticket.css'
})
export class CreateTicket {
  summary = '';
  description = '';
  selectedFiles: File[] = [];
  isDragOver = false;

  loading = signal(false);
  error = signal('');
  createdTicketId = signal<number | null>(null);

  constructor(
    private http: HttpClient,
    private auth: AuthService
  ) {}

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.addFiles(Array.from(input.files));
      input.value = '';
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave() {
    this.isDragOver = false;
  }

  onDrop(event: DragEvent) {
    event.preventDefault();
    this.isDragOver = false;
    if (event.dataTransfer?.files) {
      this.addFiles(Array.from(event.dataTransfer.files));
    }
  }

  addFiles(files: File[]) {
    const allowed = files.filter(f => f.size <= 10 * 1024 * 1024);
    this.selectedFiles = [...this.selectedFiles, ...allowed];
  }

  removeFile(index: number) {
    this.selectedFiles = this.selectedFiles.filter((_, i) => i !== index);
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  isImage(file: File): boolean {
    return file.type.startsWith('image/');
  }

  onSubmit() {
    if (!this.summary.trim()) {
      this.error.set('Summary is required.');
      return;
    }

    const user = this.auth.user();
    if (!user) {
      this.error.set('You must be logged in to create a ticket.');
      return;
    }

    this.error.set('');
    this.loading.set(true);

    const payload = {
      summary: this.summary,
      description: this.description,
      reporterId: user.id
    };

    this.http.post<any>('/api/ticket', payload, { withCredentials: true }).subscribe({
      next: (ticket) => {
        const ticketId = ticket.id;
        if (this.selectedFiles.length === 0) {
          this.onSuccess(ticketId);
          return;
        }

        const uploads = this.selectedFiles.map(file => {
          const formData = new FormData();
          formData.append('file', file);
          return this.http.post(`/api/ticket/${ticketId}/attachments`, formData, { withCredentials: true }).toPromise();
        });

        Promise.allSettled(uploads).then(() => this.onSuccess(ticketId));
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Failed to create ticket. Please try again.');
      }
    });
  }

  private onSuccess(ticketId: number) {
    this.loading.set(false);
    this.createdTicketId.set(ticketId);
    this.summary = '';
    this.description = '';
    this.selectedFiles = [];
  }
}
