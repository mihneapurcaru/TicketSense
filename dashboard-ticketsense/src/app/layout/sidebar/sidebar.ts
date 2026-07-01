import { Component, computed } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar {
  readonly role = computed(() => this.auth.user()?.role ?? 'NORMAL_USER');

  constructor(private auth: AuthService) {}

  logout() {
    this.auth.logout();
  }
}
