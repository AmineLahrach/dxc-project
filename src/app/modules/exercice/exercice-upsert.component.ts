import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { ExerciceService, Exercice } from './exercice.service';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

export interface ExerciseDialogData {
  isEdit: boolean;
  exercise?: {
    id: number;
    annee: number;
    verrouille: boolean;
  };
}

@Component({
  selector: 'app-exercise-upsert',
  standalone: true,
  imports: [
    CommonModule, MatLabel, MatDialogModule,
    FormsModule, ReactiveFormsModule, MatFormFieldModule,
    RouterModule, MatButtonModule,
    MatIconModule, MatInputModule, MatCheckboxModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './exercice-upsert.component.html',
})
export class ExerciseUpsertComponent implements OnInit {
  exerciceForm: FormGroup;
  isSubmitting = false;
  isEditMode = false;
  exerciseId: number | null = null;
  loading = false;
  currentYear = new Date().getFullYear();

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ExerciseUpsertComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ExerciseDialogData,
    private _exerciceService: ExerciceService,
    private _router: Router,
    private _route: ActivatedRoute,
    private _snackBar: MatSnackBar
  ) {}
  
  ngOnInit(): void {
    this.initializeForm();
    this.checkRouteParams();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private initializeForm(): void {
    this.exerciceForm = this.fb.group({
      id: [this.data.exercise?.id || 0],
      annee: [
        this.data.exercise?.annee || this.currentYear, 
        [Validators.required, Validators.min(2000), Validators.max(2100)]
      ],
      verrouille: [this.data.exercise?.verrouille || false]
    });
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
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading exercice:', error);
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
    const exerciceData: Exercice = this.exerciceForm.value;

    if (this.isEditMode && this.exerciseId) {
      this._exerciceService.updateExercice(exerciceData)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: (response) => {
            this.loading = false;
            this._snackBar.open('Exercise updated successfully', 'Close', { duration: 3000 });
            this._router.navigate(['/exercices']);
          },
          error: (error) => {
            console.error('Error updating exercice:', error);
            this.loading = false;
            this._snackBar.open('Error updating exercise', 'Close', { duration: 3000 });
          }
        });
    } else {
      this._exerciceService.createExercice(exerciceData)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: (response) => {
            this.loading = false;
            this._snackBar.open('Exercise created successfully', 'Close', { duration: 3000 });
            this._router.navigate(['/exercices']);
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
    this._router.navigate(['/exercices']);
  }
}