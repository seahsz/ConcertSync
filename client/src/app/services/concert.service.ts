import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Concert } from '../models/concert';

@Injectable({
  providedIn: 'root'
})
export class ConcertService {

  private http = inject(HttpClient);

  private upcomingConcertUrl = "/api/concerts/upcoming";

  getUpcomingConcerts(): Observable<Concert[]> {
    return this.http.get<Concert[]>(this.upcomingConcertUrl);
  }
}
