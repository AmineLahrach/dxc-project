import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { Profile } from 'app/models/business.models';
import { ProfileService } from './profile-service';
import { Router } from '@angular/router';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { MatPaginator } from '@angular/material/paginator';
import { SharedModule } from '../shared/shared.module';
import { ProfileFormComponent } from './profile-form-component';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../shared/confirm-dialog-component/confirm-dialog-component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-profile-list',
  imports: [SharedModule, ProfileFormComponent],
  templateUrl: './profile-list.component.html',
  standalone: true
})
export class ProfileListComponent implements OnInit, AfterViewInit {
  profiles: MatTableDataSource<Profile>;
  selectedProfile: Profile | null = null;
  isEditing = false;
  displayedColumns: string[] = ['nom', 'actions'];
  
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(
    private profileService: ProfileService,
    private router: Router,
    private dialog: MatDialog,
    private _snackBar: MatSnackBar
  ) {
    this.profiles = new MatTableDataSource<Profile>([]);
  }

  ngOnInit(): void {
    this.loadProfiles();
  }
  
  ngAfterViewInit(): void {
    if (this.profiles) {
      this.profiles.sort = this.sort;
      this.profiles.paginator = this.paginator;
    }
  }

  loadProfiles(): void {
    this.profileService.getProfiles().subscribe(
      (data: Profile[]) => {
        this.profiles.data = data;
      },
      error => {
        console.error('Error fetching profiles:', error);
      }
    );
  }

  createProfile(): void {
    this.selectedProfile = { nom: '' };
    this.isEditing = false;
  }

  editProfile(profile: Profile): void {
    this.selectedProfile = { ...profile };
    this.isEditing = true;
  }

  saveProfile(profile: Profile): void {
    if (this.isEditing && profile.id) {
      this.profileService.updateProfile(profile.id, profile).subscribe(
        () => {
          this.loadProfiles();
          this.cancelEdit();
        },
        error => {
          console.error('Error updating profile:', error);
        }
      );
    } else {
      this.profileService.createProfile(profile).subscribe(
        () => {
          this.loadProfiles();
          this.cancelEdit();
        },
        error => {
          console.error('Error creating profile:', error);
        }
      );
    }
  }

  deleteProfile(profile: Profile): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete Profile',
        message: `Are you sure you want to delete the profile "${profile.nom}"?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.profileService.deleteProfile(profile.id).subscribe({
          next: () => {
            this._snackBar.open('Profile deleted successfully', 'Close', { duration: 3000 });
            this.loadProfiles();
          },
          error: (error) => {
            this._snackBar.open('Failed to delete profile', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  cancelEdit(): void {
    this.selectedProfile = null;
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.profiles.filter = filterValue.trim().toLowerCase();

    if (this.profiles.paginator) {
      this.profiles.paginator.firstPage();
    }
  }

  clearFilter(): void {
    this.profiles.filter = '';
    if (this.profiles.paginator) {
      this.profiles.paginator.firstPage();
    }
  }
}