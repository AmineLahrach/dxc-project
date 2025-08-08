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

  private apiUrl = `${environment.apiUrl}/variable-actions`

  constructor(private _httpClient: HttpClient) {}

  get variables$(): Observable<VariableAction[]> {
    return this._variables.asObservable();
  }

  get selectedVariable$(): Observable<VariableAction | null> {
    return this._selectedVariable.asObservable();
  }

  getVariables(filter?: VariableFilter): Observable<VariableAction[]> {
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

  getVariableByIdForEdit(id: number): Observable<VariableAction> {   
    return this._httpClient.get<VariableAction>(`${environment.apiUrl}/variable-actions/edit/${id}`);
  }

  // Create new variable
  createVariable(variable: any) {
    return this._httpClient.post(`${environment.apiUrl}/variable-actions`, variable);
  }

  // Update variable
  updateVariable(id: number, variable: any) {

    return this._httpClient.put(`${environment.apiUrl}/variable-actions/${id}`, variable);
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
  updateVariableProgress(id: number, progress: number){
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

  // Get hierarchical tree structure
  getVariableActionHierarchy(planActionId?: number): Observable<any[]> {
    const params = planActionId ? { planActionId: planActionId.toString() } : {};
    return this._httpClient.get<any[]>(`${this.apiUrl}/hierarchy`, { params });
  }

  // Create variable action
  createVariableAction(variableAction: any): Observable<any> {
    return this._httpClient.post<any>(this.apiUrl, variableAction);
  }

  // Update variable action
  updateVariableAction(id: number, variableAction: any): Observable<any> {
    return this._httpClient.put<any>(`${this.apiUrl}/${id}`, variableAction);
  }

  // Delete variable action
  deleteVariableAction(id: number): Observable<void> {
    return this._httpClient.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Create child variable
  createChildVariable(parentId: number, variableAction: any): Observable<any> {
    return this._httpClient.post<any>(`${this.apiUrl}/${parentId}/children`, variableAction);
  }

  // Move variable to different parent
  moveVariableAction(id: number, newParentId?: number): Observable<any> {
    const params = newParentId ? { newParentId: newParentId.toString() } : {};
    return this._httpClient.put<any>(`${this.apiUrl}/${id}/move`, {}, { params });
  }

  // Recalculate weights for parent
  recalculateWeights(parentId: number): Observable<string> {
    return this._httpClient.put<string>(`${this.apiUrl}/${parentId}/recalculate-weights`, {});
  }

  // Update fixed status
  updateFige(id: number, fige: boolean): Observable<any> {
    return this._httpClient.put<any>(`${this.apiUrl}/${id}/fige`, { fige });
  }

  // Get variable actions dropdown
  getVariableActionsDropdown(planId: number): Observable<VariableAction[]> {
    return this._httpClient.get<VariableAction[]>(`${this.apiUrl}/dropdown/${planId}`);
  }
}