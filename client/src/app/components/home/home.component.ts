import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Concert } from '../../models/concert';
import { ConcertService } from '../../services/concert.service';
import { map, Subscription } from 'rxjs';
import { UserProfileService } from '../../services/user-profile.service';
import { AuthService } from '../../services/auth.service';
import { UserProfile } from '../../models/user-profile';

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit, OnDestroy {

  private concertSvc = inject(ConcertService);
  private userSvc = inject(UserProfileService);
  private authSvc = inject(AuthService);

  private monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

  protected concerts: Concert[] = [];
  protected filteredConcerts: Concert[] = [];
  protected displayedConcerts: Concert[] = [];
  protected pageSize = 8;
  currentIndex = 0;

  // Add these properties for checking login and premium status
  protected isLoggedIn = false;
  protected userProfile: UserProfile | null = null;
  protected isLoading = true;
  protected userSub!: Subscription;

  ngOnInit(): void {
    this.fetchConcerts();
    this.checkLoginStatus();
  }

  ngOnDestroy(): void {
    if (this.userSub)
        this.userSub.unsubscribe();
  }

  fetchConcerts() {
    this.concertSvc.getUpcomingConcerts().pipe(
      map(concerts => {
        //Sort concerts by earliest date
        return concerts.sort((a, b) => {
          const earliestA = new Date(Math.min(...a.dates.map(d => new Date(d).getTime())));
          const earliestB = new Date(Math.min(...b.dates.map(d => new Date(d).getTime())));
          return earliestA.getTime() - earliestB.getTime();
        });
      })
    ).subscribe({
      next: (data) => {
        console.info(">>> Incoming concerts: ", data);
        this.concerts = data;
        this.filteredConcerts = data; // Initialize the filtered list
        this.updateDisplayedConcerts();
      },
      error: (err) => {
        console.error('Error fetching concerts: ', err);
        this.concerts = [];
        this.filteredConcerts = [];
        this.updateDisplayedConcerts();
      }
    })
  }

  filterConcerts($event: Event) {
    const searchTerm = ($event.target as HTMLInputElement).value.toLowerCase();
    this.filteredConcerts = this.concerts.filter(concert =>
      concert.artist.toLowerCase().includes(searchTerm) ||
      concert.venue.toLowerCase().includes(searchTerm)
    ).sort((a, b) => {
      const earliestA = new Date(Math.min(...a.dates.map(d => new Date(d).getTime())));
      const earliestB = new Date(Math.min(...b.dates.map(d => new Date(d).getTime())));
      return earliestA.getTime() - earliestB.getTime();
    });
    this.currentIndex = 0; // Reset pagination on filter
    this.updateDisplayedConcerts();
  }

  getDateList(concert: Concert): string {
    if (!concert.dates || concert.dates.length === 0) {
      return 'TBD';
    }

    const dates = concert.dates.map(date => new Date(date))
      .sort((a, b) => a.getTime() - b.getTime());
    if (dates.length === 1) {
      const date = dates[0];
      return `${date.getDate()} ${this.monthNames[date.getMonth()]} ${date.getFullYear()}`; // "15 Apr 2025"
    }

    const allSameYear = dates.every(date => date.getFullYear() === dates[0].getFullYear());
    if (allSameYear) {
      // Same year: "15 Apr, 16 Apr 2025"
      const dateParts = dates.map(date =>
        `${date.getDate()} ${this.monthNames[date.getMonth()]}`)
        .join(', ')
      return `${dateParts} ${dates[0].getFullYear()}`
    } else {
      // Cross-year: "31 Dec 2025, 1 Jan 2026" or "31 Dec 2025, 1 Jan, 2 Jan 2026"
      return dates.map(date =>
        `${date.getDate()} ${this.monthNames[date.getMonth()]} ${date.getFullYear()}`)
        .join(', ');
    }
  }

  // Pagination
  updateDisplayedConcerts(): void {
    this.displayedConcerts = this.filteredConcerts.slice(0, this.currentIndex + this.pageSize);
  }

  viewMore(): void {
    this.currentIndex += 8;
    this.updateDisplayedConcerts();
  }

  hasMoreConcerts(): boolean {
    return this.currentIndex + this.pageSize < this.filteredConcerts.length;
  }

  // Login status and fetch user profile
  checkLoginStatus(): void {
    this.isLoggedIn = this.authSvc.isLoggedIn();

    if (this.isLoggedIn) {
      this.userSub = this.userSvc.getUserProfile().subscribe({
        next: (profile) => {
          this.userProfile = profile;
          this.isLoading = false;
        },
        error: (error) => {
          console.error("Error loading user profile: ", error);
          this.isLoading = false;
        }
      });
    } else {
      this.isLoading = false;
    }
  }

  // Check if premium banner should be shown
  shouldShowPremiumBanner(): boolean {
    if (this.isLoggedIn && this.userProfile)
        return !this.userProfile.premium_status; // show for non-premium users
    return true; //  show for non-logged in users
  }

}
