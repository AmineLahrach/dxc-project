import { Component, OnInit } from "@angular/core";
import { SharedModule } from "app/modules/shared/shared.module";
import { DashboardService, DashboardStats } from '../dashboard-service';
import { finalize, forkJoin, Subject, takeUntil } from 'rxjs';
import { Router } from '@angular/router';
import { UserService } from "app/core/user/user.service";
import { User } from "app/core/user/user.types";

@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    imports: [SharedModule],
    standalone: true
})
export class AdminDashboardComponent implements OnInit {
    logsToShow = 3;
    user: User;

    viewAuditLogs() {
        if (this.stats.recentAudits?.length > this.logsToShow) {
            this.logsToShow += 4;
        }
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
        userActivity: [],
        recentAudits: [] 
    };

    recentActivities: any[] = [];
    isLoading: boolean = false;
    error: string | null = null;
    
    private _unsubscribeAll: Subject<any> = new Subject<any>();
    constructor(
        private _userService: UserService,
        private dashboardService: DashboardService,
        private router: Router
    ) {}
    
    ngOnInit() {
        this._userService.user$
                    .pipe(takeUntil(this._unsubscribeAll))
                    .subscribe((user: User) => {
                        this.user = user;
                    });
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

    collapseAuditLogs() {
        this.logsToShow = 3;
    }
}
