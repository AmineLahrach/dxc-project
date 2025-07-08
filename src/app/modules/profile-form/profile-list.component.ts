import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Profile } from 'app/models/business.models';
import { ProfileService } from './profile-service';
import { ProfileFormComponent } from './profile-form-component';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-profile-list',
  imports: [CommonModule, MatIconModule, ReactiveFormsModule,
    MatFormFieldModule, MatTableModule, MatInputModule,
    MatPaginatorModule, RouterModule, MatButtonModule,
    MatIconModule, MatMenuModule
  ],
  templateUrl: './profile-list.component.html'
})
export class ProfileListComponent implements OnInit {
  profiles: Profile[] = [];

  constructor(
    private profileService: ProfileService,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadProfiles();
  }

  loadProfiles(): void {
    this.profileService.getProfiles().subscribe(
      (data: Profile[]) => {
        this.profiles = data;
      },
      error => {
        console.error('Error fetching profiles:', error);
      }
    );
  }

  // Add this method to fix the error
  openProfileForm(): void {
    const dialogRef = this.dialog.open(ProfileFormComponent, {
      width: '400px',
      data: { profile: { nom: '' } }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.profileService.createProfile(result).subscribe(
          () => {
            this.loadProfiles();
          },
          error => {
            console.error('Error creating profile:', error);
          }
        );
      }
    });
  }

  editProfile(profile: Profile): void {
    const dialogRef = this.dialog.open(ProfileFormComponent, {
      width: '400px',
      data: { profile: { ...profile } }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.profileService.updateProfile(profile.id, result).subscribe(
          () => {
            this.loadProfiles();
          },
          error => {
            console.error('Error updating profile:', error);
          }
        );
      }
    });
  }

  deleteProfile(id: number): void {
    if (confirm('Are you sure you want to delete this profile?')) {
      this.profileService.deleteProfile(id).subscribe(
        () => {
          this.loadProfiles();
        },
        error => {
          console.error('Error deleting profile:', error);
        }
      );
    }
  }
}