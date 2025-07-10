import { Profile } from "./business.models";

// src/app/core/models/auth.models.ts
export interface LoginRequest {
  usernameOrEmail: string;
  motDePasse: string;
}

export interface JwtResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  email: string;
  nom: string;
  prenom: string;
  roles: string[];
  serviceLine: string;
}

export interface User {
  id: string;
  nom: string;
  prenom: string;
  email: string;
  username: string;
  roles: string[];
  serviceLine: string;
  actif: boolean;
  createdAt: string;
  updatedAt: string;
  avatar?: string; // Add this
  status?: 'online' | 'away' | 'busy' | 'not-visible'; // Add this
}

export interface SignupRequest {
  nom: string;
  prenom: string;
  username: string;
  email: string;
  motDePasse: string;
  roles: string[];
  serviceLine: number;
}