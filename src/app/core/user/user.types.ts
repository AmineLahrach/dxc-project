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