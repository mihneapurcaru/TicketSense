import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AdminSidebar } from '../admin-sidebar/admin-sidebar';
import { Header } from '../header/header';

@Component({
  selector: 'app-admin-shell',
  imports: [RouterOutlet, AdminSidebar, Header],
  template: `
    <div class="flex h-screen overflow-hidden">
      <app-admin-sidebar />
      <main class="flex-1 flex flex-col overflow-hidden">
        <app-header />
        <div class="flex-1 overflow-y-auto bg-[#e8eaf0]">
          <router-outlet />
        </div>
      </main>
    </div>
  `
})
export class AdminShell {}
