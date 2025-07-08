import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProfileService } from './profile-service';
import { Profile } from '../../models/business.models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile-form',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './profile-form-component.html',
})
export class ProfileFormComponent implements OnInit {
  profileForm: FormGroup;
  isEditMode: boolean = false;
  profileId?: number;

  constructor(
    private fb: FormBuilder, 
    private profileService: ProfileService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.profileForm = this.fb.group({
      nom: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    // Get profile ID from route parameters if it exists
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.profileId = +id;
        this.isEditMode = true;
        this.loadProfile(this.profileId);
      }
    });
  }

  loadProfile(id: number): void {
    this.profileService.getProfileById(id).subscribe(
      (profile: Profile) => {
        this.profileForm.patchValue(profile);
      },
      (error) => {
        console.error('Error loading profile:', error);
      }
    );
  }

  onSubmit(): void {
    if (this.profileForm.valid) {
      const profileData: Profile = this.profileForm.value;
      if (this.isEditMode && this.profileId) {
        this.profileService.updateProfile(this.profileId, profileData).subscribe(
          (updatedProfile) => {
            console.log('Profile updated successfully', updatedProfile);
            this.router.navigate(['/profiles']);
          },
          (error) => {
            console.error('Error updating profile:', error);
          }
        );
      } else {
        this.profileService.createProfile(profileData).subscribe(
          (newProfile) => {
            console.log('Profile created successfully', newProfile);
            this.router.navigate(['/profiles']);
          },
          (error) => {
            console.error('Error creating profile:', error);
          }
        );
      }
    }
  }

  cancel(): void {
    this.router.navigate(['/profiles']);
  }
}