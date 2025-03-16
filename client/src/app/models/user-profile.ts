export interface UserProfile {
    username: string;
    email: string;
    name: string;
    birth_date: string;
    profile_picture_url: string;
    phone_number: string;
    created_at: string;
    premium_status: boolean;
    premium_expiry?: string;
    last_name_update: string;
}