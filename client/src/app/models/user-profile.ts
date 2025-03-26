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
    subscription_id?: string;
    auto_renew?: boolean;
    last_name_update: string;
}