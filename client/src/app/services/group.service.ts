import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { Group, GroupWithMembers } from "../models/group.models";
import { map, Observable, tap } from "rxjs";
import { CreateGroupRequest } from "../models/create-group-request";

@Injectable()
export class GroupService {

    private apiUrl = "/api/protected/groups";

    private http = inject(HttpClient);

    createGroup(request: CreateGroupRequest): Observable<Group> {
        return this.http.post<any>(this.apiUrl, request).pipe(
            map((response) => {
                console.info("Create Group [Response]: ", response.group);
                return response.group as Group;
            })
        );
    }

    getGroupsByConcert(concertId: number): Observable<Group[]> {
        return this.http.get<any>(`${this.apiUrl}/concert/${concertId}`).pipe(
            map((response) => {
                console.info("Getting Group By Concert [Response]: ", response.groups);
                return response.groups as Group[];
            })
        )
    }

    getMyGroups(): Observable<Group[]> {
        return this.http.get<any>(`${this.apiUrl}/my-groups`).pipe(
            map((response) => {
                console.info("Getting My Groups [Response]: ", response.groups);
                return response.groups as Group[];
            })
        );
    }

    getGroupDetails(groupId: number): Observable<GroupWithMembers> {
        return this.http.get<any>(`${this.apiUrl}/${groupId}`).pipe(
            map((response) => {
                const groupWithMembers: GroupWithMembers = {
                    ...response.group,
                    members: response.members,
                    memberCount: response.memberCount
                };
                console.info("Getting Group Details [Response]: ", groupWithMembers);
                return groupWithMembers;
            })
        )
    }

    joinGroup(groupId: number): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${groupId}/request`, {});
    }

    approveJoinRequest(groupId: number, userId: number): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${groupId}/approve/${userId}`, {});
    }

    rejectJoinRequest(groupId: number, userId: number): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${groupId}/reject/${userId}`, {});
    }

    leaveGroup(groupId: number): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${groupId}/leave`, {});
    }

    getPendingRequests(groupId: number): Observable<any[]> {
        return this.http.get<any>(`${this.apiUrl}/${groupId}/pending-requests`).pipe(
            map(response => response.pendingRequests)
        );
    }
}