import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { ServiceLine } from 'app/models/business.models';
import { ServiceLineService } from './service-line-service';
import { SharedModule } from '../shared/shared.module';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { ServiceLineFormComponent } from './service-line-form.component';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../shared/confirm-dialog-component/confirm-dialog-component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-service-line-list',
  imports: [SharedModule, ServiceLineFormComponent],
  templateUrl: './service-line-list-component.html',
  standalone: true
})
export class ServiceLineListComponent implements OnInit, AfterViewInit {
  serviceLines: MatTableDataSource<ServiceLine>;
  selectedServiceLine: ServiceLine | null = null;
  isEditing = false;
  displayedColumns: string[] = ['nom', 'actions'];
  
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  logsToShow = 4;

  constructor(
    private serviceLineService: ServiceLineService,
    private dialog: MatDialog,
    private _snackBar: MatSnackBar // <-- Add this
  ) {
    this.serviceLines = new MatTableDataSource<ServiceLine>([]);
  }

  ngOnInit(): void {
    this.loadServiceLines();
  }
  
  ngAfterViewInit(): void {
    if (this.serviceLines) {
      this.serviceLines.sort = this.sort;
      this.serviceLines.paginator = this.paginator;
    }
  }

  loadServiceLines(): void {
    this.serviceLineService.getServiceLines().subscribe(
      (data: ServiceLine[]) => {
        this.serviceLines.data = data;
      },
      (error) => {
        console.error('Error fetching service lines', error);
      }
    );
  }

  createServiceLine(): void {
    this.selectedServiceLine = { nom: ''};
    this.isEditing = false;
  }

  editServiceLine(serviceLine: ServiceLine): void {
    // Create a copy to avoid direct reference modification
    this.selectedServiceLine = { ...serviceLine };
    this.isEditing = true;
  }

  saveServiceLine(serviceLine: ServiceLine): void {
    if (this.isEditing && serviceLine.id) {
      this.serviceLineService.updateServiceLine(serviceLine).subscribe(
        () => {
          this.loadServiceLines();
          this.cancelEdit();
        },
        (error) => {
          console.error('Error updating service line', error);
        }
      );
    } else {
      this.serviceLineService.createServiceLine(serviceLine).subscribe(
        () => {
          this.loadServiceLines();
          this.cancelEdit();
        },
        (error) => {
          console.error('Error creating service line', error);
        }
      );
    }
  }

  deleteServiceLine(serviceLine: ServiceLine): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete Service Line',
        message: `Are you sure you want to delete the service line "${serviceLine.nom}"?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.serviceLineService.deleteServiceLine(serviceLine.id).subscribe({
          next: () => {
            this._snackBar.open('Service line deleted successfully', 'Close', { duration: 3000 });
            this.loadServiceLines();
          },
          error: (error) => {
            this._snackBar.open('Failed to delete service line', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  cancelEdit(): void {
    this.selectedServiceLine = null;
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.serviceLines.filter = filterValue.trim().toLowerCase();

    if (this.serviceLines.paginator) {
      this.serviceLines.paginator.firstPage();
    }
  }

  clearFilter(): void {
    this.serviceLines.filter = '';
    if (this.serviceLines.paginator) {
      this.serviceLines.paginator.firstPage();
    }
  }

  viewAuditLogs(): void {
    this.logsToShow += 4;
  }

  collapseAuditLogs(): void {
    this.logsToShow = 4;
  }
}