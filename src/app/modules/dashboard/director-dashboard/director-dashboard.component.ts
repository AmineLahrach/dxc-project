import { Component, OnInit } from "@angular/core";
import { FuseCardComponent } from "@fuse/components/card";
import { MatTable } from "@angular/material/table";
import { SharedModule } from "app/modules/shared/shared.module";

@Component({
    selector: 'app-director-dashboard',
    template: `
        <div class="flex flex-col w-full">
            <!-- Header -->
            <div class="text-4xl font-extrabold tracking-tight leading-none mb-8">
                Tableau de Bord Directeur
            </div>
            
            <!-- Key Metrics -->
            <div class="grid grid-cols-1 sm:grid-cols-4 gap-6 mb-8">
                <fuse-card class="p-6">
                    <div class="text-lg font-medium text-secondary">Plans à Valider</div>
                    <div class="text-3xl font-bold mt-2 text-orange-600">{{metrics.pendingValidation}}</div>
                </fuse-card>
                
                <fuse-card class="p-6">
                    <div class="text-lg font-medium text-secondary">Taux de Réalisation</div>
                    <div class="text-3xl font-bold mt-2 text-green-600">{{metrics.completionRate}}%</div>
                </fuse-card>
                
                <fuse-card class="p-6">
                    <div class="text-lg font-medium text-secondary">Plans Actifs</div>
                    <div class="text-3xl font-bold mt-2 text-blue-600">{{metrics.activePlans}}</div>
                </fuse-card>
                
                <fuse-card class="p-6">
                    <div class="text-lg font-medium text-secondary">Exercices</div>
                    <div class="text-3xl font-bold mt-2">{{metrics.totalExercises}}</div>
                </fuse-card>
            </div>
            
            <!-- Charts Section -->
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
                <!-- Progress Chart -->
                <fuse-card class="p-6">
                    <div class="text-lg font-semibold mb-4">Progression par Service Line</div>
                    <canvas #progressChart></canvas>
                </fuse-card>
                
                <!-- Status Distribution -->
                <fuse-card class="p-6">
                    <div class="text-lg font-semibold mb-4">Répartition des Statuts</div>
                    <canvas #statusChart></canvas>
                </fuse-card>
            </div>
            
            <!-- Validation Queue -->
            <fuse-card class="p-6">
                <div class="text-lg font-semibold mb-4">Plans en Attente de Validation</div>
                <mat-table [dataSource]="pendingPlans">
                    <ng-container matColumnDef="title">
                        <mat-header-cell *matHeaderCellDef>Titre</mat-header-cell>
                        <mat-cell *matCellDef="let plan">{{plan.titre}}</mat-cell>
                    </ng-container>
                    
                    <ng-container matColumnDef="submitter">
                        <mat-header-cell *matHeaderCellDef>Soumis par</mat-header-cell>
                        <mat-cell *matCellDef="let plan">{{plan.submitter}}</mat-cell>
                    </ng-container>
                    
                    <ng-container matColumnDef="date">
                        <mat-header-cell *matHeaderCellDef>Date</mat-header-cell>
                        <mat-cell *matCellDef="let plan">{{plan.submissionDate | date}}</mat-cell>
                    </ng-container>
                    
                    <ng-container matColumnDef="actions">
                        <mat-header-cell *matHeaderCellDef>Actions</mat-header-cell>
                        <mat-cell *matCellDef="let plan">
                            <button mat-raised-button color="primary" (click)="validatePlan(plan)" class="mr-2">
                                Valider
                            </button>
                            <button mat-stroked-button color="warn" (click)="requestChanges(plan)">
                                Demander Modifications
                            </button>
                        </mat-cell>
                    </ng-container>
                    
                    <mat-header-row *matHeaderRowDef="pendingColumns"></mat-header-row>
                    <mat-row *matRowDef="let row; columns: pendingColumns;"></mat-row>
                </mat-table>
            </fuse-card>
        </div>
    `,
    imports: [SharedModule, FuseCardComponent]
})
export class DirectorDashboardComponent implements OnInit {
    metrics = {
        pendingValidation: 0,
        completionRate: 0,
        activePlans: 0,
        totalExercises: 0
    };
    pendingPlans: any[] = [];
    pendingColumns = ['title', 'submitter', 'date', 'actions'];

    ngOnInit() {
        this.loadDirectorMetrics();
        this.loadPendingValidations();
    }
  loadPendingValidations() {
    throw new Error("Method not implemented.");
  }
  loadDirectorMetrics() {
    throw new Error("Method not implemented.");
  }
}