import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { PlanService } from '../plan-service';
import { UserService } from 'app/core/user/user.service';
import { PlanAction, Exercise, ActionPlanStatus, PlanActionCreateRequest } from 'app/models/plan.models';
import { User } from 'app/models/auth.models';
import { MatIconModule } from '@angular/material/icon';
import { MatFormField, MatHint, MatLabel, MatError } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule, MatOptionModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router } from '@angular/router';

@Component({
  selector: 'app-plan-form',
  imports: [
    CommonModule, MatInputModule, MatButtonModule,
    FormsModule, ReactiveFormsModule, MatSelectModule,
    MatIconModule, MatMenuModule, 
    MatFormField, MatError, MatLabel, 
    MatHint, MatDatepickerModule, 
    MatOptionModule, MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './plan-form.component.html',
  standalone: true
})
export class PlanFormComponent implements OnInit, OnChanges, OnDestroy {
  @Input() planId: number;
  @Input() isEditMode: boolean = false;
  @Input() loading: boolean = false;

  @Output() formSubmit = new EventEmitter<any>();
  @Output() formCancel = new EventEmitter<void>();
  @Output() formDraftSave = new EventEmitter<void>();
  @Output() formValidityChange = new EventEmitter<boolean>();

  planForm: FormGroup;
  plan: PlanAction | null = null;
  // Data
  exercises: Exercise[] = [];
  users: User[] = [];
  selectedExerciseYear: string = '';

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _snackBar: MatSnackBar,
    private _router: Router,
    private _formBuilder: FormBuilder,
    private _planService: PlanService,
    private _userService: UserService,
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadData();
    this.planForm.statusChanges.subscribe(status => {
      this.formValidityChange.emit(status === 'VALID');
    });
    
    // Listen for exercise change to update title
    this.planForm.get('exerciceId').valueChanges
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(exerciseId => {
        if (exerciseId) {
          this.updateTitleFromExercise(exerciseId);
        }
      });
    
    // Emit initial form validity
    this.formValidityChange.emit(this.planForm.valid);
  }

  // Update title based on selected exercise
  private updateTitleFromExercise(exerciseId: number): void {
    const selectedExercise = this.exercises.find(ex => ex.id === exerciseId);
    if (selectedExercise) {
      this.selectedExerciseYear = selectedExercise.annee.toString();
      const newTitle = `PA-${this.selectedExerciseYear}`;
      this.planForm.get('titre').setValue(newTitle);
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['planId'] && changes['planId'].currentValue) {
      this.planId = changes['planId'].currentValue;
      this.loadPlanData();
    }
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private initializeForm(): void {
    this.planForm = this._formBuilder.group({
      titre: [{value: '', disabled: true}, [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]],
      verrouille: [false],
      exerciceId: ['', Validators.required],
      statut: [''],
      // dueDate: [''],
      // variableActions: this._formBuilder.array([])
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
    console.log('Loading plan data for ID:', this.planId);
    if (this.planId) {
      this.loadPlanData();
    }
  }

  private loadPlanData(): void {
    if (!this.planId) return;

    this.loading = true;
    this._planService.getPlanById(this.planId)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.populateForm(plan);
          this.plan = plan;
          this.loading = false;
          
          // Store the exercise year
          if (plan.exercice) {
            this.selectedExerciseYear = plan.exercice.annee.toString();
          }
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to load plan data', 'Close', { duration: 3000 });
          this._router.navigate(['/plans']);
        }
      });
  }

  private populateForm(plan: PlanAction): void {
    // Update form with plan data, title is disabled so we use patchValue
    this.planForm.patchValue({
      titre: plan.titre,
      exerciceId: plan.exercice && plan.exercice.id ? plan.exercice.id : null,
      description: plan.description,
      verrouille : plan.verrouille || false,
      statut: plan.statut || ActionPlanStatus.IN_PROGRESS,
    });

    // Reset variable actions array
    // while (this.variableActions.length !== 0) {
    //   this.variableActions.removeAt(0);
    // }

    // // Add variable actions if any
    // if (plan.variableActions && plan.variableActions.length > 0) {
    //   plan.variableActions.forEach(va => {
    //     this.variableActions.push(this.createVariableFormGroup(va));
    //   });
    // }
  }

  onSubmit(): void {
    if (this.planForm.invalid) {
      this.markFormGroupTouched();
      return;
    }

    // Get raw value to include disabled fields
    const formValues = {...this.planForm.getRawValue()};
    this.formSubmit.emit(formValues);
  }

  onCancel(): void {
    this.formCancel.emit();
  }

  onSaveDraft(): void {
    this.formDraftSave.emit();
  }

  // Form validation helpers
  markFormGroupTouched(): void {
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

  getVariableActionMatches(details: string): Array<{id: string, desc: string, index: number}> {
      const regex = /\[id=(\d+), desc="([^\"]+)"\]/g;
      const matches: Array<{id: string, desc: string, index: number}> = [];
      let match;
      while ((match = regex.exec(details)) !== null) {
          matches.push({ id: match[1], desc: match[2], index: match.index });
      }
      return matches;
    }
}