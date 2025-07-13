import { CommonModule } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { PlanService } from '../plan-service';
import { UserService } from 'app/core/user/user.service';
import { PlanAction, Exercise, ActionPlanStatus, PlanActionCreateRequest } from 'app/models/plan.models';
import { User } from 'app/models/auth.models';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MatOptionModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatChipsModule } from '@angular/material/chips';

@Component({
  selector: 'app-plan-detail',
  imports: [
    CommonModule, 
    RouterModule, 
    MatInputModule, 
    MatButtonModule,
    FormsModule, 
    ReactiveFormsModule, 
    MatSelectModule,
    MatIconModule, 
    MatMenuModule, 
    MatFormFieldModule,
    MatDatepickerModule, 
    MatNativeDateModule,
    MatOptionModule, 
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatCardModule,
    MatDividerModule,
    MatTooltipModule,
    MatChipsModule
  ],
  templateUrl: './plan-detail.component.html',
  standalone: true
})
export class PlanDetailComponent implements OnInit, OnDestroy {
  ActionPlanStatus = ActionPlanStatus;

  // View mode properties
  plan: PlanAction | null = null;
  loading = false;
  isEditMode = false;
  
  // Edit mode properties
  planForm: FormGroup;
  exercises: Exercise[] = [];
  users: User[] = [];

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _formBuilder: FormBuilder,
    private _planService: PlanService,
    private _userService: UserService,
    private _router: Router,
    private _route: ActivatedRoute,
    private _snackBar: MatSnackBar
  ) {
    // Initialize the form
    this.initializeForm();
  }

  ngOnInit(): void {
    // Load data and check route params
    this.loadData();
    this.checkRouteParams();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private checkRouteParams(): void {
    this._route.params
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(params => {
        if (params['id']) {
          const planId = +params['id'];
          this.loadPlanData(planId);
        }
      });
  }

  private loadPlanData(planId: number): void {
    this.loading = true;
    this._planService.getPlanById(planId)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.plan = plan;
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to load plan data', 'Close', { duration: 3000 });
          this._router.navigate(['/plans']);
        }
      });
  }

  private loadData(): void {
    // Load exercises
    this._planService.getExercises()
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(exercises => {
        this.exercises = exercises;
      });

    // Load users for variable assignment
    this._userService.getUsers()
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(users => {
        this.users = users;
      });
  }

  // Toggle between view and edit modes
  toggleEditMode(): void {
    if (!this.isEditMode && this.plan) {
      // Entering edit mode - populate form with current data
      this.populateForm(this.plan);
    }
    this.isEditMode = !this.isEditMode;
  }

  // Form methods
  private initializeForm(): void {
    this.planForm = this._formBuilder.group({
      titre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
      exerciceId: ['', Validators.required],
      dueDate: [''],
      variableActions: this._formBuilder.array([])
    });
  }

  private populateForm(plan: PlanAction): void {
    this.planForm.patchValue({
      titre: plan.titre,
      description: plan.description,
      exerciceId: plan.exercice && plan.exercice.id ? plan.exercice.id : null, // Safe check
      dueDate: plan.dueDate
    });

    // Populate variable actions
    const variableActionsArray = this.planForm.get('variableActions') as FormArray;
    variableActionsArray.clear();
    
    if (plan.variableActions) {
      plan.variableActions.forEach(variable => {
        variableActionsArray.push(this.createVariableFormGroup(variable));
      });
    }
  }

  // Variable Actions Management
  get variableActions(): FormArray {
    return this.planForm.get('variableActions') as FormArray;
  }

  private createVariableFormGroup(variable?: any): FormGroup {
    return this._formBuilder.group({
      id: [variable?.id || null],
      description: [variable?.description || '', [Validators.required, Validators.minLength(5)]],
      poids: [variable?.poids || 0, [Validators.required, Validators.min(0), Validators.max(1)]],
      niveau: [variable?.niveau || 1, [Validators.required, Validators.min(1)]],
      responsableId: [variable?.responsable?.id || '', Validators.required],
      vaMereId: [variable?.vaMere?.id || null],
      fige: [variable?.fige || false]
    });
  }

  addVariableAction(): void {
    this.variableActions.push(this.createVariableFormGroup());
  }

  removeVariableAction(index: number): void {
    this.variableActions.removeAt(index);
  }

  // Form submission
  onSubmit(): void {
    if (this.planForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    this.loading = true;
    const formValue = this.planForm.value;

    if (this.plan?.id) {
      this.updatePlan(formValue);
    } else {
      this.createPlan(formValue);
    }
  }

  private updatePlan(formValue: any): void {
   const updateData: PlanActionCreateRequest = {
      titre: formValue.titre,
      description: formValue.description,
      statut: this.plan?.statut || ActionPlanStatus.PLANNING,
      exercice: { id: Number(formValue.exerciceId) }, // Send as object
      variableActions: formValue.variableActions?.map((va: any) => ({
        description: va.description,
        poids: Number(va.poids),
        niveau: Number(va.niveau),
        responsable: { id: va.responsableId ? Number(va.responsableId) : 0 },
        vaMereId: va.vaMereId ? Number(va.vaMereId) : null
      }))
    };

    this._planService.updatePlan(this.plan!.id!, updateData)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.plan = plan;
          this.loading = false;
          this._snackBar.open('Plan updated successfully!', 'Close', { duration: 3000 });
          this.isEditMode = false;
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to update plan', 'Close', { duration: 3000 });
        }
      });
  }

  private createPlan(formValue: any): void {
    const createRequest: PlanActionCreateRequest = {
      titre: formValue.titre,
      description: formValue.description,
      exercice: { id: Number(formValue.exerciceId) },
      statut: ActionPlanStatus.PLANNING,
      variableActions: formValue.variableActions?.map((va: any) => ({
        description: va.description,
        poids: va.poids,
        niveau: va.niveau,
        responsableId: va.responsableId,
        vaMereId: va.vaMereId
      }))
    };

    this._planService.createPlan(createRequest)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.plan = plan;
          this.loading = false;
          this._snackBar.open('Plan created successfully!', 'Close', { duration: 3000 });
          this.isEditMode = false;
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to create plan', 'Close', { duration: 3000 });
        }
      });
  }

  // Form validation helpers
  private markFormGroupTouched(): void {
    Object.keys(this.planForm.controls).forEach(key => {
      const control = this.planForm.get(key);
      control?.markAsTouched();

      if (control instanceof FormArray) {
        control.controls.forEach(groupControl => {
          if (groupControl instanceof FormGroup) {
            Object.keys(groupControl.controls).forEach(nestedKey => {
              groupControl.get(nestedKey)?.markAsTouched();
            });
          }
        });
      }
    });
  }

  getFieldError(fieldName: string): string {
    const control = this.planForm.get(fieldName);
    if (control?.errors && control.touched) {
      if (control.errors['required']) return `${fieldName} is required`;
      if (control.errors['minlength']) return `${fieldName} is too short`;
      if (control.errors['maxlength']) return `${fieldName} is too long`;
      if (control.errors['min']) return `Value too low`;
      if (control.errors['max']) return `Value too high`;
    }
    return '';
  }

  getVariableFieldError(index: number, fieldName: string): string {
    const control = this.variableActions.at(index).get(fieldName);
    if (control?.errors && control.touched) {
      if (control.errors['required']) return `${fieldName} is required`;
      if (control.errors['minlength']) return `${fieldName} is too short`;
      if (control.errors['min']) return `Value too low`;
      if (control.errors['max']) return `Value too high`;
    }
    return '';
  }

  // Utility methods
  getUserName(userId: number | string): string {
    const user = this.users.find(u => u.id === userId.toString());
    return user ? `${user.prenom} ${user.nom}` : 'Unknown User';
  }

  getTotalWeight(): number {
    return this.variableActions.controls.reduce((total, control) => {
      return total + (control.get('poids')?.value || 0);
    }, 0);
  }

  isWeightValid(): boolean {
    const total = this.getTotalWeight();
    return Math.abs(total - 1.0) < 0.01; // Allow small floating point differences
  }

  getProgressColor(progress: number): string {
    if (progress >= 80) return 'text-green-600';
    if (progress >= 50) return 'text-yellow-600';
    return 'text-red-600';
  }

  getStatusColor(status: ActionPlanStatus): string {
    switch (status) {
      case ActionPlanStatus.PLANNING: return 'text-blue-600 bg-blue-100';
      case ActionPlanStatus.IN_PROGRESS: return 'text-green-600 bg-green-100';
      case ActionPlanStatus.TRACKING: return 'text-orange-600 bg-orange-100';
      case ActionPlanStatus.LOCKED: return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  }

  getStatusLabel(status: ActionPlanStatus): string {
    switch (status) {
      case ActionPlanStatus.PLANNING: return 'Planning';
      case ActionPlanStatus.IN_PROGRESS: return 'In Progress';
      case ActionPlanStatus.TRACKING: return 'Tracking';
      case ActionPlanStatus.LOCKED: return 'Locked';
      default: return status;
    }
  }

  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  }

  // Navigation methods
  goBack(): void {
    this._router.navigate(['/plans']);
  }

  cancelEdit(): void {
    this.isEditMode = false;
    // Reload plan data to refresh any changes
    if (this.plan?.id) {
      this.loadPlanData(this.plan.id);
    }
  }

  // Plan actions
  deletePlan(): void {
    if (!this.plan?.id) return;

    if (confirm(`Are you sure you want to delete "${this.plan.titre}"?`)) {
      this._planService.deletePlan(this.plan.id)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: () => {
            this._snackBar.open('Plan deleted successfully', 'Close', { duration: 3000 });
            this._router.navigate(['/plans']);
          },
          error: () => {
            this._snackBar.open('Failed to delete plan', 'Close', { duration: 3000 });
          }
        });
    }
  }

  // Update plan status
  updateStatus(status: ActionPlanStatus): void {
    if (!this.plan?.id) return;

    this._planService.updatePlanStatus(this.plan.id, status)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.plan = plan;
          this._snackBar.open(`Plan status updated to ${this.getStatusLabel(status)}`, 'Close', { duration: 3000 });
        },
        error: () => {
          this._snackBar.open('Failed to update plan status', 'Close', { duration: 3000 });
        }
      });
  }
}
