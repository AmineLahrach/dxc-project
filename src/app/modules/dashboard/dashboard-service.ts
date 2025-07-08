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
    // Mock data for development - replace with real API call
    const mockStats: DashboardStats = {
      totalPlans: 24,
      activePlans: 18,
      completedPlans: 6,
      pendingApprovals: 3,
      recentPlans: [
        {
          id: 1,
          title: 'Q1 Marketing Strategy',
          status: 'IN_PROGRESS',
          progress: 75,
          dueDate: '2025-01-15',
          responsible: 'John Doe'
        },
        {
          id: 2,
          title: 'Customer Satisfaction Improvement',
          status: 'PLANNING',
          progress: 25,
          dueDate: '2025-01-20',
          responsible: 'Jane Smith'
        },
        {
          id: 3,
          title: 'Digital Transformation',
          status: 'TRACKING',
          progress: 90,
          dueDate: '2025-01-10',
          responsible: 'Mike Johnson'
        }
      ],
      plansByStatus: {
        'Planning': 8,
        'In Progress': 10,
        'Tracking': 4,
        'Completed': 6
      },
      plansByServiceLine: {
        'IT': 12,
        'Finance': 5,
        'HR': 4,
        'Operations': 7
      },
      monthlyProgress: [
        { month: 'Jan', completed: 5, created: 8 },
        { month: 'Feb', completed: 7, created: 6 },
        { month: 'Mar', completed: 4, created: 9 },
        { month: 'Apr', completed: 8, created: 5 },
        { month: 'May', completed: 6, created: 7 },
        { month: 'Jun', completed: 9, created: 4 }
      ],
      userActivity: [
        {
          id: 1,
          user: 'John Doe',
          action: 'Created plan',
          target: 'Marketing Strategy Q2',
          timestamp: '2025-01-07T10:30:00Z'
        },
        {
          id: 2,
          user: 'Jane Smith',
          action: 'Updated variable',
          target: 'Customer Satisfaction Score',
          timestamp: '2025-01-07T09:15:00Z'
        },
        {
          id: 3,
          user: 'Admin',
          action: 'Approved plan',
          target: 'Digital Transformation',
          timestamp: '2025-01-07T08:45:00Z'
        }
      ]
    };

    return of(mockStats);
    
    // Uncomment when backend is ready:
    // return this._httpClient.get<DashboardStats>(`${environment.apiUrl}/analytics/dashboard`);
  }

  getRecentActivities(): Observable<UserActivity[]> {
    // Mock data - replace with real API call
    return of([
      {
        id: 1,
        user: 'John Doe',
        action: 'Created plan',
        target: 'Marketing Strategy Q2',
        timestamp: '2025-01-07T10:30:00Z'
      }
    ]);
    
    // Uncomment when backend is ready:
    // return this._httpClient.get<UserActivity[]>(`${environment.apiUrl}/audit/recent`);
  }
}