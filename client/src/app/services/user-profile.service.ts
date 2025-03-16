import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { UserProfile } from "../models/user-profile";
import { ProfileUpdateRequest } from "../models/profile-update-request";

@Injectable()
export class UserProfileService{
    private http = inject(HttpClient);

    private readonly API_URL = "/api/protected";

    getUserProfile(): Observable<UserProfile> {
        return this.http.get<UserProfile>(`${this.API_URL}/get-profile`);
    }

    updatePhoneNumber(phoneNumber: string): Observable<UserProfile> {
        const request: ProfileUpdateRequest = {
            phoneNumber
        };
        return this.http.put<UserProfile>(`${this.API_URL}/update/phone-number`, request);
    }

    updateName(name: string): Observable<UserProfile> {
        const request: ProfileUpdateRequest = {
            name
        };
        return this.http.put<UserProfile>(`${this.API_URL}/update/name`, request);
    }

    updateProfilePicture(file: File): Observable<UserProfile> {
        const formData = new FormData();
        formData.append("file", file);

        return this.http.put<UserProfile>(`${this.API_URL}/update/profile-picture`, formData);
    }
}