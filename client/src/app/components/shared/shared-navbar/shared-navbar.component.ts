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
  protected showUserMenu = false;

  ngOnInit(): void {
    this.isLoggedIn = this.authSvc.isLoggedIn();

    // If logged in, get profile information
    if (this.isLoggedIn)
      // TODO: fetch user profile information when profile service is implemented
      // For now use placeholder information
      this.userProfile = {
        username: "User",
        profilePictureUrl: "/images/blank_profile_pic_160px"
      };
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
    this.showUserMenu = false;
  }

  logout(): void {
    this.authSvc.logout();
    this.isLoggedIn = false;
    this.userProfile = null;
    this.showUserMenu = false;
    this.router.navigate(['/']);
  }

}
