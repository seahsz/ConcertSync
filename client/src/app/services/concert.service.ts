import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, of } from 'rxjs';
import { Concert } from '../models/concert';

@Injectable({
  providedIn: 'root'
})
export class ConcertService {

  private http = inject(HttpClient);

  private apiUrl = "/api/concerts";

  private concertCache: Map<number, Concert> = new Map();

  getUpcomingConcerts(): Observable<Concert[]> {
    return this.http.get<Concert[]>(`${this.apiUrl}/upcoming`);
  }

  getConcertById(id: number): Observable<Concert> {
    // Check if we already have this concert in our cache
    if (this.concertCache.has(id)) {
      return of(this.concertCache.get(id)!);
    }

    // Otherwise, fetch it from the API
    return this.http.get<Concert>(`${this.apiUrl}/${id}`).pipe(
      map(concert => {
        this.concertCache.set(id, concert);
        return concert;
      })
    );
  }
}
