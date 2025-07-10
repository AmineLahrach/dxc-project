import { Component, OnInit } from '@angular/core';
import { SharedModule } from 'app/modules/shared/shared.module';
import { MatIcon } from "@angular/material/icon";
import { FuseCardComponent } from "@fuse/components/card";
import { MatTable } from "@angular/material/table";

@Component({
    selector: 'app-collaborator-dashboard',
    template: `
        <div class="flex flex-col w-full">
            <!-- Header -->
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between w-full">
                <div class="text-4xl font-extrabold tracking-tight leading-none">Mes Plans d'Action</div>
                <button mat-raised-button color="primary" (click)="createPlan()">
                    <mat-icon>add</mat-icon>
                    Nouveau Plan
                </button>
            </div>
            
            <!-- Progress Overview -->
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-6 mt-8">
                <fuse-card class="flex flex-col p-6">
                    <div class="text-lg font-medium text-secondary">Plans En Cours</div>
                    <div class="text-3xl font-bold mt-2 text-blue-600">{{myStats.inProgress}}</div>
                </fuse-card>
                
                <fuse-card class="flex flex-col p-6">
                    <div class="text-lg font-medium text-secondary">Plans Terminés</div>
                    <div class="text-3xl font-bold mt-2 text-green-600">{{myStats.completed}}</div>
                </fuse-card>
                
                <fuse-card class="flex flex-col p-6">
                    <div class="text-lg font-medium text-secondary">En Attente</div>
                    <div class="text-3xl font-bold mt-2 text-orange-600">{{myStats.pending}}</div>
                </fuse-card>
            </div>
            
            <!-- My Action Plans -->
            <fuse-card class="mt-8 p-6">
                <div class="text-lg font-semibold mb-4">Mes Plans d'Action</div>
                <mat-table [dataSource]="myPlans" class="mat-elevation-z0">
                    <ng-container matColumnDef="title">
                        <mat-header-cell *matHeaderCellDef>Titre</mat-header-cell>
                        <mat-cell *matCellDef="let plan">{{plan.titre}}</mat-cell>
                    </ng-container>
                    
                    <ng-container matColumnDef="status">
                        <mat-header-cell *matHeaderCellDef>Statut</mat-header-cell>
                        <mat-cell *matCellDef="let plan">
                            <mat-chip [color]="getStatusColor(plan.statut)">{{plan.statut}}</mat-chip>
                        </mat-cell>
                    </ng-container>
                    
                    <ng-container matColumnDef="progress">
                        <mat-header-cell *matHeaderCellDef>Progression</mat-header-cell>
                        <mat-cell *matCellDef="let plan">
                            <mat-progress-bar [value]="plan.progress" mode="determinate"></mat-progress-bar>
                            <span class="ml-2">{{plan.progress}}%</span>
                        </mat-cell>
                    </ng-container>
                    
                    <ng-container matColumnDef="actions">
                        <mat-header-cell *matHeaderCellDef>Actions</mat-header-cell>
                        <mat-cell *matCellDef="let plan">
                            <button mat-icon-button (click)="editPlan(plan)">
                                <mat-icon>edit</mat-icon>
                            </button>
                            <button mat-icon-button (click)="viewDetails(plan)">
                                <mat-icon>visibility</mat-icon>
                            </button>
                        </mat-cell>
                    </ng-container>
                    
                    <mat-header-row *matHeaderRowDef="displayedColumns"></mat-header-row>
                    <mat-row *matRowDef="let row; columns: displayedColumns;"></mat-row>
                </mat-table>
            </fuse-card>
        </div>
    `,
    imports: [SharedModule, FuseCardComponent]
})
export class CollaboratorDashboardComponent implements OnInit {
createPlan() {
throw new Error('Method not implemented.');
}
    myStats = {
        inProgress: 0,
        completed: 0,
        pending: 0
    };
    myPlans: any[] = [];
    displayedColumns = ['title', 'status', 'progress', 'actions'];

    ngOnInit() {
        this.loadMyPlans();
    }
  loadMyPlans() {
    throw new Error('Method not implemented.');
  }
}