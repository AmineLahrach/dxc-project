import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { environment } from 'environments/environment';
import { PlanAction, PlanActionFilter, PlanActionCreateRequest, ActionPlanStatus, Exercise } 
from 'app/models/plan.models';

@Injectable({
  providedIn: 'root'
})
export class PlanService {
  private _plans: BehaviorSubject<PlanAction[]> = new BehaviorSubject([]);
  private _selectedPlan: BehaviorSubject<PlanAction | null> = new BehaviorSubject(null);
  private apiUrl = `${environment.apiUrl}/plans`
  constructor(private _httpClient: HttpClient) {}

  get plans$(): Observable<PlanAction[]> {
    return this._plans.asObservable();
  }

  get selectedPlan$(): Observable<PlanAction | null> {
    return this._selectedPlan.asObservable();
  }

  getPlans(filter?: PlanActionFilter): Observable<PlanAction[]> {
    let params = new HttpParams();
    if (filter) {
      if (filter.status?.length) params = params.set('status', filter.status.join(','));
      if (filter.searchTerm) params = params.set('search', filter.searchTerm);
    }
    return this._httpClient.get<PlanAction[]>(`${environment.apiUrl}/plans`, { params });
  }
  
  getPlanById(id: number): Observable<PlanAction> {  
    return this._httpClient.get<PlanAction>(`${environment.apiUrl}/plans/${id}`);
  }

  createPlan(plan: PlanActionCreateRequest): Observable<PlanAction> {
    return this._httpClient.post<PlanAction>(`${environment.apiUrl}/plans`, plan);
  }

  updatePlan(id: number, plan: PlanActionCreateRequest): Observable<PlanAction> {
    return this._httpClient.put<PlanAction>(`${environment.apiUrl}/plans/${id}`, plan);
  }

  deletePlan(id: number): Observable<void> {
    return this._httpClient.delete<void>(`${environment.apiUrl}/plans/${id}`);
  }

  getExercises(): Observable<Exercise[]> {
    return this._httpClient.get<Exercise[]>(`${environment.apiUrl}/exercices`);
  }

  updatePlanStatus(id: number, status: ActionPlanStatus): Observable<PlanAction> {
    return this._httpClient.patch<PlanAction>(
      `${environment.apiUrl}/plans/${id}/status`,
      { status },
      { headers: { 'Content-Type': 'application/json' } }
    );
  }

  updatePlanLockStatus(plan: PlanAction): Observable<PlanAction> {
    return this._httpClient.patch<PlanAction>(
      `${environment.apiUrl}/plans/${plan.id}/lock`,
      { verrouille: plan.verrouille },
      { headers: { 'Content-Type': 'application/json' } }
    );
  }

  getPlansForApproval(): Observable<PlanAction[]> {
    return this.getPlans({ status: [ActionPlanStatus.PLANNING] });
  }

  getAllPlans(): Observable<any[]> {
    return this._httpClient.get<any[]>(this.apiUrl);
  }

  getTreeData(): Observable<any[]> {
    return this._httpClient.get<any[]>(`${this.apiUrl}/tree`);
  }

  // getPlanById(id: number): Observable<any> {
  //   return this._httpClient.get<any>(`${this.apiUrl}/${id}`);
  // }

  // createPlan(plan: any): Observable<any> {
  //   return this._httpClient.post<any>(this.apiUrl, plan);
  // }

  // updatePlan(id: number, plan: any): Observable<any> {
  //   return this._httpClient.put<any>(`${this.apiUrl}/${id}`, plan);
  // }

  // deletePlan(id: number): Observable<void> {
  //   return this._httpClient.delete<void>(`${this.apiUrl}/${id}`);
  // }

  // Get plan with variable hierarchy
  getPlanWithHierarchy(id: number): Observable<any> {
    return this._httpClient.get<any>(`${this.apiUrl}/${id}/hierarchy`);
  }
}