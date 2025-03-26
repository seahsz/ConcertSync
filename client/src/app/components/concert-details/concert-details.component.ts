import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConcertService } from '../../services/concert.service';
import { GroupService } from '../../services/group.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { Concert } from '../../models/concert';
import { Group } from '../../models/group.models';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-concert-details',
  standalone: false,
  templateUrl: './concert-details.component.html',
  styleUrl: './concert-details.component.css'
})
export class ConcertDetailsComponent implements OnInit {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private concertSvc = inject(ConcertService);
  private groupSvc = inject(GroupService);
  private snackBar = inject(MatSnackBar);
  private authSvc = inject(AuthService);
  private title = inject(Title);

  protected concert: Concert | null = null;
  protected relatedGroups: Group[] = [];
  protected isLoading = true;
  protected isLoadingGroups = false;
  protected error = '';
  protected isLoggedIn = false;

  private monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

    ngOnInit(): void {
      this.isLoggedIn = this.authSvc.isLoggedIn();
      const concertId = this.route.snapshot.paramMap.get('id');

      if (!concertId) {
        this.error = "No concert ID provided";
        this.isLoading = false;
        return;
      }
      this.loadConcertDetails(+concertId); // set title inside this method because of async issues
    }

    private loadConcertDetails(concertId: number): void {
      this.isLoading = true;
      this.concertSvc.getConcertById(concertId).subscribe({
        next: (concert) => {
          this.concert = concert;
          this.isLoading = false;

          if (this.concert) {
            const dateString = this.getFormattedDateList();
            this.title.setTitle(`${this.concert.artist} Concert | ${dateString} | ConcertSync`);
          }
          
          // Once we have concert details, load related groups
          if (this.isLoggedIn) {
            this.loadConcertGroups(concertId);
          }
        },
        error: (err) => {
          console.error('Error loading concert details:', err);
          this.error = 'Failed to load concert details. Please try again later.';
          this.isLoading = false;
        }
      });
    }

    private loadConcertGroups(concertId: number): void {
      this.isLoadingGroups = true;
      this.groupSvc.getGroupsByConcert(concertId).subscribe({
        next: (groups) => {
          this.relatedGroups = groups;
          this.isLoadingGroups = false;
        },
        error: (err) => {
          console.error('Error loading concert groups:', err);
          this.isLoadingGroups = false;
        }
      });
    }

    protected getFormattedDateList(): string {
      if (!this.concert || !this.concert.dates || this.concert.dates.length === 0) {
        return 'TBD';
      }
  
      const dates = this.concert.dates.map(date => new Date(date))
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

    protected navigateBack(): void {
      this.router.navigate(['/']);
    }
    
    protected createGroup(): void {
      if (this.concert) {
        this.router.navigate(['/create-group', this.concert.id]);
      }
    }

    protected joinGroup(groupId: number): void {
      this.groupSvc.joinGroup(groupId).subscribe({
        next: () => {
          this.snackBar.open('Join request submitted successfully!', 'Close', { duration: 3000 });
        },
        error: (err) => {
          console.error('Error joining group', err);
          this.snackBar.open('Failed to join group. Please try again later.', 'Close', { duration: 3000 });
        }
      });
    }
}
