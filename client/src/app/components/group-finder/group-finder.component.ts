import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { Concert } from '../../models/concert';
import { FormControl } from '@angular/forms';
import { Group } from '../../models/group.models';
import { ConcertService } from '../../services/concert.service';
import { GroupService } from '../../services/group.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { debounceTime, distinctUntilChanged, Subscription } from 'rxjs';

@Component({
  selector: 'app-group-finder',
  standalone: false,
  templateUrl: './group-finder.component.html',
  styleUrl: './group-finder.component.css'
})
export class GroupFinderComponent implements OnInit, OnDestroy {

  upcomingConcerts: Concert[] = [];
  filteredConcerts: Concert[] = [];
  searchInput = new FormControl('');
  selectedConcert: Concert | null = null;
  concertGroups: Group[] = [];
  myGroups: Group[] = [];

  loading = {
    concerts: false,
    groups: false,
    myGroups: false
  }

  error = {
    concerts: '',
    groups: '',
    myGroups: ''
  };

  private searchSub !: Subscription;

  private concertSvc = inject(ConcertService);
  private groupSvc = inject(GroupService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  ngOnInit(): void {
    this.loadUpcomingConcerts();
    this.loadMyGroups();

    this.searchSub = this.searchInput.valueChanges
        .pipe(
          debounceTime(300),    // Waits 300ms after typing stops before emitting
          distinctUntilChanged() // Only emits if the value is different from previous
        )
        .subscribe(term => {    // Subscribes to the filtered stream of values
          this.filterConcerts(term || ''); // Calls filterConcerts with the term
        });
  }

  ngOnDestroy(): void {
    this.searchSub.unsubscribe();
  }

  loadUpcomingConcerts(): void {
    this.loading.concerts = true;
    this.error.concerts = '';

    this.concertSvc.getUpcomingConcerts().subscribe({
      next: (concerts) => {
              // Sort concerts by date (first date in their dates array)
      this.upcomingConcerts = concerts.sort((a, b) => {
        const dateA = new Date(a.dates[0]);
        const dateB = new Date(b.dates[0]);
        return dateA.getTime() - dateB.getTime(); // Ascending order (oldest to newest)
      });
        this.filteredConcerts = [...this.upcomingConcerts];
        this.loading.concerts = false;
      },
      error: (err) => {
        console.error('Error loading concerts', err);
        this.error.concerts = 'Unable to load upcoming concerts. Please try again later.';
        this.loading.concerts = false;
      }
    })
  }

  loadMyGroups(): void {
    this.loading.myGroups = true;
    this.error.myGroups = '';

    this.groupSvc.getMyGroups().subscribe({
      next: (groups) => {
        this.myGroups = groups;
        this.loading.myGroups = false;
      },
      error: (err) => {
        console.error('Error loading my groups', err);
        this.error.myGroups = 'Unable to load your groups. Please try again later.';
        this.loading.myGroups = false;
      }
    });
  }

  loadConcertGroups(concert: Concert): void {
    this.selectedConcert = concert;
    this.loading.groups = true;
    this.error.groups = '';

    this.groupSvc.getGroupsByConcert(concert.id).subscribe({
      next: (groups) => {
        this.concertGroups = groups;
        this.loading.groups = false;
      },
      error: (err) => {
        console.error('Error loading concert groups', err);
        this.error.groups = 'Unable to load groups for this concert. Please try again later.';
        this.loading.groups = false;
      }
    });
  }

  joinGroup(group: Group): void {
    this.groupSvc.joinGroup(group.id!).subscribe({
      next: () => {
        this.snackBar.open('Join request submitted successfully!', 'Close', { duration: 3000 });
      },
      error: (err) => {
        console.error('Error joining group', err);
        this.snackBar.open('Failed to join group. Please try again later.', 'Close', { duration: 3000 });
      }
    });
  }

  createGroup(concert: Concert): void {
    this.router.navigate(['/create-group', concert.id]);
  }


  formatMultipleDates(concert: Concert): string {
    if (!concert.dates || concert.dates.length === 0) {
      return 'No dates available';
    }

    const dates = concert.dates.map(date => new Date(date));
    dates.sort((a, b) => a.getTime() - b.getTime());

    if (dates.length === 1) {
      return this.formatDate(dates[0]);
    }

    return `${this.formatDate(dates[0])} - ${this.formatDate(dates[dates.length - 1])} (${dates.length} shows)`;
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private filterConcerts(searchTerm: string): void {
    if (!searchTerm) {
      this.filteredConcerts = [...this.upcomingConcerts];
      return;
    }

    const term = searchTerm.toLowerCase();
    this.filteredConcerts = this.upcomingConcerts.filter(concert =>
      concert.artist.toLowerCase().includes(term) ||
      concert.venue.toLowerCase().includes(term)
    );
  }

  private formatDate(date: Date): string {
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric'
    });
  }


}
