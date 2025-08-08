import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { PlanService } from '../plan-service';
import { PlanAction, ActionPlanStatus, PlanActionCreateRequest } from 'app/models/plan.models';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { PlanFormComponent } from '../plan-form/plan-form.component';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from 'app/core/auth/auth.service';

@Component({
  selector: 'app-plan-edit',
  imports: [
    CommonModule, RouterModule, MatButtonModule,
    MatIconModule, PlanFormComponent, MatProgressSpinnerModule
  ],
  templateUrl: './plan-edit.component.html',
  standalone: true
})
export class PlanEditComponent implements OnInit, OnDestroy {
  @ViewChild(PlanFormComponent) planFormComponent: PlanFormComponent;
  
  isEditMode = false;
  planId: number | null = null;
  loading = false;
  currentPlan: PlanAction | null = null;
  isFormValid: boolean = false;
  isDirector: boolean = false;

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _planService: PlanService,
    private _router: Router,
    private _route: ActivatedRoute,
    private _snackBar: MatSnackBar,
    private _authService: AuthService
  ) {
     this.isDirector = this._authService.isDirector();
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
          this.planId = +params['id'];
          this.isEditMode = true;
        }
      });
  }
  
  submitForm(): void {
    if (this.planFormComponent) {
      this.planFormComponent.onSubmit();
    }
  }

  onFormSubmit(formValue: any): void {
    if (!formValue) {
      return;
    }
    
    this.loading = true;

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
      verrouille: formValue.verrouille || false,
      exerciceId: Number(formValue.exerciceId),
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
          this._router.navigate(['/plans']);
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
      exerciceId: Number(formValue.exerciceId),
      verrouille: formValue.verrouille || false,
      variableActions: formValue.variableActions?.map((va: any) => ({
        id: va.id || null,
        description: va.description,
        poids: Number(va.poids),
        niveau: Number(va.niveau),
        responsable: { id: va.responsableId ? Number(va.responsableId) : 0 },
        vaMereId: va.vaMereId ? Number(va.vaMereId) : null,
        plan_action_id: this.planId
      }))
    };

    this._planService.updatePlan(this.planId!, updateData)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (plan) => {
          this.loading = false;
          this._snackBar.open('Plan updated successfully!', 'Close', { duration: 3000 });
          this._router.navigate(['/plans']);
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to update plan', 'Close', { duration: 3000 });
        }
      });
  }

  onFormCancel(): void {
    this._router.navigate(['/plans']);
  }

  saveDraft(): void {
    // Save as draft functionality
    this._snackBar.open('Draft saved', 'Close', { duration: 2000 });
  }

  onFormValidityChange(isValid: boolean): void {
    this.isFormValid = isValid;
  }
}