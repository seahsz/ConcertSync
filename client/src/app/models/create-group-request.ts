export interface CreateGroupRequest {
    name: string;
    description: string;
    concertId: number;
    concertDate: string;
    capacity: number;
    isPublic: boolean;
}