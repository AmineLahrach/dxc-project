import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, BehaviorSubject } from 'rxjs';
import { environment } from 'environments/environment';
import { VariableAction, PlanAction } from 'app/models/business.models';
import { User } from 'app/models/auth.models';

export interface VariableFilter {
  searchTerm?: string;
  planId?: number;
  responsibleId?: number;
  niveau?: number[];
  status?: string[];
}

export interface VariableActionCreateRequest {
  description: string;
  poids: number;
  niveau: number;
  responsableId: number;
  planActionId: number;
  vaMereId?: number;
}

export interface VariableHierarchy {
  variable: VariableAction;
  children: VariableHierarchy[];
  level: number;
}

@Injectable({
  providedIn: 'root'
})
export class VariableService {
  private _variables: BehaviorSubject<VariableAction[]> = new BehaviorSubject([]);
  private _selectedVariable: BehaviorSubject<VariableAction | null> = new BehaviorSubject(null);

  constructor(private _httpClient: HttpClient) {}

  get variables$(): Observable<VariableAction[]> {
    return this._variables.asObservable();
  }

  get selectedVariable$(): Observable<VariableAction | null> {
    return this._selectedVariable.asObservable();
  }

  // Get all variables with optional filtering
  getVariables(filter?: VariableFilter): Observable<VariableAction[]> {
    // const mockVariables: VariableAction[] = [
    //   {
    //     id: 1,
    //     description: 'Customer Satisfaction Score',
    //     poids: 0.4,
    //     fige: false,
    //     niveau: 1,
    //     responsable: {
    //       id: "1",
    //       nom: 'Doe',
    //       prenom: 'John',
    //       username: 'john.doe',
    //       email: 'john.doe@dxc.com',
    //       roles: ['ROLE_COLLABORATEUR'],
    //       serviceLine: 'Customer Service',
    //       actif: true,
    //       createdAt: '2024-01-15T00:00:00Z',
    //       updatedAt: '2025-01-07T00:00:00Z'
    //     },
    //     planAction: {
    //       id: 1,
    //       titre: 'Customer Experience Improvement',
    //       description: 'Enhance customer satisfaction',
    //       statut: 'EN_COURS' as any,
    //       exercice: { id: 1, annee: 2025, verrouille: false }
    //     },
    //     progress: 75,
    //     status: 'On Track'
    //   },
    //   {
    //     id: 2,
    //     description: 'Response Time Improvement',
    //     poids: 0.3,
    //     fige: false,
    //     niveau: 2,
    //     vaMere: {
    //       id: 1,
    //       description: 'Customer Satisfaction Score',
    //       poids: 0.4,
    //       fige: false,
    //       niveau: 1,
    //       responsable: {} as User,
    //       planAction: {} as PlanAction
    //     },
    //     responsable: {
    //       id: "2",
    //       nom: 'Smith',
    //       prenom: 'Jane',
    //       username: 'jane.smith',
    //       email: 'jane.smith@dxc.com',
    //       roles: ['ROLE_COLLABORATEUR'],
    //       serviceLine: 'Customer Service',
    //       actif: true,
    //       createdAt: '2024-01-10T00:00:00Z',
    //       updatedAt: '2025-01-05T00:00:00Z'
    //     },
    //     planAction: {
    //       id: 1,
    //       titre: 'Customer Experience Improvement',
    //       description: 'Enhance customer satisfaction',
    //       statut: 'EN_COURS' as any,
    //       exercice: { id: 1, annee: 2025, verrouille: false }
    //     },
    //     progress: 60,
    //     status: 'Behind Schedule'
    //   },
    //   {
    //     id: 3,
    //     description: 'Digital Transformation KPIs',
    //     poids: 0.5,
    //     fige: true,
    //     niveau: 1,
    //     responsable: {
    //       id: "3",
    //       nom: 'Johnson',
    //       prenom: 'Mike',
    //       username: 'mike.johnson',
    //       email: 'mike.johnson@dxc.com',
    //       roles: ['ROLE_ADMINISTRATEUR'],
    //       serviceLine: 'Information Technology',
    //       actif: true,
    //       createdAt: '2024-01-01T00:00:00Z',
    //       updatedAt: '2025-01-01T00:00:00Z'
    //     },
    //     planAction: {
    //       id: 2,
    //       titre: 'Digital Transformation Initiative',
    //       description: 'Modernize business processes',
    //       statut: 'SUIVI_REALISATION' as any,
    //       exercice: { id: 1, annee: 2025, verrouille: false }
    //     },
    //     progress: 90,
    //     status: 'Ahead of Schedule'
    //   }
    // ];

    // // Apply filtering
    // let filteredVariables = mockVariables;
    // if (filter) {
    //   if (filter.searchTerm) {
    //     const term = filter.searchTerm.toLowerCase();
    //     filteredVariables = filteredVariables.filter(variable =>
    //       variable.description.toLowerCase().includes(term) ||
    //       variable.responsable.nom.toLowerCase().includes(term) ||
    //       variable.responsable.prenom.toLowerCase().includes(term)
    //     );
    //   }
    //   if (filter.planId) {
    //     filteredVariables = filteredVariables.filter(variable => 
    //       variable.planAction.id === filter.planId
    //     );
    //   }
    //   if (filter.niveau?.length) {
    //     filteredVariables = filteredVariables.filter(variable => 
    //       filter.niveau!.includes(variable.niveau)
    //     );
    //   }
    // }

    // this._variables.next(filteredVariables);
    // return of(filteredVariables);

    // Uncomment when backend is ready:
    let params = new HttpParams();
    if (filter) {
      if (filter.searchTerm) params = params.set('search', filter.searchTerm);
      if (filter.planId) params = params.set('planId', filter.planId.toString());
    }
    return this._httpClient.get<VariableAction[]>(`${environment.apiUrl}/variable-actions`, { params });
  }

