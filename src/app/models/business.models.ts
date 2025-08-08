import { User } from "./auth.models";
import { AuditLog } from "./plan.models";

export interface PlanAction {
  id?: number;
  titre: string;
  description: string;
  statut: ActionPlanStatus;
  exercice: Exercise;
  variableActions?: VariableAction[];
}

// export interface AuditLog {
//   date: string;
//   action: string;
//   details: string;
//   user: {
//     initials: string;
//     name: string;
//     id: number;
//   };
// }

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
  auditLogs?: AuditLog[];
  responsableId?: number;
  planActionId?: number;
  vaMereId?: number | null; // Optional parent variable ID
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
  auditLogs?: Array<{
      date: string;
      action: string;
      details: string;
      user: {
          initials: string;
          name: string;
          id: number;
      };
  }>;
}

export interface Profile {
  id?: number;
  nom: string;
  auditLogs?: Array<{
      date: string;
      action: string;
      details: string;
      user: {
          initials: string;
          name: string;
          id: number;
      };
  }>;
}

export enum ActionPlanStatus {
  IN_PROGRESS = 'EN_COURS_PLANIFICATION',
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