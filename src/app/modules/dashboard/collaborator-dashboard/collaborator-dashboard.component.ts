import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { DashboardService, DashboardStats } from '../dashboard-service';
import { AuthService } from 'app/core/auth/auth.service';
import { ApexOptions } from 'ng-apexcharts';
import { SharedModule } from 'app/modules/shared/shared.module';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-collaborator-dashboard',
  imports: [CommonModule, SharedModule],
  templateUrl: './collaborator-dashboard.component.html'
})
export class CollaboratorDashboardComponent implements OnInit, OnDestroy {
  stats: DashboardStats;
  loading = true;
  
  // Chart configurations
  chartPlansByStatus: ApexOptions;
  chartPlansByServiceLine: ApexOptions;
  chartMonthlyProgress: ApexOptions;

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _dashboardService: DashboardService,
    private _authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  loadDashboardData(): void {
    this._dashboardService.getDashboardStats()
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (stats) => {
          this.stats = stats;
          this.prepareCharts();
          this.loading = false;
        },
        error: () => {
          this.loading = false;
        }
      });
  }

  private prepareCharts(): void {
    // Plans by Status Pie Chart
    this.chartPlansByStatus = {
      chart: {
        type: 'pie',
        height: 300
      },
      series: Object.values(this.stats.plansByStatus),
      labels: Object.keys(this.stats.plansByStatus),
      colors: ['#2563eb', '#059669', '#dc2626', '#d97706'],
      legend: {
        position: 'bottom'
      },
      responsive: [{
        breakpoint: 480,
        options: {
          chart: {
            height: 250
          },
          legend: {
            position: 'bottom'
          }
        }
      }]
    };

    // Plans by Service Line Bar Chart
    this.chartPlansByServiceLine = {
      chart: {
        type: 'bar',
        height: 300,
        toolbar: { show: false }
      },
      series: [{
        name: 'Plans',
        data: Object.values(this.stats.plansByServiceLine)
      }],
      xaxis: {
        categories: Object.keys(this.stats.plansByServiceLine)
      },
      colors: ['#3b82f6'],
      plotOptions: {
        bar: {
          borderRadius: 4,
          horizontal: false
        }
      },
      dataLabels: {
        enabled: false
      }
    };

    // Monthly Progress Line Chart
    this.chartMonthlyProgress = {
      chart: {
        type: 'line',
        height: 300,
        toolbar: { show: false }
      },
      series: [
        {
          name: 'Completed',
          data: this.stats.monthlyProgress.map(item => item.completed)
        },
        {
          name: 'Created',
          data: this.stats.monthlyProgress.map(item => item.created)
        }
      ],
      xaxis: {
        categories: this.stats.monthlyProgress.map(item => item.month)
      },
      colors: ['#059669', '#3b82f6'],
      stroke: {
        curve: 'smooth',
        width: 3
      },
      markers: {
        size: 6
      }
    };
  }

  get currentUser() {
    return this._authService.currentUser$;
  }

  get isAdmin() {
    return this._authService.isAdmin();
  }

  get isDirector() {
    return this._authService.isDirector();
  }

  getStatusColor(status: string): string {
    switch (status.toUpperCase()) {
      case 'PLANNING':
        return 'text-blue-600 bg-blue-100';
      case 'IN_PROGRESS':
        return 'text-green-600 bg-green-100';
      case 'TRACKING':
        return 'text-orange-600 bg-orange-100';
      case 'COMPLETED':
        return 'text-purple-600 bg-purple-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  }

  getTimeAgo(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));
    
    if (diffInHours < 1) return 'Just now';
    if (diffInHours < 24) return `${diffInHours}h ago`;
    
    const diffInDays = Math.floor(diffInHours / 24);
    return `${diffInDays}d ago`;
  }
}