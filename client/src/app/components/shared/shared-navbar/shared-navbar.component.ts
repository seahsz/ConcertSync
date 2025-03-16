import { Component, inject, OnInit } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-shared-navbar',
  standalone: false,
  templateUrl: './shared-navbar.component.html',
  styleUrl: './shared-navbar.component.css'
})
export class SharedNavbarComponent implements OnInit {

  private router = inject(Router);
  private authSvc = inject(AuthService);

  protected isLoggedIn = false;
  protected userProfile: any = null;

  ngOnInit(): void {
    this.isLoggedIn = this.authSvc.isLoggedIn();

    // If logged in, get profile information
    if (this.isLoggedIn)
      // TODO: fetch user profile information when profile service is implemented
      // For now use placeholder information
      this.userProfile = {
        username: "User",
        name: "Shun Zhou",
        email: "seahsz@gmail.com",
        profilePictureUrl: "/images/blank_profile_pic_160px.png"
      };
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  logout(): void {
    this.authSvc.logout();
    this.isLoggedIn = false;
    this.userProfile = null;
    this.router.navigate(['/']);
  }

}