  // Get variable by ID
  getVariableById(id: number): Observable<VariableAction> {   
    return this._httpClient.get<VariableAction>(`${environment.apiUrl}/variable-actions/${id}`);
  }

  // Create new variable
  createVariable(variable: VariableActionCreateRequest): Observable<VariableAction> {
    
    return this._httpClient.post<VariableAction>(`${environment.apiUrl}/variable-actions`, variable);
  }

  // Update variable
  updateVariable(id: number, variable: Partial<VariableAction>): Observable<VariableAction> {
    
    return this._httpClient.put<VariableAction>(`${environment.apiUrl}/variable-actions/${id}`, variable);
  }

  // Delete variable
  deleteVariable(id: number): Observable<void> {
    // const currentVariables = this._variables.value;
    // const filteredVariables = currentVariables.filter(v => v.id !== id);
    // this._variables.next(filteredVariables);

    // return of(void 0);

    // Uncomment when backend is ready:
    return this._httpClient.delete<void>(`${environment.apiUrl}/variable-actions/${id}`);
  }

  // Get variables hierarchy
  getVariablesHierarchy(planId?: number): Observable<VariableHierarchy[]> {
    return new Observable(observer => {
      this.getVariables(planId ? { planId } : undefined).subscribe(variables => {
        const hierarchy = this.buildHierarchy(variables);
        observer.next(hierarchy);
        observer.complete();
      });
    });
  }

  private buildHierarchy(variables: VariableAction[]): VariableHierarchy[] {
    const variableMap = new Map<number, VariableAction>();
    variables.forEach(v => variableMap.set(v.id!, v));

    const rootVariables = variables.filter(v => !v.vaMere);
    
    return rootVariables.map(variable => this.buildVariableHierarchy(variable, variableMap, 1));
  }

  private buildVariableHierarchy(
    variable: VariableAction, 
    variableMap: Map<number, VariableAction>, 
    level: number
  ): VariableHierarchy {
    const children = Array.from(variableMap.values())
      .filter(v => v.vaMere?.id === variable.id)
      .map(child => this.buildVariableHierarchy(child, variableMap, level + 1));

    return {
      variable,
      children,
      level
    };
  }

  // Update variable progress
  updateVariableProgress(id: number, progress: number): Observable<VariableAction> {
    return this.updateVariable(id, { progress });
  }

  // Get variables by responsible user
  getVariablesByResponsible(userId: number): Observable<VariableAction[]> {
    return this.getVariables({ responsibleId: userId });
  }

  // Get variables summary for dashboard
  getVariablesSummary(): Observable<any> {
    const summary = {
      totalVariables: 15,
      completedVariables: 8,
      behindSchedule: 3,
      onTrack: 12,
      averageProgress: 72
    };

    return of(summary);
  }
}