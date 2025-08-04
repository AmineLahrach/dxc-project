import { Component, OnInit } from "@angular/core";
import { DashboardService, DirectorDashboardStats } from "../dashboard-service";
import { SharedModule } from "app/modules/shared/shared.module";
import { User } from "app/models/auth.models";
import { Subject, takeUntil } from "rxjs";
import { UserService } from "app/core/user/user.service";

@Component({
    selector: 'app-director-dashboard',
    templateUrl: './director-dashboard.component.html',
    imports: [SharedModule]
})
export class DirectorDashboardComponent implements OnInit {
    stats: DirectorDashboardStats;
    loading = true;
    logsToShow = 4;
    user: User;

     private _unsubscribeAll: Subject<any> = new Subject<any>();
    
      constructor(
        private _userService: UserService,
        private dashboardService: DashboardService) {}

    ngOnInit() {
        this._userService.user$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((user: User) => {
                this.user = user;
            });
        this.dashboardService.getDashboardStatsForDirector().subscribe({
            next: (stats) => {
                this.stats = stats;
                this.loading = false;
            },
            error: () => {
                this.loading = false;
            }
        });
    }

    viewAuditLogs() {
        if (this.stats.auditLogs?.length > this.logsToShow) {
            this.logsToShow += 4;
        }
    }

    collapseAuditLogs() {
        this.logsToShow = 4;
    }
}