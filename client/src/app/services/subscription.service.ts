import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";

@Injectable()
export class SubscriptionService {
    private http = inject(HttpClient);
    private readonly baseUrl = '/api/protected/subscription';

    createCheckoutSession(): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/create-checkout`, {});
    }

    reactivateSubscription(): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/reactivate`, {});
      }

    cancelSubscription(): Observable<any> {
        return this.http.post<any>(`${this.baseUrl}/cancel`, {});
    }

}