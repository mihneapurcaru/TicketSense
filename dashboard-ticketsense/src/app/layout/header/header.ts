import { Component, computed } from '@angular/core';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.css'
})
export class Header {
  readonly user = computed(() => this.auth.user());
  readonly initials = computed(() => {
    const u = this.user();
    if (!u) return '';
    return (u.firstName?.charAt(0) ?? '') + (u.lastName?.charAt(0) ?? '');
  });
  readonly displayRole = computed(() => {
    const role = this.user()?.role;
    switch (role) {
      case 'ADMIN': return 'Administrator';
      case 'IT_SUPPORT_MEMBER': return 'IT Support';
      case 'NORMAL_USER': return 'Employee';
      default: return '';
    }
  });

  constructor(private auth: AuthService) {}
}
