import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from 'environments/environment';

export interface DashboardStats {
  totalPlans: number;
  activePlans: number;
  completedPlans: number;
  pendingApprovals: number;
  recentPlans: any[];
  plansByStatus: any;
  plansByServiceLine: any;
  monthlyProgress: any[];
  userActivity: any[];
  totalUsers?: number;
  totalServiceLines?: number;
  activeExercises?: number;
  recentAudits: {
      date: string;
      action: string;
      details: string;
      user: {
          initials: string;
          name: string;
          id: number;
      };
  }[];
}

export interface PlanSummary {
  id: number;
  title: string;
  status: string;
  progress: number;
  dueDate: string;
  responsible: string;
}

export interface UserActivity {
  id: number;
  user: string;
  action: string;
  target: string;
  timestamp: string;
}

export interface CollaboratorDashboardStats {
  plansByStatus?: any; 
  plansByServiceLine?: any;
  monthlyProgress?: any[];
  userActivity?: UserActivity[];

  myPlansInProgress: number;
  myPlansCompleted: number;
  myPlansPending: number;
  myVariableActions: number;
  totalPlans: number;
  totalVariables: number;
  recentAudits: {
    date: string;
    action: string;
    details: string;
    user: {
      initials: string;
      name: string;
      id: number;
    };
  }[];
}

export interface DirectorDashboardStats {
  pendingValidation: number;
  completionRate: number;
  activePlans: number;
  totalExercises: number;
  auditLogs: {
    date: string;
    action: string;
    details: string;
    user: {
      initials: string;
      name: string;
      id: number;
    };
  }[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private _httpClient: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this._httpClient.get<DashboardStats>(`${environment.apiUrl}/dashboard/admin/stats`);
  }

  getDashboardStatsForCollaborator(): Observable<CollaboratorDashboardStats> {
    return this._httpClient.get<CollaboratorDashboardStats>(`${environment.apiUrl}/dashboard/collaborator/stats`);
  }

  getRecentActivities(): Observable<UserActivity[]> {
    return this._httpClient.get<UserActivity[]>(`${environment.apiUrl}/audit/recent`);
  }

  getDashboardStatsForDirector(): Observable<DirectorDashboardStats> {
    return this._httpClient.get<DirectorDashboardStats>(`${environment.apiUrl}/dashboard/director/stats`);
  }
}