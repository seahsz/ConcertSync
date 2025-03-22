export interface Group {
    id?: number,
    name: string,
    description: string,
    concertId: number,
    concertDate: string,
    creatorId: number,
    capacity: number,
    isPublic: boolean,
    createdAt?: string
}

export interface GroupWithMembers extends Group {
    members: GroupMember[];
    memberCount: number;
}

export interface GroupMember {
    id: number;
    username: string;
    name: string;
    profilePictureUrl: string;
}