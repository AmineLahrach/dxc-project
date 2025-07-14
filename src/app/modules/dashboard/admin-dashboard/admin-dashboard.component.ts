import { Component, OnInit } from "@angular/core";
import { FuseCardComponent } from "@fuse/components/card";
import { MatIcon } from "@angular/material/icon";
import { SharedModule } from "app/modules/shared/shared.module";

@Component({
    selector: 'app-admin-dashboard',
    templateUrl: './admin-dashboard.component.html',
    imports: [SharedModule, FuseCardComponent]
})
export class AdminDashboardComponent implements OnInit {

    viewAuditLogs() {
        throw new Error('Method not implemented.');
    }
    manageProfiles() {
        throw new Error('Method not implemented.');
    }
    createUser() {
        throw new Error('Method not implemented.');
    }
    
    stats = {
        totalUsers: 0,
        totalPlans: 0,
        totalServiceLines: 0,
        activeExercises: 0
    };
    recentActivities: any[] = [];

    ngOnInit() {
        this.loadDashboardData();
    }

    loadDashboardData() {
        // Load statistics and recent activities
    }
}
