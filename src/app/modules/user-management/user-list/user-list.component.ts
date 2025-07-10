import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { Subject, takeUntil } from 'rxjs';
import { FormControl } from '@angular/forms';

import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { UserService } from 'app/core/user/user.service';
import { User } from 'app/models/auth.models';
import { UserUpsertDialogComponent } from '../user-upsert/user-upsert-dialog.component';
import { SharedModule } from 'app/modules/shared/shared.module';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [SharedModule,
    MatChipsModule,
    MatSnackBarModule,
    MatTooltipModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './user-list.component.html'
})
export class UserListComponent implements OnInit, OnDestroy {
  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  // Data source and columns
  dataSource: MatTableDataSource<User> = new MatTableDataSource<User>();
  displayedColumns: string[] = ['avatar', 'nom', 'prenom', 'email', 'username', 'roles', 'serviceLine', 'actif', 'actions'];
  
  // Filter controls
  searchControl = new FormControl('');
  roleFilter = new FormControl([]);
  activeFilter = new FormControl('all');
  
  // Role options for filtering
  roleOptions = [
    { value: 'ADMIN', label: 'Administrator' },
    { value: 'USER', label: 'User' },
    { value: 'MANAGER', label: 'Manager' },
    { value: 'AUDITOR', label: 'Auditor' }
  ];
  
  loading = false;
  selectedUsers: User[] = [];
  
  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    private _userService: UserService,
    private _router: Router,
    private _dialog: MatDialog,
    private _snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.initializeDataSource();
    this.setupFilters();
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private initializeDataSource(): void {
    this.dataSource.filterPredicate = (data: User, filter: string) => {
      const searchFilter = filter.toLowerCase();
      
      // Apply text search
      const matchesSearch = 
        data.nom.toLowerCase().includes(searchFilter) ||
        data.prenom.toLowerCase().includes(searchFilter) ||
        data.email.toLowerCase().includes(searchFilter) ||
        data.username.toLowerCase().includes(searchFilter) ||
        data.serviceLine.toLowerCase().includes(searchFilter);
      
      // Apply role filter if selected
      const roleValues = this.roleFilter.value || [];
      const matchesRole = roleValues.length === 0 || 
        data.roles.some(role => roleValues.includes(role));
      
      // Apply active filter
      const activeValue = this.activeFilter.value;
      const matchesActive = activeValue === 'all' || 
        (activeValue === 'active' && data.actif) ||
        (activeValue === 'inactive' && !data.actif);
        
      return matchesSearch && matchesRole && matchesActive;
    };

    // Configure sorting
    this.dataSource.sortingDataAccessor = (data: User, sortHeaderId: string) => {
      switch (sortHeaderId) {
        case 'nom': return data.nom.toLowerCase();
        case 'prenom': return data.prenom.toLowerCase();
        case 'email': return data.email.toLowerCase();
        case 'username': return data.username.toLowerCase();
        case 'serviceLine': return data.serviceLine.toLowerCase();
        case 'actif': return data.actif ? 'active' : 'inactive';
        default: return data[sortHeaderId];
      }
    };
  }

  private setupFilters(): void {
    // Search filter
    this.searchControl.valueChanges
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(() => {
        this.applyFilters();
      });
      
    // Role filter
    this.roleFilter.valueChanges
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(() => {
        this.applyFilters();
      });
      
    // Active filter
    this.activeFilter.valueChanges
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(() => {
        this.applyFilters();
      });
  }

  loadUsers(): void {
    this.loading = true;
    this.selectedUsers = [];
    
    this._userService.getUsers()
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: (users) => {
          this.dataSource.data = users;
          this.loading = false;
          
          // Initialize paginator and sort after data is loaded
          setTimeout(() => {
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
          });
        },
        error: () => {
          this.loading = false;
          this._snackBar.open('Failed to load users', 'Close', { duration: 3000 });
        }
      });
  }

  private applyFilters(): void {
    this.dataSource.filter = this.searchControl.value || '';
  }

  // User actions
  createUser(): void {
    const dialogRef = this._dialog.open(UserUpsertDialogComponent, {
      width: '600px',
      maxHeight: '90vh', // Add this line to limit height
      data: { isEdit: false }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  editUser(user: User): void {
    const dialogRef = this._dialog.open(UserUpsertDialogComponent, {
      width: '600px',
      maxHeight: '90vh', // Add this line to limit height
      data: { isEdit: true, user: user }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  toggleUserStatus(user: User): void {
    const newStatus = !user.actif;
    const action = newStatus ? 'activate' : 'deactivate';
    
    if (confirm(`Are you sure you want to ${action} user "${user.prenom} ${user.nom}"?`)) {
      this._userService.updateUserStatus(user.id, newStatus)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: () => {
            user.actif = newStatus;
            this._snackBar.open(`User ${action}d successfully`, 'Close', { duration: 3000 });
          },
          error: () => {
            this._snackBar.open(`Failed to ${action} user`, 'Close', { duration: 3000 });
          }
        });
    }
  }

  deleteUser(user: User): void {
    if (confirm(`Are you sure you want to delete user "${user.prenom} ${user.nom}"? This action cannot be undone.`)) {
      this._userService.deleteUser(user.id)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: () => {
            this._snackBar.open('User deleted successfully', 'Close', { duration: 3000 });
            this.loadUsers();
          },
          error: () => {
            this._snackBar.open('Failed to delete user', 'Close', { duration: 3000 });
          }
        });
    }
  }

  // Bulk actions
  toggleSelection(user: User): void {
    const index = this.selectedUsers.findIndex(u => u.id === user.id);
    if (index === -1) {
      this.selectedUsers.push(user);
    } else {
      this.selectedUsers.splice(index, 1);
    }
  }

  isSelected(user: User): boolean {
    return this.selectedUsers.some(u => u.id === user.id);
  }

  selectAll(): void {
    this.selectedUsers = [...this.dataSource.filteredData];
  }

  clearSelection(): void {
    this.selectedUsers = [];
  }

  deleteSelected(): void {
    if (confirm(`Are you sure you want to delete ${this.selectedUsers.length} users? This action cannot be undone.`)) {
      const userIds = this.selectedUsers.map(user => user.id);
      
      // You'll need to implement a bulk delete method in your service
      this._userService.bulkDeleteUsers(userIds)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: () => {
            this._snackBar.open(`${userIds.length} users deleted successfully`, 'Close', { duration: 3000 });
            this.selectedUsers = [];
            this.loadUsers();
          },
          error: () => {
            this._snackBar.open('Failed to delete users', 'Close', { duration: 3000 });
          }
        });
    }
  }

  // Utility methods
  getRoleChipClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'bg-red-100 text-red-800';
      case 'MANAGER': return 'bg-blue-100 text-blue-800';
      case 'USER': return 'bg-green-100 text-green-800';
      case 'AUDITOR': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  getUserInitials(user: User): string {
    return `${user.prenom.charAt(0)}${user.nom.charAt(0)}`;
  }

  getUserStatusClass(status: boolean): string {
    return status ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800';
  }

  getStatusText(status: boolean): string {
    return status ? 'Active' : 'Inactive';
  }

  trackByFn(index: number, item: User): any {
    return item.id || index;
  }
}