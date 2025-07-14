import { Component, OnInit } from "@angular/core";
import { FuseCardComponent } from "@fuse/components/card";
import { MatIcon } from "@angular/material/icon";
import { SharedModule } from "app/modules/shared/shared.module";
import { DashboardService, DashboardStats } from '../dashboard-service';
import { finalize, forkJoin } from 'rxjs';
import { Router } from '@angular/router';

@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    imports: [SharedModule, FuseCardComponent],
    standalone: true
})
export class AdminDashboardComponent implements OnInit {

    viewAuditLogs() {
        throw new Error('Method not implemented.');
    }
    
    manageProfiles() {
        this.router.navigate(['/profiles']);
    }
    
    createUser() {
        this.router.navigate(['/user-management/create']);
    }
    
    stats: DashboardStats = {
       totalPlans: 0,
         activePlans: 0,
        completedPlans: 0,
        pendingApprovals: 0,
        recentPlans: [],
        plansByStatus: {},
        plansByServiceLine: {},
        monthlyProgress: [],
        userActivity: []
    };
    recentActivities: any[] = [];
    isLoading: boolean = false;
    error: string | null = null;

    constructor(
        private dashboardService: DashboardService,
        private router: Router
    ) {}

    ngOnInit() {
        this.loadDashboardData();
    }

    loadDashboardData() {
        this.isLoading = true;

        forkJoin({
            stats: this.dashboardService.getDashboardStats(),
            // activities: this.dashboardService.getRecentActivities()
        })
        .pipe(
            finalize(() => this.isLoading = false)
        )
        .subscribe({
            next: (data) => {
                this.stats = data.stats;
                // this.recentActivities = data.activities;
            },
            error: (err) => {
                this.error = 'Failed to load data';
                console.error(err);
            }
        });
    }

    navigateToUsers() {
        this.router.navigate(['/user-management']);
    }
}
