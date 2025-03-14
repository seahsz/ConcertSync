export interface AuthResponse {
    success: boolean;
    message?: string;
    token?: string;
    errors?: any;
}