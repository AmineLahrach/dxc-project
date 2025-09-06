import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { ExerciceService, Exercice, AuditLog } from './exercice.service';
import { SharedModule } from '../shared/shared.module';
import { AuthService } from 'app/core/auth/auth.service_bckup';

@Component({
  selector: 'app-exercise-upsert',
  imports: [SharedModule],
  templateUrl: './exercice-upsert.component.html'
})
export class ExerciseUpsertComponent implements OnInit, OnDestroy {
  exerciceForm: FormGroup;
  isSubmitting = false;
  isEditMode = false;
  exerciseId: number | null = null;
  loading = false;
  currentYear = new Date().getFullYear();
  auditLogs: AuditLog[] = [];
  logsToShow = 4;
  isDirector: boolean = false;
  private _unsubscribeAll: Subject<any> = new Subject<any>();
  
  constructor(
    private fb: FormBuilder,
    private _exerciceService: ExerciceService,
    private _router: Router,
    private _route: ActivatedRoute,
    private _snackBar: MatSnackBar,
    private _authService: AuthService
  ) {
    this.isDirector = this._authService.isDirector();
    this.exerciceForm = this.fb.group({
      id: [0],
      annee: [
        this.currentYear, 
        [Validators.required, Validators.min(this.currentYear), Validators.max(this.currentYear + 1)]
      ],
      verrouille: [false]
    });
  }
  
  ngOnInit(): void {
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
          this.exerciseId = +params['id'];
          this.isEditMode = true;
          this.loadExerciceData();
        }
      });
  }
  
  private loadExerciceData(): void {
    if (!this.exerciseId) return;

    this.loading = true;
    this._exerciceService.getExerciceById(this.exerciseId)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (exercice) => {
          this.exerciceForm.patchValue({
            id: exercice.id,
            annee: exercice.annee,
            verrouille: exercice.verrouille
          });
          this.auditLogs = exercice.auditLogs ?? [];
          this.loading = false;
        },
        error: (error) => {
          this.loading = false;
          this._snackBar.open('Failed to load exercise data', 'Close', { duration: 3000 });
          this._router.navigate(['/exercices']);
        }
      });
  }
  
  onSubmit(): void {
    if (this.exerciceForm.invalid) {
      this.exerciceForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const exerciceData = this.exerciceForm.value;

    if (this.isEditMode && this.exerciseId) {
      this._exerciceService.updateExercice(exerciceData)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: (response) => {
            this.loading = false;
            this._snackBar.open('Exercise updated successfully', 'Close', { duration: 3000 });
            this._router.navigate(['/exercises']);
          },
          error: (error) => {
            console.error('Error updating exercice:', error);
            this.loading = false;
            this._snackBar.open('Error updating exercise', 'Close', { duration: 3000 });
          }
        });
    } else {
      // Remove id field when creating a new exercise
      const { id, ...exerciceDataWithoutId } = exerciceData;
      
      this._exerciceService.createExercice(exerciceDataWithoutId)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: (response) => {
            this.loading = false;
            this._snackBar.open('Exercise created successfully', 'Close', { duration: 3000 });
            this._router.navigate(['/exercises']);
          },
          error: (error) => {
            console.error('Error creating exercice:', error);
            this.loading = false;
            this._snackBar.open('Error creating exercise', 'Close', { duration: 3000 });
          }
        });
    }
  }

  getFieldError(fieldName: string): string {
    const control = this.exerciceForm.get(fieldName);
    if (control?.errors && control.touched) {
      if (control.errors['required']) return 'This field is required';
      if (control.errors['min']) return `Minimum value is ${control.errors['min'].min}`;
      if (control.errors['max']) return `Maximum value is ${control.errors['max'].max}`;
    }
    return '';
  }
  
  cancel(): void {
    this._router.navigate(['/exercises']);
  }
  
  viewAuditLogs() {
    if (this.auditLogs.length > this.logsToShow) {
      this.logsToShow += 4;
    }
  }

  collapseAuditLogs() {
    this.logsToShow = 4;
  }
}