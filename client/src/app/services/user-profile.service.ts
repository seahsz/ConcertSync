import { HttpClient } from "@angular/common/http";
import { EventEmitter, inject, Injectable } from "@angular/core";
import { Observable, tap } from "rxjs";
import { UserProfile } from "../models/user-profile";
import { ProfileUpdateRequest } from "../models/profile-update-request";

@Injectable()
export class UserProfileService{
    private http = inject(HttpClient);

    private readonly API_URL = "/api/protected";

    // Add an event emitter to broadcast profile updates to other components
    profileUpdated = new EventEmitter<UserProfile>();

    getUserProfile(): Observable<UserProfile> {
        return this.http.get<UserProfile>(`${this.API_URL}/get-profile`);
    }

    updatePhoneNumber(phoneNumber: string): Observable<UserProfile> {
        const request: ProfileUpdateRequest = {
            phoneNumber
        };
        return this.http.put<UserProfile>(`${this.API_URL}/update/phone-number`, request)
            .pipe(
                tap(updatedProfile => {
                    this.profileUpdated.emit(updatedProfile);
                })
            );
    }

    updateName(name: string): Observable<UserProfile> {
        const request: ProfileUpdateRequest = {
            name
        };
        return this.http.put<UserProfile>(`${this.API_URL}/update/name`, request)
            .pipe(
                tap(updatedProfile => {
                    this.profileUpdated.emit(updatedProfile);
                })
            );
    }

    updateProfilePicture(file: File): Observable<UserProfile> {
        const formData = new FormData();
        formData.append("file", file);

        return this.http.put<UserProfile>(`${this.API_URL}/update/profile-picture`, formData)
            .pipe(
                tap(updatedProfile => {
                    this.profileUpdated.emit(updatedProfile);
                })
            );
    }

    deleteAccount(): Observable<any> {
        return this.http.delete(`${this.API_URL}/delete-account`);
    }
}