// FIXED variable-upsert-dialog.component.ts
import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';

// Import your services - adjust paths as needed
import { UserService } from 'app/core/user/user.service';
import { PlanService } from '../plan-management/plan-service';

@Component({
  selector: 'app-variable-upsert-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  template: `
    <div class="max-w-lg">
      <!-- Header -->
      <div class="flex items-center justify-between p-6 border-b">
        <div>
          <h2 class="text-xl font-semibold">
            {{ data.isEdit ? 'Edit Variable' : 'Add Variable' }}
          </h2>
          <p class="text-sm text-secondary mt-1" *ngIf="data.parentVariable">
            Adding child to: {{ data.parentVariable.code }} - {{ data.parentVariable.description }}
          </p>
          <p class="text-sm text-secondary mt-1" *ngIf="data.planActionId && !data.parentVariable">
            Adding root variable to plan ID: {{ data.planActionId }}
          </p>
        </div>
        <button mat-icon-button (click)="onCancel()">
          <mat-icon>close</mat-icon>
        </button>
      </div>

      <!-- Form -->
      <form [formGroup]="variableForm" (ngSubmit)="onSubmit()" class="p-6 space-y-4">
        
        <!-- Description -->
        <mat-form-field class="w-full">
          <mat-label>Description</mat-label>
          <input matInput formControlName="description" placeholder="Enter variable description">
          <mat-error *ngIf="formControls['description'].invalid && formControls['description'].touched">
            Description is required
          </mat-error>
        </mat-form-field>

        <!-- Weight -->
        <mat-form-field class="w-full">
          <mat-label>Weight (%)</mat-label>
          <input matInput type="number" formControlName="poids" 
                 min="0" max="100" step="0.1" placeholder="Weight percentage">
          <mat-hint>Weight as percentage of parent (will be auto-calculated if left empty)</mat-hint>
          <mat-error *ngIf="formControls['poids'].invalid && formControls['poids'].touched">
            Weight must be between 0 and 100
          </mat-error>
        </mat-form-field>

        <!-- Responsible Person -->
        <mat-form-field class="w-full">
          <mat-label>Responsible Person</mat-label>
          <mat-select formControlName="responsableId">
            <mat-option *ngFor="let user of users" [value]="user.id">
              {{ user.prenom }} {{ user.nom }}
            </mat-option>
          </mat-select>
          <mat-error *ngIf="formControls['responsableId'].invalid && formControls['responsableId'].touched">
            Responsible person is required
          </mat-error>
        </mat-form-field>

        <!-- Action Plan (only if no parent) -->
        <mat-form-field class="w-full" *ngIf="!data.parentVariable">
          <mat-label>Action Plan</mat-label>
          <mat-select formControlName="planActionId">
            <mat-option *ngFor="let plan of plans" [value]="plan.id">
              {{ plan.titre }}
            </mat-option>
          </mat-select>
          <mat-error *ngIf="formControls['planActionId'].invalid && formControls['planActionId'].touched">
            Action Plan is required
          </mat-error>
        </mat-form-field>

        <!-- Level Info (read-only) -->
        <div class="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
          <div class="flex items-center space-x-2">
            <span class="text-sm font-medium">Level:</span>
            <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  [class]="getLevelColor(getTargetLevel())">
              Level {{ getTargetLevel() }}
            </span>
          </div>
          <div class="text-xs text-secondary mt-1">
            Code will be auto-generated: {{ getPreviewCode() }}
          </div>
        </div>

        <!-- Fixed checkbox -->
        <div class="flex items-center">
          <mat-checkbox formControlName="fige">
            Fixed Variable
          </mat-checkbox>
          <mat-icon class="ml-2 text-secondary" 
                    matTooltip="Fixed variables cannot be modified or have children added">
            info
          </mat-icon>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-end space-x-3 pt-4">
          <button type="button" mat-stroked-button (click)="onCancel()">
            Cancel
          </button>
          <button type="submit" mat-flat-button color="primary" 
                  [disabled]="variableForm.invalid || loading">
            <mat-spinner diameter="20" *ngIf="loading"></mat-spinner>
            <span [class.ml-2]="loading">
              {{ data.isEdit ? 'Update' : 'Create' }} Variable
            </span>
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }
    .w-full {
      width: 100%;
    }
  `]
})
export class VariableUpsertDialogComponent implements OnInit {
  
  variableForm: FormGroup;
  users: any[] = [];
  plans: any[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<VariableUpsertDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private userService: UserService,
    private planService: PlanService
  ) {
    this.variableForm = this.createForm();
  }

  ngOnInit() {
    console.log('Dialog data:', this.data);
    this.loadUsers();
    if (!this.data.parentVariable) {
      this.loadPlans();
    }
    
    if (this.data.isEdit && this.data.variable) {
      this.populateForm();
    }
  }

  createForm(): FormGroup {
    return this.fb.group({
      description: ['', [Validators.required]],
      poids: [null, [Validators.min(0), Validators.max(100)]],
      responsableId: [null, [Validators.required]],
      planActionId: [this.data.planActionId || null, this.data.parentVariable ? [] : [Validators.required]],
      fige: [false]
    });
  }

  get formControls() {
    return this.variableForm.controls;
  }

  async loadUsers() {
    try {
      this.userService.getUsers().subscribe({
        next: (users) => {
          this.users = users;
          console.log('Users loaded:', users);
        },
        error: (error) => {
          console.error('Error loading users:', error);
        }
      });
    } catch (error) {
      console.error('Error loading users:', error);
    }
  }

  async loadPlans() {
    try {
      this.planService.getAllPlans().subscribe({
        next: (plans) => {
          this.plans = plans;
          console.log('Plans loaded:', plans);
        },
        error: (error) => {
          console.error('Error loading plans:', error);
        }
      });
    } catch (error) {
      console.error('Error loading plans:', error);
    }
  }

  populateForm() {
    const variable = this.data.variable;
    this.variableForm.patchValue({
      description: variable.description,
      poids: variable.poids,
      responsableId: variable.responsableId,
      planActionId: variable.planActionId,
      fige: variable.fige
    });
  }

  getTargetLevel(): number {
    if (this.data.parentVariable) {
      return this.data.parentVariable.niveau + 1;
    }
    return 1;
  }

  getPreviewCode(): string {
    if (this.data.parentVariable) {
      return this.data.parentVariable.code + '1'; // Will be auto-incremented
    }
    return 'VA1'; // Will be auto-incremented
  }

  getLevelColor(niveau: number): string {
    switch (niveau) {
      case 1: return 'text-red-600 bg-red-100';
      case 2: return 'text-yellow-600 bg-yellow-100';
      case 3: return 'text-green-600 bg-green-100';
      case 4: return 'text-blue-600 bg-blue-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSubmit() {
    if (this.variableForm.valid) {
      const formValue = this.variableForm.value;
      
      console.log('Form submitted with values:', formValue);
      
      // Prepare the data
      const variableData = {
        description: formValue.description,
        poids: formValue.poids || 0,
        fige: formValue.fige,
        responsable: formValue.responsableId ? { id: formValue.responsableId } : null,
        planAction: { id: formValue.planActionId || this.data.planActionId },
        vaMere: this.data.parentVariable ? { id: this.data.parentVariable.id } : null
      };

      console.log('Closing dialog with data:', variableData);
      this.dialogRef.close(variableData);
    } else {
      console.log('Form is invalid:', this.variableForm.errors);
      // Mark all fields as touched to show validation errors
      Object.keys(this.formControls).forEach(key => {
        this.formControls[key].markAsTouched();
      });
    }
  }
}