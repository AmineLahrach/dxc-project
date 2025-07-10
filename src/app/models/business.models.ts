import { User } from "./auth.models";

// src/app/core/models/business.models.ts
export interface PlanAction {
  id?: number;
  titre: string;
  description: string;
  statut: ActionPlanStatus;
  exercice: Exercise;
  variableActions?: VariableAction[];
}

export interface VariableAction {
  id?: number;
  description: string;
  poids: number;
  fige: boolean;
  niveau: number;
  vaMere?: VariableAction;
  sousVAs?: VariableAction[];
  progress?: number;
  status?: string;
  responsable: User;
  planAction: PlanAction;
}

export interface Exercise {
  id?: number;
  annee: number;
  verrouille: boolean;
}

export interface ServiceLine {
  id?: number;
  nom: string;
  // description?: string;  
}

export interface Profile {
  id?: number;
  nom: string;
}

export enum ActionPlanStatus {
  IN_PROGRESS = 'EN_COURS',
  PLANNING = 'PLANIFICATION',
  TRACKING = 'SUIVI_REALISATION',
  LOCKED = 'VERROUILLE'
}

export interface Notification {
  id: number;
  titre: string;
  contenu: string;
  type: string;
  date: string;
  recu: boolean;
}