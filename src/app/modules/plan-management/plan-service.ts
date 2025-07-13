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

  constructor(private _httpClient: HttpClient) {}

  get plans$(): Observable<PlanAction[]> {
    return this._plans.asObservable();
  }

  get selectedPlan$(): Observable<PlanAction | null> {
    return this._selectedPlan.asObservable();
  }

  // Get all plans with optional filtering
  getPlans(filter?: PlanActionFilter): Observable<PlanAction[]> {
    // Mock data for development
    // const mockPlans: PlanAction[] = [
    //   {
    //     id: 1,
    //     titre: 'Q1 Marketing Strategy',
    //     description: 'Develop comprehensive marketing strategy for Q1 2025 focusing on digital transformation and customer acquisition.',
    //     statut: ActionPlanStatus.IN_PROGRESS,
    //     exercice: { id: 1, annee: 2025, verrouille: false },
    //     progress: 75,
    //     dueDate: '2025-03-31',
    //     createdAt: '2025-01-01T00:00:00Z',
    //     createdBy: 'John Doe'
    //   },
    //   {
    //     id: 2,
    //     titre: 'Customer Satisfaction Improvement',
    //     description: 'Initiative to improve customer satisfaction scores through service quality enhancement.',
    //     statut: ActionPlanStatus.PLANNING,
    //     exercice: { id: 1, annee: 2025, verrouille: false },
    //     progress: 25,
    //     dueDate: '2025-06-30',
    //     createdAt: '2025-01-02T00:00:00Z',
    //     createdBy: 'Jane Smith'
    //   },
    //   {
    //     id: 3,
    //     titre: 'Digital Transformation Initiative',
    //     description: 'Comprehensive digital transformation plan to modernize business processes and technology stack.',
    //     statut: ActionPlanStatus.TRACKING,
    //     exercice: { id: 1, annee: 2025, verrouille: false },
    //     progress: 90,
    //     dueDate: '2025-12-31',
    //     createdAt: '2024-12-15T00:00:00Z',
    //     createdBy: 'Mike Johnson'
    //   }
    // ];

    // // Apply filtering if provided
    // let filteredPlans = mockPlans;
    // if (filter) {
    //   if (filter.status?.length) {
    //     filteredPlans = filteredPlans.filter(plan => filter.status!.includes(plan.statut));
    //   }
    //   if (filter.searchTerm) {
    //     const term = filter.searchTerm.toLowerCase();
    //     filteredPlans = filteredPlans.filter(plan => 
    //       plan.titre.toLowerCase().includes(term) || 
    //       plan.description.toLowerCase().includes(term)
    //     );
    //   }
    // }

    // this._plans.next(filteredPlans);
    // return of(filteredPlans);

    // Uncomment when backend is ready:
    let params = new HttpParams();
    if (filter) {
      if (filter.status?.length) params = params.set('status', filter.status.join(','));
      if (filter.searchTerm) params = params.set('search', filter.searchTerm);
    }
    return this._httpClient.get<PlanAction[]>(`${environment.apiUrl}/plans`, { params });
  }

  // Get single plan by ID
  getPlanById(id: number): Observable<PlanAction> {
    // const mockPlan: PlanAction = {
    //   id: id,
    //   titre: 'Q1 Marketing Strategy',
    //   description: 'Develop comprehensive marketing strategy for Q1 2025',
    //   statut: ActionPlanStatus.IN_PROGRESS,
    //   exercice: { id: 1, annee: 2025, verrouille: false },
    //   progress: 75,
    //   variableActions: [
    //     {
    //       id: 1,
    //       description: 'Social Media Campaign',
    //       poids: 0.4,
    //       fige: false,
    //       niveau: 1,
    //       responsable: { id: "1", nom: 'Doe', prenom: 'John', username: 'john.doe', email: 'john@example.com', roles: [], serviceLine: 'Marketing', actif: true, createdAt: '', updatedAt: '' },
    //       planAction: {} as PlanAction,
    //       progress: 80
    //     },
    //     {
    //       id: 2,
    //       description: 'Content Marketing Strategy',
    //       poids: 0.6,
    //       fige: false,
    //       niveau: 1,
    //       responsable: { id: "2", nom: 'Smith', prenom: 'Jane', username: 'jane.smith', email: 'jane@example.com', roles: [], serviceLine: 'Marketing', actif: true, createdAt: '', updatedAt: '' },
    //       planAction: {} as PlanAction,
    //       progress: 70
    //     }
    //   ]
    // };

    // this._selectedPlan.next(mockPlan);
    // return of(mockPlan);

    // Uncomment when backend is ready:
    return this._httpClient.get<PlanAction>(`${environment.apiUrl}/plans/${id}`);
  }

  // Create new plan
  createPlan(plan: PlanActionCreateRequest): Observable<PlanAction> {
    // const newPlan: PlanAction = {
    //   id: Math.floor(Math.random() * 1000),
    //   titre: plan.titre,
    //   description: plan.description,
    //   statut: ActionPlanStatus.PLANNING,
    //   exercice: { id: plan.exerciceId, annee: 2025, verrouille: false },
    //   progress: 0,
    //   createdAt: new Date().toISOString(),
    //   createdBy: 'Current User'
    // };

    // // Update local state
    // const currentPlans = this._plans.value;
    // this._plans.next([newPlan, ...currentPlans]);

    // return of(newPlan);

    // Uncomment when backend is ready:
    return this._httpClient.post<PlanAction>(`${environment.apiUrl}/plans`, plan);
  }

  // Update existing plan
  updatePlan(id: number, plan: PlanActionCreateRequest): Observable<PlanAction> {
    // const currentPlans = this._plans.value;
    // const index = currentPlans.findIndex(p => p.id === id);
    
    // if (index > -1) {
    //   const updatedPlan = { ...currentPlans[index], ...plan };
    //   currentPlans[index] = updatedPlan;
    //   this._plans.next([...currentPlans]);
    //   this._selectedPlan.next(updatedPlan);
    //   return of(updatedPlan);
    // }

    // throw new Error('Plan not found');

    // Uncomment when backend is ready:
    return this._httpClient.put<PlanAction>(`${environment.apiUrl}/plans/${id}`, plan);
  }

  // Delete plan
  deletePlan(id: number): Observable<void> {
    // const currentPlans = this._plans.value;
    // const filteredPlans = currentPlans.filter(p => p.id !== id);
    // this._plans.next(filteredPlans);

    // return of(void 0);

    // Uncomment when backend is ready:
    return this._httpClient.delete<void>(`${environment.apiUrl}/plans/${id}`);
  }

  // Get available exercises
  getExercises(): Observable<Exercise[]> {
    // const mockExercises: Exercise[] = [
    //   { id: 1, annee: 2025, verrouille: false, description: 'Strategic Planning 2025' },
    //   { id: 2, annee: 2024, verrouille: true, description: 'Strategic Planning 2024' }
    // ];

    // return of(mockExercises);

    // Uncomment when backend is ready:
    return this._httpClient.get<Exercise[]>(`${environment.apiUrl}/exercices`);
  }

  // Update plan status (for approvals)
  updatePlanStatus(id: number, status: ActionPlanStatus): Observable<PlanAction> {
    return this._httpClient.patch<PlanAction>(
      `${environment.apiUrl}/plans/${id}/status`,
      { status }, // Send as JSON object
      { headers: { 'Content-Type': 'application/json' } }
    );
  }

  // Get plans for approval (Director only)
  getPlansForApproval(): Observable<PlanAction[]> {
    return this.getPlans({ status: [ActionPlanStatus.PLANNING] });
  }
}