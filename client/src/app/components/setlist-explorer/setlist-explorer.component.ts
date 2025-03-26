import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ConcertService } from '../../services/concert.service';
import { SetlistStore } from '../../services/setlist.store';
import { debounceTime, distinctUntilChanged, filter, Observable } from 'rxjs';
import { Setlist } from '../../models/setlist.models';

@Component({
  selector: 'app-setlist-explorer',
  standalone: false,
  templateUrl: './setlist-explorer.component.html',
  styleUrl: './setlist-explorer.component.css'
})
export class SetlistExplorerComponent implements OnInit {

  private fb = inject(FormBuilder);
  private concertSvc = inject(ConcertService);
  private setlistStore = inject(SetlistStore);

  searchForm!: FormGroup;
  availableArtists: string[] = [];
  filteredArtists: string[] = [];

  // Observables from store
  setlists$: Observable<Setlist[]> = this.setlistStore.setlists$;
  loading$: Observable<boolean> = this.setlistStore.loading$;
  error$: Observable<string | null> = this.setlistStore.error$;
  selectedArtist$: Observable<string | null> = this.setlistStore.selectedArtist$;

  expandedSetlistId: string | null = null;

  ngOnInit(): void {
    this.initForm();
    this.loadInitialData();
    this.setupSearchListener();
  }

  private initForm(): void {
    this.searchForm = this.fb.group({
      artist: ['']
    })
  }

  private loadInitialData(): void {
    this.concertSvc.getUpcomingConcerts().subscribe({
      next: (concerts) => {
        // Extract unique artist names
        this.availableArtists = Array.from(
          new Set(concerts.map(concert => concert.artist))
        ).sort();
      },
      error: (err) => console.error("Error loading artists from concert: ", err)
    });

    // Initially load all setlists
    this.setlistStore.loadAllSetlists();
  }

  private setupSearchListener(): void {
    this.searchForm.get('artist')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged(),
    )
    .subscribe((artistName: string) => {
      // Handles autocomplete options
      if (artistName) {
        const searchTerm = artistName.toLowerCase();
        this.filteredArtists = this.availableArtists.filter(
          artist => artist.toLowerCase().includes(searchTerm)
        );
      } else {
        this.filteredArtists = this.availableArtists;
      }

      // Handles search submission
      if (artistName && artistName.trim() !== "") {
        this.searchSetlists(artistName);
      }else {
        this.setlistStore.loadAllSetlists();
        this.setlistStore.setSelectedArtist(null);
      }
    })
  }

  private searchSetlists(artistName: string): void {
    if (artistName && artistName.trim() !== '') {
      this.setlistStore.loadSetlistsByArtist(artistName);
    } else {
      this.setlistStore.loadAllSetlists();
      this.setlistStore.setSelectedArtist(null);
    }
  }

  clearSearch(): void {
    this.searchForm.get('artist')?.setValue('');
    this.setlistStore.loadAllSetlists();
    this.setlistStore.setSelectedArtist(null);
  }

  toggleSetlistExpansion(setlistId: string): void {
    this.expandedSetlistId = this.expandedSetlistId === setlistId ? null : setlistId;
  }

  isSetlistExpanded(setlistId: string): boolean {
    return this.expandedSetlistId === setlistId;
  }

  formatDate(dateString: string): string {
    // Convert from DD-MM-YYYY to a more readable format
    if (!dateString) return '';
    
    const parts = dateString.split('-');
    if (parts.length !== 3) return dateString;
    
    const day = parts[0];
    const month = parts[1];
    const year = parts[2];
    
    const date = new Date(`${year}-${month}-${day}`);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    });
  }

}
