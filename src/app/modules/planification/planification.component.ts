import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { CommonModule } from '@angular/common';
import { MatTreeModule } from '@angular/material/tree';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { takeUntil } from 'rxjs/operators';
import { Subject } from 'rxjs'; // Add this import

// Import your services - adjust paths as needed
import { PlanService } from '../plan-management/plan-service';
import { VariableService } from '../variables/variable-service';
import { VariableFormComponent } from '../variables/variable-form/variable-form.component';
import { FlyoutService } from './flyout-service';
import { PlanFormComponent } from '../plan-management/plan-form/plan-form.component';
import { ActionPlanStatus, PlanActionCreateRequest } from 'app/models/plan.models';
import { AuthService } from 'app/core/auth/auth.service';

interface FoodNode {
  name: string;
  children?: FoodNode[];
}

const EXAMPLE_DATA: FoodNode[] = [
  {
    name: 'Action Plan 1',
    children: [{name: 'Variable 1'}, {name: 'Variable 2'}, {name: 'Variable 3'}],
  },
  {
    name: 'Action Plan 2',
    children: [
      {
        name: 'Variable 1',
        children: [{name: 'Variable 1.1'}, {name: 'Variable 1.2'}],
      },
      {
        name: 'Variable 2',
        children: [{name: 'Variable 2.1'}, {name: 'Variable 2.2'}],
      },
    ],
  },
];

@Component({
  selector: 'app-planification',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTreeModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    // VariableFormComponent,
    // PlanFormComponent
  ],
  templateUrl: './planification.component.html',
  styleUrls: ['./planification.component.scss']
})
export class PlanificationComponent implements OnInit, OnDestroy { // Add OnDestroy
  
  plans: any[] = [];
  loading = false;
  isDirector: boolean = false;
  // Add the missing property
  private _unsubscribeAll: Subject<any> = new Subject<any>();
  
  constructor(
    private planService: PlanService,
    private variableService: VariableService,
    private snackBar: MatSnackBar,
    private flyoutService: FlyoutService,
    private _authService: AuthService
  ) {
    // this.isDirector = this._authService.isDirector();
    this.isDirector = true;
  }

  ngOnInit() {
    this.loadTreeData();
  }
  
