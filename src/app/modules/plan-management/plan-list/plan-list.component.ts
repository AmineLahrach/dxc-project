import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { PlanService } from '../plan-service';
import { AuthService } from 'app/core/auth/auth.service';
import { PlanAction, ActionPlanStatus, PlanActionFilter } from 'app/models/plan.models';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatFormField, MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatOptionModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-plan-list',
  imports: [
    CommonModule, RouterModule, MatButtonModule,
    MatFormFieldModule, MatInputModule,
    MatSelectModule, ReactiveFormsModule,
    FormsModule, MatPaginator, MatLabel,
    MatIconModule, MatFormField, MatCheckboxModule,
    MatTableModule, MatMenuModule, MatOptionModule
  ],
  templateUrl: './plan-list.component.html'
})
export class PlanListComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  dataSource: MatTableDataSource<PlanAction> = new MatTableDataSource();
  displayedColumns: string[] = ['titre', 'statut', 'progress', 'dueDate', 'createdBy', 'actions'];
  
  // Filter controls
  searchControl = new FormControl('');
  statusFilter = new FormControl([]);
  
  // Filter options
  statusOptions = [
    { value: ActionPlanStatus.PLANNING, label: 'Planning', color: 'text-blue-600 bg-blue-100' },
    { value: ActionPlanStatus.IN_PROGRESS, label: 'In Progress', color: 'text-green-600 bg-green-100' },
    { value: ActionPlanStatus.TRACKING, label: 'Tracking', color: 'text-orange-600 bg-orange-100' },
    { value: ActionPlanStatus.LOCKED, label: 'Locked', color: 'text-gray-600 bg-gray-100' }
  ];

  loading = false;
  selectedPlans: PlanAction[] = [];

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _planService: PlanService,
    private _authService: AuthService,
    private _router: Router,
    private _dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.initializeDataSource();
    this.setupFilters();
    this.loadPlans();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private initializeDataSource(): void {
    this.dataSource.filterPredicate = (data: PlanAction, filter: string) => {
      const searchTerm = filter.toLowerCase();
      return data.titre.toLowerCase().includes(searchTerm) ||
             data.description.toLowerCase().includes(searchTerm) ||
             data.createdBy?.toLowerCase().includes(searchTerm) || false;
    };

    this.dataSource.sortingDataAccessor = (data: PlanAction, sortHeaderId: string) => {
      switch (sortHeaderId) {
        case 'titre': return data.titre;
        case 'statut': return data.statut;
        case 'progress': return data.progress || 0;
        case 'dueDate': return new Date(data.dueDate || '').getTime();
        case 'createdBy': return data.createdBy || '';
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
      .subscribe(searchTerm => {
        this.applyFilters();
      });

    // Status filter
    this.statusFilter.valueChanges
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(() => {
        this.applyFilters();
      });
  }

  loadPlans(): void {
    this.loading = true;
    
    const filter: PlanActionFilter = {
      searchTerm: this.searchControl.value || undefined,
      status: this.statusFilter.value?.length ? this.statusFilter.value : undefined
    };

    this._planService.getPlans(filter)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plans) => {
          this.dataSource.data = plans;
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
      filteredData = filteredData.filter(plan =>
        plan.titre.toLowerCase().includes(searchTerm) ||
        plan.description.toLowerCase().includes(searchTerm) ||
        plan.createdBy?.toLowerCase().includes(searchTerm)
      );
    }

    // Apply status filter
    const selectedStatuses = this.statusFilter.value || [];
    if (selectedStatuses.length > 0) {
      filteredData = filteredData.filter(plan => selectedStatuses.includes(plan.statut));
    }

    this.dataSource.data = filteredData;
  }

  // Actions
  createPlan(): void {
    this._router.navigate(['/plans/create']);
  }

  viewPlan(plan: PlanAction): void {
    this._router.navigate(['/plans/detail', plan.id]);
  }

  editPlan(plan: PlanAction): void {
    this._router.navigate(['/plans/edit', plan.id]);
  }

  deletePlan(plan: PlanAction): void {
    if (confirm(`Are you sure you want to delete "${plan.titre}"?`)) {
      this._planService.deletePlan(plan.id!).subscribe(() => {
        this.loadPlans();
      });
    }
  }

  duplicatePlan(plan: PlanAction): void {
    const duplicatedPlan = {
      titre: `${plan.titre} (Copy)`,
      description: plan.description,
      exerciceId: plan.exercice.id!
    };

    this._planService.createPlan(duplicatedPlan).subscribe(() => {
      this.loadPlans();
    });
  }

  // Bulk actions
  toggleSelection(plan: PlanAction): void {
    const index = this.selectedPlans.findIndex(p => p.id === plan.id);
    if (index > -1) {
      this.selectedPlans.splice(index, 1);
    } else {
      this.selectedPlans.push(plan);
    }
  }

  isSelected(plan: PlanAction): boolean {
    return this.selectedPlans.some(p => p.id === plan.id);
  }

  selectAll(): void {
    this.selectedPlans = [...this.dataSource.data];
  }

  clearSelection(): void {
    this.selectedPlans = [];
  }

  deleteSelected(): void {
    if (confirm(`Delete ${this.selectedPlans.length} selected plans?`)) {
      // Implementation for bulk delete
      this.clearSelection();
      this.loadPlans();
    }
  }

  // Utility methods
  getStatusColor(status: ActionPlanStatus): string {
    const statusOption = this.statusOptions.find(option => option.value === status);
    return statusOption?.color || 'text-gray-600 bg-gray-100';
  }

  getStatusLabel(status: ActionPlanStatus): string {
    const statusOption = this.statusOptions.find(option => option.value === status);
    return statusOption?.label || status;
  }

  getProgressColor(progress: number): string {
    if (progress >= 80) return 'text-green-600';
    if (progress >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  }

  canEdit(plan: PlanAction): boolean {
    return true;
    return this._authService.isAdmin() || 
           this._authService.isCollaborator() && 
           plan.statut !== ActionPlanStatus.LOCKED;
  }

  canDelete(plan: PlanAction): boolean {
    return this._authService.isAdmin() || 
           (this._authService.isCollaborator() && plan.statut === ActionPlanStatus.PLANNING);
  }

  canApprove(plan: PlanAction): boolean {
    return (this._authService.isAdmin() || this._authService.isDirector()) &&
           plan.statut === ActionPlanStatus.PLANNING;
  }

  approvePlan(plan: PlanAction): void {
    this._planService.updatePlanStatus(plan.id!, ActionPlanStatus.IN_PROGRESS)
      .subscribe(() => {
        this.loadPlans();
      });
  }

  trackByFn(index: number, item: PlanAction): any {
    return item.id || index;
  }
}