export interface Group {
    id: number,
    name: string,
    description: string,
    concertId: number,
    concertDate: string,
    creatorId: number,
    capacity: number,
    memberCount: number,
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

export interface GroupMessage {
    id: number;
    groupId: number;
    userId: number;
    message: string;
    createdAt: string;
    updatedAt: string;

    // Additional properties from joined user data
    username?: string;
    name?: string;
    profilePictureUrl?: string;
}