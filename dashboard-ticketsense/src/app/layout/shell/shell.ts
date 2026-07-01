import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from '../sidebar/sidebar';
import { Header } from '../header/header';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, Sidebar, Header],
  template: `
    <div class="flex h-screen overflow-hidden">
      <app-sidebar />
      <main class="flex-1 flex flex-col overflow-hidden">
        <app-header />
        <div class="flex-1 overflow-y-auto bg-[#e8eaf0]">
          <router-outlet />
        </div>
      </main>
    </div>
  `
})
export class Shell {}
