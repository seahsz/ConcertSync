import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-payment-success',
  standalone: false,
  templateUrl: './payment-success.component.html',
  styleUrl: './payment-success.component.css'
})
export class PaymentSuccessComponent implements OnInit {

  private router = inject(Router);

  ngOnInit(): void {
    setTimeout(() => {
      this.redirectToProfile();
    }, 5000);
  }

  redirectToProfile(): void {
    this.router.navigate(['/profile']);
  }
}
