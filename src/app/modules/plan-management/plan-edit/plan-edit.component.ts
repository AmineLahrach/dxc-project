import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { PlanService } from '../plan-service';
import { UserService } from 'app/core/user/user.service';
import { PlanAction, Exercise, ActionPlanStatus, PlanActionCreateRequest } from 'app/models/plan.models';
import { User } from 'app/models/auth.models';
import { MatIcon, MatIconModule } from '@angular/material/icon';
import { MatError, MatFormField, MatHint, MatLabel } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MatOption, MatOptionModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-plan-form',
  imports: [
    CommonModule, RouterModule, MatInputModule, MatButtonModule,
    FormsModule, ReactiveFormsModule, MatSelectModule,
    MatIconModule, MatMenuModule, RouterModule,
    MatFormField, MatError, MatLabel, 
    MatHint, MatDatepickerModule, 
    MatOptionModule, MatCheckboxModule,
    MatInputModule, MatSelectModule,
    MatCheckboxModule
  ],
  templateUrl: './plan-edit.component.html',
  standalone: true
})
export class PlanEditComponent implements OnInit, OnDestroy {
  planForm: FormGroup;
  isEditMode = false;
  planId: number | null = null;
  loading = false;
  
  // Data
  exercises: Exercise[] = [];
  users: User[] = [];
  currentPlan: PlanAction | null = null;

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _formBuilder: FormBuilder,
    private _planService: PlanService,
    private _userService: UserService,
    private _router: Router,
    private _route: ActivatedRoute,
    private _snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadData();
    this.checkRouteParams();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private initializeForm(): void {
    this.planForm = this._formBuilder.group({
      titre: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
      exerciceId: ['', Validators.required],
      dueDate: [''],
      variableActions: this._formBuilder.array([])
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

  private checkRouteParams(): void {
    this._route.params
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(params => {
        if (params['id']) {
          this.planId = +params['id'];
          this.isEditMode = true;
          this.loadPlanData();
        }
      });
  }

  private loadPlanData(): void {
    if (!this.planId) return;

    this.loading = true;
    this._planService.getPlanById(this.planId)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.currentPlan = plan;
          this.populateForm(plan);
          this.loading = false;
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to load plan data', 'Close', { duration: 3000 });
          this._router.navigate(['/plans']);
        }
      });
  }

  private populateForm(plan: any): void {
    this.planForm.patchValue({
      titre: plan.titre,
      exerciceId: plan.exercice && plan.exercice.id ? plan.exercice.id : null,
      dueDate: plan.dueDate,
      description: plan.description,
      // ...other fields
    });
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

    if (this.isEditMode && this.planId) {
      this.updatePlan(formValue);
    } else {
      this.createPlan(formValue);
    }
  }

  private createPlan(formValue: any): void {
    const createRequest: PlanActionCreateRequest = {
      titre: formValue.titre,
      description: formValue.description,
      exercice: { id: Number(formValue.exerciceId) },
      statut: ActionPlanStatus.PLANNING,
      variableActions: formValue.variableActions?.map((va: any) => ({
        description: va.description,
        poids: Number(va.poids),
        niveau: Number(va.niveau),
        responsable: { id: va.responsableId ? Number(va.responsableId) : 0 },
        vaMereId: va.vaMereId ? Number(va.vaMereId) : null
      }))
    };

    this._planService.createPlan(createRequest)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.loading = false;
          this._snackBar.open('Plan created successfully!', 'Close', { duration: 3000 });
          this._router.navigate(['/plans', plan.id]);
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to create plan', 'Close', { duration: 3000 });
        }
      });
  }

  private updatePlan(formValue: any): void {
    const updateData: PlanActionCreateRequest = {
      titre: formValue.titre,
      description: formValue.description,
      statut: this.currentPlan?.statut || ActionPlanStatus.PLANNING,
      exercice: { id: Number(formValue.exerciceId) }, // Send as object
      variableActions: formValue.variableActions?.map((va: any) => ({
        description: va.description,
        poids: Number(va.poids),
        niveau: Number(va.niveau),
        responsable: { id: va.responsableId ? Number(va.responsableId) : 0 },
        vaMereId: va.vaMereId ? Number(va.vaMereId) : null
      }))
    };

    this._planService.updatePlan(this.planId!, updateData)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.loading = false;
          this._snackBar.open('Plan updated successfully!', 'Close', { duration: 3000 });
          this._router.navigate(['/plans', plan.id]);
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to update plan', 'Close', { duration: 3000 });
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
  getUserName(userId: number): string {
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

  // Navigation
  cancel() {
    if (this.isEditMode && this.planId) {
      this._router.navigate(['/plans']);
    } else {
      this._router.navigate(['/plans']);
    }
  }

  saveDraft(): void {
    // Save as draft functionality
    this._snackBar.open('Draft saved', 'Close', { duration: 2000 });
  }
}