import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';
import { VariableService, VariableFilter } from '../variables/variable-service';
import { PlanService } from 'app/modules/plan-management/plan-service';
import { AuthService } from 'app/core/auth/auth.service';
import { VariableAction } from 'app/models/business.models';
import { PlanAction, VariableActionListRequest } from 'app/models/plan.models';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { User } from 'app/models/auth.models';
import { SharedModule } from 'app/modules/shared/shared.module';
import { NotificationsService } from 'app/layout/common/notifications/notifications.service';
import { Notification } from 'app/layout/common/notifications/notifications.types';

@Component({
  selector: 'app-alerts',
  imports: [SharedModule],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss'
})
export class AlertsComponent implements OnInit, OnDestroy  {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  dataSource: MatTableDataSource<Notification> = new MatTableDataSource();
  displayedColumns: string[] = ['type', 'title', 'content', 'time', 'actions'];
 
  // Data
  notifications: Notification[] = [];

  loading = false;

  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _notificationService: NotificationsService,
    private _authService: AuthService,
    private _router: Router
  ) { }

  ngOnInit(): void {
    this.initializeDataSource();
    this.loadData();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private initializeDataSource(): void {
    
    this.dataSource.sortingDataAccessor = (data: Notification, sortHeaderId: string) => {
      switch (sortHeaderId) {
        case 'date': return data.date;
        default: return '';
      }
    };
  }

  private loadData(): void {
    this.loading = true;
    this._notificationService.getAll(0, 100).subscribe();
    this._notificationService.notifications$
      .subscribe(list => {
        this.dataSource.data = list;
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
          this.loading = false;
      });
    
  }

  // Utility methods
  getTypeColor(type: string): string {
    switch (type?.toLowerCase()) {
      case 'info': return 'text-blue-600 bg-blue-100';
      case 'warning': return 'text-yellow-600 bg-yellow-100';
      case 'success': return 'text-green-600 bg-green-100';
      case 'error': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  }

  getTypeIcon(type: string) : string {
    switch (type?.toLowerCase()) {
      case 'info': return 'heroicons_outline:information-circle';
      case 'warning': return 'heroicons_outline:exclamation-triangle';
      case 'success': return 'heroicons_outline:check-circle';
      case 'error': return 'heroicons_outline:x-circle';
      default: return 'heroicons_outline:bell';
    }
  } 

  markAsRead(notificationId: Number): void {
    this._notificationService.markAsRead(notificationId).subscribe();
    this.dataSource.data = this.dataSource.data.map(notification => {
      if (notification.id === notificationId) {
        return { ...notification, recu: true };
      }
      return notification;
    });
  }

  markAsUnread(notificationId: Number): void {
    this._notificationService.markAsUnread(notificationId).subscribe();
    this.dataSource.data = this.dataSource.data.map(notification => {
      if (notification.id === notificationId) {
        return { ...notification, recu: false };
      }
      return notification;
    });
  }

  markAllAsRead(): void {
      this._notificationService.markAllAsRead().subscribe();
      this.dataSource.data = this.dataSource.data.map(notification => {
        return { ...notification, recu: true };
      });
  }

  trackByFn(index: number, item: VariableAction): any {
    return item.id || index;
  }
}
