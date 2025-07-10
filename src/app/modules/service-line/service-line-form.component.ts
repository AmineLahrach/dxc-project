import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ServiceLine } from 'app/models/business.models';
import { FormsModule, NgForm, ReactiveFormsModule } from '@angular/forms';
import { ViewChild } from '@angular/core';
import { SharedModule } from '../shared/shared.module';
import { MatError, MatHint } from '@angular/material/form-field';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from 'app/modules/shared/confirm-dialog-component/confirm-dialog-component';

@Component({
  selector: 'app-service-line-form',
  imports: [SharedModule, MatHint, MatError], 
  templateUrl: './service-line-form.component.html',
  standalone: true
})
export class ServiceLineFormComponent implements OnInit {
  @Input() serviceLine: ServiceLine = { nom: ''};
  @Output() save = new EventEmitter<ServiceLine>();
  @Output() cancel = new EventEmitter<void>();
  @Output() delete = new EventEmitter<number>();
  
  @ViewChild('serviceLineForm') form!: NgForm;
  loading = false;

  constructor(
    private _snackBar: MatSnackBar,
    private _dialog: MatDialog
  ) {}

  ngOnInit(): void {
    // Initialize the serviceLine if it's not already set
    if (!this.serviceLine) {
      this.serviceLine = { nom: '' };
    }
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.loading = true;
      this.save.emit(this.serviceLine);
      
      // Show success message
      this._snackBar.open('Service line saved successfully', 'Close', { 
        duration: 3000
      });
      
      this.loading = false;
    } else {
      this._snackBar.open('Please fill in all required fields', 'Close', { 
        duration: 3000
      });
    }
  }

  onCancel(): void {
    this.cancel.emit();
  }
  
  onDelete(): void {
    if (!this.serviceLine.id) {
      return;
    }
    
    const dialogRef = this._dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Confirm Deletion',
        message: 'Are you sure you want to delete this service line? This action cannot be undone.',
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === true) {
        this.loading = true;
        this.delete.emit(this.serviceLine.id);
        
        // Notification will typically be shown by the parent component after delete completes
        this._snackBar.open('Deleting service line...', '', { 
          duration: 3000
        });
      }
    });
  }
}