  async loadTreeData() {
    this.loading = true;

    this.planService.getTreeData().subscribe({
      next: (data) => {
        this.dataSourceNew = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading tree data:', error);
        this.snackBar.open('Error loading tree data', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }  

  childrenAccessor = (node: FoodNode) => node.children ?? [];

  dataSourceNew = EXAMPLE_DATA;

  hasChildNew = (_: number, node: FoodNode) => !!node.children && node.children.length > 0;

  // Replace the createPlan method
  createPlan(): void {
    const flyoutRef = this.flyoutService.openFlyout({
      title: 'Create Action Plan',
      component: PlanFormComponent,
      inputs: {
        isEditMode: false,
        loading: false
      }
    });

    flyoutRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;

        const createRequest: PlanActionCreateRequest = {
          titre: result.titre,
          description: result.description,
          verrouille: result.verrouille,
          exerciceId: Number(result.exerciceId),
          statut: ActionPlanStatus.IN_PROGRESS,
          variableActions: result.variableActions?.map((va: any) => ({
            description: va.description,
            poids: Number(va.poids),
            niveau: Number(va.niveau),
            responsable: { id: va.responsableId ? Number(va.responsableId) : 0 },
            vaMereId: va.vaMereId ? Number(va.vaMereId) : null
          }))
        };

        this.planService.createPlan(createRequest)
          .pipe(takeUntil(this._unsubscribeAll))
          .subscribe({
            next: () => {
              this.snackBar.open('Plan created successfully!', 'Close', { duration: 3000 });              
              this.loadTreeData(); // Refresh the tree data
              this.loading = false;
            },
            error: () => {
              this.snackBar.open('Failed to create plan', 'Close', { duration: 3000 });
              this.loading = false;
            }
          });
      }
    });
  }

  addChildVariable(currentNode?: any , event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    
    // console.log('Add child variable clicked for:', parentNode);
    
    // if (parentNode && parentNode.fige) {
    //   this.snackBar.open('Cannot add children to fixed variables', 'Close', { duration: 3000 });
    //   return;
    // }

    // if (parentNode && parentNode.niveau >= 4) {
    //   this.snackBar.open('Maximum nesting level reached', 'Close', { duration: 3000 });
    //   return;
    // }

    // Get plan ID - either from parent node or from the clicked plan
    let planId: number;
    let variableId: number;
    if(currentNode && currentNode.nodeType === 'PLAN_ACTION') {   
      planId = currentNode.planActionId;
      variableId = null;
    }
    else{      
        planId = currentNode.planActionId;
        variableId = currentNode.id
    }

    // Use flyout with VariableFormComponent
    const flyoutRef = this.flyoutService.openFlyout({
      title: 'Create Variable',
      component: VariableFormComponent,
      inputs: {
        isEditMode: false,
        variable: {
          niveau: currentNode ? Math.min(currentNode.niveau + 1, 4) : 1,
          planActionId: planId,
          vaMereId: variableId
        }
      }
    });

    flyoutRef.afterClosed().subscribe(result => {
      console.log('Flyout closed with result:', result);
      if (result) {
        this.createVariable(result, currentNode);
      }
    });
  }

  async createVariable(variableData: any, parentNode?: any) {
      try {
          console.log('Creating variable:', variableData);
          
          if (parentNode) {
          variableData.vaMere = { id: parentNode.id };
          }
          
          this.variableService.createVariableAction(variableData).subscribe({
          next: (newVariable) => {
              console.log('Variable created:', newVariable);
              this.loadTreeData();
              this.snackBar.open('Variable created successfully', 'Close', { duration: 3000 });
          },
          error: (error) => {
              console.error('Error creating variable:', error);
              this.snackBar.open('Error creating variable', 'Close', { duration: 3000 });
          }
          });
      } catch (error) {
          console.error('Error in createVariable:', error);
          this.snackBar.open('Error creating variable', 'Close', { duration: 3000 });
      }
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

  getStatusColor(statut: string): string {
    switch (statut?.toLowerCase()) {
      case 'en_cours_planification': return 'text-blue-600 bg-blue-100';
      case 'en_cours': return 'text-green-600 bg-green-100';
      case 'planification': return 'text-orange-600 bg-orange-100';
      case 'suivi_realisation': return 'text-green-600 bg-green-100';
      case 'verrouille': return 'text-gray-600 bg-gray-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  }

  editVariable(node: any, event: Event) {
    event.stopPropagation();
    
    console.log('Edit variable clicked:', node);
    
    // Use flyout with VariableFormComponent
    const flyoutRef = this.flyoutService.openFlyout({
      title: 'Edit Variable',
      component: VariableFormComponent,
      inputs: {
        isEditMode: true,
        variableId:node.id,
        planActionId: node.planActionId,
        vaMereId: node.id
      }
    });

    flyoutRef.afterClosed().subscribe(result => {
      if (result) {
        this.updateVariable(node.id, result);
      }
    });
  }

  async updateVariable(id: number, variableData: any) {
    try {
      this.variableService.updateVariableAction(id, variableData).subscribe({
        next: (updatedVariable) => {
          console.log('Variable updated:', updatedVariable);
          this.loadTreeData();          
          this.snackBar.open('Variable updated successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error updating variable:', error);
          this.snackBar.open('Error updating variable', 'Close', { duration: 3000 });
        }
      });
    } catch (error) {
      console.error('Error in updateVariable:', error);
      this.snackBar.open('Error updating variable', 'Close', { duration: 3000 });
    }
  }

  async deleteVariable(node: any, event: Event) {
    event.stopPropagation();
    
    if (node.children && node.children.length > 0) {
      this.snackBar.open('Cannot delete variable with children', 'Close', { duration: 3000 });
      return;
    }

    if (confirm('Are you sure you want to delete this variable?')) {
      try {
        this.variableService.deleteVariableAction(node.id).subscribe({
          next: () => {
            console.log('Variable deleted:', node.id);
            this.loadTreeData();            
            this.snackBar.open('Variable deleted successfully', 'Close', { duration: 3000 });
          },
          error: (error) => {
            console.error('Error deleting variable:', error);
            this.snackBar.open('Error deleting variable', 'Close', { duration: 3000 });
          }
        });
      } catch (error) {
        console.error('Error in deleteVariable:', error);
        this.snackBar.open('Error deleting variable', 'Close', { duration: 3000 });
      }
    }
  }

  onNodeNameClick(node: any, event: Event): void {
    event.stopPropagation();
    
    if (node.nodeType === 'VARIABLE_ACTION') {
      this.editVariable(node, event);
    } else {
      this.editPlan(node, event);
    }
  }

  editPlan(node: any, event: Event): void {
    event.stopPropagation();
    
    console.log('Edit plan clicked:', node);
    
    const flyoutRef = this.flyoutService.openFlyout({
      title: 'Edit Plan',
      component: PlanFormComponent,
      inputs: {
        isEditMode: true,
        planId: node.id,
        loading: false
      }
    });

    flyoutRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        
        const updateData = {
          titre: result.titre,
          description: result.description,
          verrouille: result.verrouille,
          statut: result.statut || ActionPlanStatus.IN_PROGRESS,
          exerciceId: Number(result.exerciceId)
        };

        this.planService.updatePlan(node.id, updateData)
          .pipe(takeUntil(this._unsubscribeAll))
          .subscribe({
            next: () => {
              this.snackBar.open('Plan updated successfully!', 'Close', { duration: 3000 });
              this.loadTreeData(); // Refresh the tree data
              this.loading = false;
            },
            error: () => {
              this.snackBar.open('Failed to update plan', 'Close', { duration: 3000 });
              this.loading = false;
            }
          });
      }
    });
  }

  // Add ngOnDestroy lifecycle hook to clean up subscriptions
  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }
}