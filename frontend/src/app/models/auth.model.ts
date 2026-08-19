export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  fullName: string;
  role: string;
}

export interface Department {
  id: number;
  name: string;
  description: string;
}
