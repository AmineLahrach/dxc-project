import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { FlyoutComponent } from './flyout-component';

@Injectable({
  providedIn: 'root'
})
export class FlyoutService {
  constructor(private dialog: MatDialog) {}

  openFlyout(data: any): MatDialogRef<FlyoutComponent> {
    return this.dialog.open(FlyoutComponent, {
      width: '600px',
      maxWidth: '100vw',
      height: '100vh',
      position: { right: '0' },
      panelClass: 'flyout-dialog',
      data,
      autoFocus: false, // Add this to prevent auto focus issues
      hasBackdrop: true,
      disableClose: false
    });
  }
}