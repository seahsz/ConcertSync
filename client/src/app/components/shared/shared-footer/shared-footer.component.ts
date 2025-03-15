import { Component } from '@angular/core';

@Component({
  selector: 'app-shared-footer',
  standalone: false,
  templateUrl: './shared-footer.component.html',
  styleUrl: './shared-footer.component.css'
})
export class SharedFooterComponent {
  currentYear = new Date().getFullYear();
}
