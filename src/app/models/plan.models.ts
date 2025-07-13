import { User } from "./auth.models";

export interface PlanAction {
  id?: number;
  titre: string;
  description: string;
  statut: ActionPlanStatus;
  exercice: Exercise;
  variableActions?: VariableAction[];
  createdAt?: string;
  updatedAt?: string;
  createdBy?: string;
  progress?: number;
  dueDate?: string;
}

export interface VariableAction {
  id?: number;
  description: string;
  poids: number;
  fige: boolean;
  niveau: number;
  vaMere?: VariableAction;
  sousVAs?: VariableAction[];
  responsable: User;
  planAction: PlanAction;
  progress?: number;
  status?: string;
}

export interface Exercise {
  id?: number;
  annee: number;
  verrouille: boolean;
  description?: string;
  startDate?: string;
  endDate?: string;
}

export enum ActionPlanStatus {
  PLANNING = 'EN_COURS_PLANIFICATION',
  IN_PROGRESS = 'PLANIFICATION',
  TRACKING = 'SUIVI_REALISATION',
  LOCKED = 'VERROUILLE'
}

export interface PlanActionFilter {
  status?: ActionPlanStatus[];
  serviceLine?: string[];
  dateRange?: {
    start: string;
    end: string;
  };
  searchTerm?: string;
  responsible?: string[];
}

export interface PlanActionCreateRequest {
  titre: string;
  description: string;
  exercice: { id: number };
  dueDate?: string;
  variableActions?: VariableActionCreateRequest[];
}

export interface VariableActionCreateRequest {
  description: string;
  poids: number;
  niveau: number;
  responsableId: number;
  vaMereId?: number;
}