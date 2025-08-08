import { Component, Inject, OnInit, NgModule } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { VariableFormComponent } from '../variables/variable-form/variable-form.component';
import { PlanFormComponent } from '../plan-management/plan-form/plan-form.component';

@Component({
  selector: 'app-flyout',
  standalone: true,
  imports: [
    CommonModule, 
    MatIconModule, 
    MatButtonModule,
    MatDialogModule,
    // Import the components that will be used in the flyout
    VariableFormComponent,
    PlanFormComponent
  ],
  template: `
    <div class="flyout-container h-full flex flex-col">
      <div class="flyout-header flex items-center justify-between p-6 border-b">
        <h2 class="text-xl font-semibold">{{ data.title }}</h2>
        <button mat-icon-button (click)="close()">
          <mat-icon>close</mat-icon>
        </button>
      </div>
      <div class="flyout-content flex-1 overflow-auto p-6">
        <ng-container *ngIf="data.component === variableFormComponentType">
          <app-variable-form
            [variableId]="data.inputs.variableId || null"
            [isEditMode]="data.inputs.isEditMode || false"
            (formSubmit)="onFormSubmit($event)"
            (formCancel)="onFormCancel()">
          </app-variable-form>
        </ng-container>
        
        <ng-container *ngIf="data.component === planFormComponentType">
          <app-plan-form
            [planId]="data.inputs.planId || null"
            [isEditMode]="data.inputs.isEditMode || false"
            [loading]="data.inputs.loading || false"
            (formSubmit)="onFormSubmit($event)"
            (formCancel)="onFormCancel()"
            (formDraftSave)="onFormDraftSave()">
          </app-plan-form>
        </ng-container>
      </div>
    </div>
  `,
  styles: [`
    .flyout-container {
      background-color: var(--bg-card);
      box-shadow: 0 0 20px rgba(0,0,0,0.2);
    }
    .flyout-content {
      height: calc(100vh - 70px);
    }
  `]
})
export class FlyoutComponent implements OnInit {
  // Store component types for comparison
  variableFormComponentType = VariableFormComponent;
  planFormComponentType = PlanFormComponent;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<FlyoutComponent>
  ) {}

  ngOnInit(): void {
    console.log('Flyout initialized with data:', this.data);
  }

  onFormSubmit(result: any): void {
    console.log('Form submitted with result:', result);
    this.dialogRef.close(result);
  }

  onFormCancel(): void {
    this.close();
  }

  onFormDraftSave(): void {
    // Handle draft save if needed
  }

  close(): void {
    this.dialogRef.close();
  }
}