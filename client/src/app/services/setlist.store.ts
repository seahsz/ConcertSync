import { inject, Injectable } from '@angular/core';
import { Setlist, SetlistSlice } from '../models/setlist.models';
import { ComponentStore } from '@ngrx/component-store';
import { catchError, Observable, of, switchMap, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const INIT_SLICE: SetlistSlice = {
  setlists: [],
  selectedArtist: null,
  loading: false,
  error: null
}

@Injectable({
  providedIn: 'root'
})
export class SetlistStore extends ComponentStore<SetlistSlice> {

  constructor() { super(INIT_SLICE) }

  private http = inject(HttpClient);

  // SELECTORS
  readonly setlists$ = this.select<Setlist[]>(state => state.setlists);
  readonly selectedArtist$ = this.select<string | null>(state => state.selectedArtist);
  readonly loading$ = this.select<boolean>(state => state.loading);
  readonly error$ = this.select<string | null>(state => state.error);

  // UPDATORS
  readonly setLoading = this.updater<boolean>(
    (slice: SetlistSlice, loading: boolean) => ({
        ...slice,
        loading,
        error: loading ? null : slice.error
    })
  )

  readonly setError = this.updater<string | null>(
    (slice: SetlistSlice, error: string | null) => ({
      ...slice,
      error,
      loading: false
    })
  )

  readonly setSetlists = this.updater<Setlist[]>(
    (slice: SetlistSlice, setlists: Setlist[]) => ({
      ...slice,
      setlists,
      loading: false,
      error: null
    })
  )

  readonly setSelectedArtist = this.updater<string | null>(
    (slice: SetlistSlice, selectedArtist: string | null) => ({
      ...slice,
      selectedArtist
    })
  )

  // EFFECTS
  readonly loadAllSetlists = this.effect(
    (trigger$: Observable<void>) => {
      return trigger$.pipe(
        tap(() => this.setLoading(true)),
        switchMap(() => // switchMap so that only the latest is emitted (ignore prev requests)
          this.http.get<Setlist[]>('/api/setlists').pipe(
            tap(setlists => this.setSetlists(setlists)),
            catchError(error => {
              console.error('Error loading setlists: ', error);
              this.setError('Failed to load setlists. Please try again');
              return of([]);
            })
          )
        )
      )
    }
  )

  readonly loadSetlistsByArtist = this.effect((artist$: Observable<string>) => {
    return artist$.pipe(
      tap(() => this.setLoading(true)),
      switchMap(artist => 
        this.http.get<Setlist[]>(`/api/setlists/artist/${artist}`).pipe(
          tap(setlists => {
            this.setSetlists(setlists);
            this.setSelectedArtist(artist);
          }),
          catchError(error => {
            console.error(`Error loading setlists for artist ${artist}:`, error);
            this.setError(`Failed to load setlists for ${artist}. Please try again.`);
            return of([]);
          })
        )
      )
    );
  });

}
