import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from 'environments/environment';

export interface DashboardStats {
  totalPlans: number;
  activePlans: number;
  completedPlans: number;
  pendingApprovals: number;
  recentPlans: PlanSummary[];
  plansByStatus: { [key: string]: number };
  plansByServiceLine: { [key: string]: number };
  monthlyProgress: { month: string; completed: number; created: number }[];
  userActivity: UserActivity[];
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

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(private _httpClient: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this._httpClient.get<DashboardStats>(`${environment.apiUrl}/dashboard/admin/stats`);
  }

  getRecentActivities(): Observable<UserActivity[]> {
    return this._httpClient.get<UserActivity[]>(`${environment.apiUrl}/audit/recent`);
  }
}