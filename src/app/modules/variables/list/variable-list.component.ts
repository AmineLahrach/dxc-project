import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { VariableService, VariableFilter } from '../variable-service';
import { PlanService } from 'app/modules/plan-management/plan-service';
import { AuthService } from 'app/core/auth/auth.service';
import { VariableAction } from 'app/models/business.models';
import { PlanAction, VariableActionListRequest } from 'app/models/plan.models';
import { Router, RouterModule } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { User } from 'app/models/auth.models';
import { SharedModule } from 'app/modules/shared/shared.module';

@Component({
  selector: 'app-variable-list',
    imports: [SharedModule],
  templateUrl: './variable-list.component.html'
})
export class VariableListComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  dataSource: MatTableDataSource<VariableActionListRequest> = new MatTableDataSource();
  displayedColumns: string[] = ['description', 'plan', 'responsible', 'poids', 'niveau', 'progress', 'status', 'actions'];
  
  // Filter controls
  searchControl = new FormControl('');
  planFilter = new FormControl('');
  levelFilter = new FormControl([]);
  
  // Data
  plans: PlanAction[] = [];
  selectedVariables: VariableActionListRequest[] = [];
  currentUser: User | null = null; // Add this line
  
  // Filter options
  levelOptions = [
    { value: 1, label: 'Level 1 (Primary)', color: 'bg-blue-100 text-blue-800' },
    { value: 2, label: 'Level 2 (Secondary)', color: 'bg-green-100 text-green-800' },
    { value: 3, label: 'Level 3 (Tertiary)', color: 'bg-purple-100 text-purple-800' }
  ];

  loading = false;

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _variableService: VariableService,
    private _planService: PlanService,
    private _authService: AuthService,
    private _router: Router,
    private _dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.initializeDataSource();
    this.setupFilters();
    this.loadData();

    this._authService.currentUser$
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(user => this.currentUser = user);
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private initializeDataSource(): void {
    this.dataSource.filterPredicate = (data: VariableActionListRequest, filter: string) => {
      const searchTerm = filter.toLowerCase();
      return data.description.toLowerCase().includes(searchTerm) ||
             data.responsableNom.toLowerCase().includes(searchTerm) ||
             data.responsablePrenom.toLowerCase().includes(searchTerm) ||
             data.planActionNom.toLowerCase().includes(searchTerm);
    };

    this.dataSource.sortingDataAccessor = (data: VariableActionListRequest, sortHeaderId: string) => {
      switch (sortHeaderId) {
        case 'description': return data.description;
        case 'plan': return data.planActionNom;
        case 'responsible': return `${data.responsablePrenom} ${data.responsableNom}`;
        case 'poids': return data.poids;
        case 'niveau': return data.niveau;
        case 'progress': return 0;
        case 'status': return '';
        default: return '';
      }
    };
  }

  private setupFilters(): void {
    // Search filter
    this.searchControl.valueChanges
      .pipe(
        takeUntil(this._unsubscribeAll),
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.applyFilters();
      });

    // Plan filter
    this.planFilter.valueChanges
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(() => {
        this.applyFilters();
      });

    // Level filter
    this.levelFilter.valueChanges
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(() => {
        this.applyFilters();
      });
  }

  private loadData(): void {
    this.loading = true;

    // Load plans for filter
    this._planService.getPlans()
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(plans => {
        this.plans = plans;
      });

    // Load variables
    this.loadVariables();
  }

  loadVariables(): void {
    const filter: VariableFilter = {
      searchTerm: this.searchControl.value || undefined,
      planId: this.planFilter.value ? parseInt(this.planFilter.value) : undefined,
      niveau: this.levelFilter.value?.length ? this.levelFilter.value : undefined
    };

    this._variableService.getVariables(filter)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (variables) => {
          this.dataSource.data = variables;
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  private applyFilters(): void {
    let filteredData = [...this.dataSource.data];

    // Apply search filter
    const searchTerm = this.searchControl.value?.toLowerCase() || '';
    if (searchTerm) {
      filteredData = filteredData.filter(variable =>
        variable.description.toLowerCase().includes(searchTerm) ||
        variable.responsableNom.toLowerCase().includes(searchTerm) ||
        variable.responsablePrenom.toLowerCase().includes(searchTerm) ||
        variable.planActionNom.toLowerCase().includes(searchTerm)
      );
    }

    // Apply plan filter
    const selectedPlanId = this.planFilter.value;
    if (selectedPlanId) {
      filteredData = filteredData.filter(variable => 
        variable.planActionId === parseInt(selectedPlanId)
      );
    }

    // Apply level filter
    const selectedLevels = this.levelFilter.value || [];
    if (selectedLevels.length > 0) {
      filteredData = filteredData.filter(variable => 
        selectedLevels.includes(variable.niveau)
      );
    }

    this.dataSource.data = filteredData;
  }

  // Actions
  createVariable(): void {
    this._router.navigate(['/variables/create']);
  }

  viewVariable(variable: VariableAction): void {
    this._router.navigate(['/variables/edit', variable.id]);
  }

  editVariable(variable: VariableAction): void {
    this._router.navigate(['/variables', variable.id, 'edit']);
  }

  deleteVariable(variable: VariableAction): void {
    if (confirm(`Are you sure you want to delete "${variable.description}"?`)) {
      this._variableService.deleteVariable(variable.id!).subscribe(() => {
        this.loadVariables();
      });
    }
  }

  updateProgress(variable: VariableAction, progress: number): void {
    this._variableService.updateVariableProgress(variable.id!, progress)
      .subscribe(() => {
        this.loadVariables();
      });
  }

  // Utility methods
  getLevelColor(niveau: number): string {
    const levelOption = this.levelOptions.find(option => option.value === niveau);
    return levelOption?.color || 'bg-gray-100 text-gray-800';
  }

  getLevelLabel(niveau: number): string {
    const levelOption = this.levelOptions.find(option => option.value === niveau);
    return levelOption?.label || `Level ${niveau}`;
  }

  getProgressColor(progress: number): string {
    if (progress >= 80) return 'text-green-600';
    if (progress >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  getStatusColor(status: string): string {
    switch (status?.toLowerCase()) {
      case 'on track': return 'text-green-600 bg-green-100';
      case 'behind schedule': return 'text-red-600 bg-red-100';
      case 'ahead of schedule': return 'text-blue-600 bg-blue-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  }

  getResponsibleName(variable: VariableActionListRequest): string {
    return `${variable.responsablePrenom} ${variable.responsableNom}`;
  }

  canEdit(variable: VariableAction): boolean {
    return !variable.fige && (
      this._authService.isAdmin() || 
      this.currentUser?.id === variable.responsable.id // Use currentUser here
    );
  }

  canDelete(variable: VariableAction): boolean {
    return this._authService.isAdmin() || 
           (this._authService.isCollaborator() && !variable.fige);
  }

  // Selection methods
  toggleSelection(variable: VariableAction): void {
    const index = this.selectedVariables.findIndex(v => v.id === variable.id);
    if (index > -1) {
      this.selectedVariables.splice(index, 1);
    } else {
      this.selectedVariables.push(variable);
    }
  }

  toggleAllSelection(checked: boolean): void {
    if (checked) {
      this.selectedVariables = [...this.dataSource.data];
    } else {
      this.clearSelection();
    }
  }

  isSelected(variable: VariableAction): boolean {
    return this.selectedVariables.some(v => v.id === variable.id);
  }

  clearSelection(): void {
    this.selectedVariables = [];
  }

  // Bulk actions
  bulkUpdateProgress(): void {
    // Implementation for bulk progress update
    console.log('Bulk update progress for:', this.selectedVariables);
  }

  bulkDelete(): void {
    if (confirm(`Delete ${this.selectedVariables.length} selected variables?`)) {
      // Implementation for bulk delete
      this.clearSelection();
      this.loadVariables();
    }
  }

  trackByFn(index: number, item: VariableAction): any {
    return item.id || index;
  }
}