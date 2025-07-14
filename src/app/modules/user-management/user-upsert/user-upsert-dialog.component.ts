import { Component, OnInit, Inject, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { Subject, takeUntil, forkJoin } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

// Angular Material Imports
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';

// User Service and Models
import { UserService } from 'app/core/user/user.service';
import { User, SignupRequest } from 'app/models/auth.models';
import { ProfileService } from 'app/modules/profile-form/profile-service';
import { ServiceLineService } from 'app/modules/service-line/service-line-service';
import { Profile } from 'app/models/business.models';
import { ServiceLine } from 'app/models/business.models';

@Component({
  selector: 'app-user-upsert-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatChipsModule
  ],
  templateUrl: './user-upsert-dialog.component.html'
})
export class UserUpsertDialogComponent implements OnInit, OnDestroy {
  userForm: FormGroup;
  loading = false;
  isEditMode: boolean;
  user: any;
  
  // Role options will be fetched from service
  availableRoles = [];
  
  // Service line options will be fetched from API
  serviceLines: ServiceLine[] = [];
  
  // Profile options will be fetched from API
  profiles: Profile[] = [];
  
  // Status options
  statusOptions = [
    { value: 'online', label: 'Online', color: 'bg-green-100 text-green-800' },
    { value: 'away', label: 'Away', color: 'bg-yellow-100 text-yellow-800' },
    { value: 'busy', label: 'Busy', color: 'bg-red-100 text-red-800' },
    { value: 'not-visible', label: 'Invisible', color: 'bg-gray-100 text-gray-800' }
  ];
  
  hidePassword = true;
  private _unsubscribeAll: Subject<any> = new Subject<any>();

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: { isEdit: boolean; user?: User },
    private _dialogRef: MatDialogRef<UserUpsertDialogComponent>,
    private _formBuilder: FormBuilder,
    private _userService: UserService,
    private _profileService: ProfileService,
    private _serviceLineService: ServiceLineService,
    private _snackBar: MatSnackBar
  ) {
    this.isEditMode = data.isEdit;
    this.user = data.user || null;
    
    // Initialize form with default values immediately
    this.userForm = this._formBuilder.group({
      nom: [this.user?.nom || '', [Validators.required, Validators.maxLength(50)]],
      prenom: [this.user?.prenom || '', [Validators.required, Validators.maxLength(50)]],
      email: [this.user?.email || '', [Validators.required, Validators.email]],
      username: [this.user?.username || '', [Validators.required, Validators.minLength(4), Validators.maxLength(20)]],
      motDePasse: ['', this.isEditMode ? [] : [Validators.required, Validators.minLength(6)]],
      // roles: [this.user?.roles || ['USER'], [Validators.required]],
      serviceLineId: [this.user?.serviceLineId ?? null, [Validators.required]],
      profileId: [this.user?.profils ? this.user.profils.map(p => p.id) : [], [Validators.required]],
      actif: [this.user?.actif ?? true],
      status: [this.user?.status || 'online'],
      avatar: [this.user?.avatar || '']
    });
  }

  ngOnInit(): void {
    this.fetchDropdownData();
  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  private fetchDropdownData(): void {
    this.loading = true;
    
    // Use forkJoin to make parallel API calls
    forkJoin({
      serviceLines: this._serviceLineService.getServiceLines(),
      profiles: this._profileService.getProfiles()
    })
    .pipe(takeUntil(this._unsubscribeAll))
    .subscribe({
      next: (result) => {
        this.serviceLines = result.serviceLines;
        this.profiles = result.profiles;

        if (this.user?.serviceLine) {
          const serviceLineId = this.getServiceLineId(this.user.serviceLine);
          this.userForm.get('serviceLineId')?.setValue(serviceLineId);
        }

        // Use setTimeout to defer loading state change and avoid ExpressionChangedAfterItHasBeenCheckedError
        // setTimeout(() => {
          this.loading = false;
        // });
      },
      error: (error) => {
        console.error('Error fetching dropdown data:', error);

        setTimeout(() => {
          this._snackBar.open('Failed to load form data. Please try again.', 'Close', { duration: 3000 });
          // this.loading = false;
        });

        this._dialogRef.close(false);
      }
    });
  }

  private initializeForm(): void {
    this.userForm = this._formBuilder.group({
      nom: [this.user?.nom || '', [Validators.required, Validators.maxLength(50)]],
      prenom: [this.user?.prenom || '', [Validators.required, Validators.maxLength(50)]],
      email: [this.user?.email || '', [Validators.required, Validators.email]],
      username: [this.user?.username || '', [Validators.required, Validators.minLength(4), Validators.maxLength(20)]],
      motDePasse: ['', this.isEditMode ? [] : [Validators.required, Validators.minLength(6)]],
      // roles: [this.user?.roles || ['USER'], [Validators.required]],
      serviceLineId: [this.getServiceLineId(this.user?.serviceLine), [Validators.required]],
      profileId: [[''], [Validators.required]],
      actif: [this.user?.actif ?? true],
      status: [this.user?.status || 'online'],
      avatar: [this.user?.avatar || '']
    });
  }

  private getServiceLineId(serviceLineName: string | undefined): number | null {
    if (!serviceLineName) return null;
    const serviceLine = this.serviceLines.find(sl => sl.nom === serviceLineName);
    return serviceLine ? serviceLine.id : null;
  }

  private getProfileId(profileName: string | undefined): number | null {
    if (!profileName) return null;
    const profile = this.profiles.find(p => p.nom === profileName);
    return profile ? profile.id : null;
  }

  private getProfileIds(profiles: Profile[] | undefined): number[] {
    if (!profiles) return [];
    return profiles.map(p => p.id);
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.markFormGroupTouched(this.userForm);
      return;
    }

    this.loading = true;
    const formValue = this.userForm.value;

    if (this.isEditMode) {
      this.updateUser(formValue);
    } else {
      this.createUser(formValue);
    }
  }

  private createUser(formValue: any): void {
    const signupRequest: SignupRequest = {
      nom: formValue.nom,
      prenom: formValue.prenom,
      email: formValue.email,
      username: formValue.username,
      motDePasse: formValue.motDePasse,
      roles: formValue.profileId ? formValue.profileId : [], // Use profileId for roles
      serviceLine: formValue.serviceLineId,
    };

    this._userService.createUser(signupRequest)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: () => {
          // Use setTimeout to defer UI updates
          setTimeout(() => {
            this._snackBar.open('User created successfully', 'Close', { duration: 3000 });
            this.loading = false;
            this._dialogRef.close(true);
          });
        },
        error: (error) => {
          setTimeout(() => {
            this._snackBar.open(`Failed to create user: ${error.message}`, 'Close', { duration: 3000 });
            this.loading = false;
          });
        }
      });
  }

  private updateUser(formValue: any): void {
    if (!this.user) return;

    const updateData: Partial<User> = {
      nom: formValue.nom,
      prenom: formValue.prenom,
      email: formValue.email,
      username: formValue.username,
      roles: formValue.profileId ? formValue.profileId : [], // Use profileId for roles
      serviceLine: formValue.serviceLineId,
      actif: formValue.actif,
      status: formValue.status,
      avatar: formValue.avatar
    };

    // Only include password if it was changed
    if (formValue.motDePasse) {
      updateData['motDePasse'] = formValue.motDePasse;
    }

    this._userService.updateUser(this.user.id, updateData)
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe({
        next: () => {
          this._snackBar.open('User updated successfully', 'Close', { duration: 3000 });
          this.loading = false;
          this._dialogRef.close(true);
        },
        error: (error) => {
          this._snackBar.open(`Failed to update user: ${error.message}`, 'Close', { duration: 3000 });
          this.loading = false;
        }
      });
  }

  // Helper methods
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  getErrorMessage(fieldName: string): string {
    const control = this.userForm.get(fieldName);
    if (control?.errors && control.touched) {
      if (control.errors['required']) return `${this.getFieldLabel(fieldName)} is required`;
      if (control.errors['email']) return 'Please enter a valid email address';
      if (control.errors['minlength']) return `${this.getFieldLabel(fieldName)} is too short`;
      if (control.errors['maxlength']) return `${this.getFieldLabel(fieldName)} is too long`;
    }
    return '';
  }

  private getFieldLabel(fieldName: string): string {
    switch (fieldName) {
      case 'nom': return 'Last name';
      case 'prenom': return 'First name';
      case 'email': return 'Email';
      case 'username': return 'Username';
      case 'motDePasse': return 'Password';
      case 'roles': return 'Roles';
      case 'serviceLineId': return 'Service Line';
      case 'profileId': return 'Profile';
      default: return fieldName;
    }
  }

  getStatusColor(status: string): string {
    const option = this.statusOptions.find(opt => opt.value === status);
    return option ? option.color : 'bg-gray-100 text-gray-800';
  }

  getRoleChipClass(role: string): string {
    switch (role) {
      case 'ADMIN': return 'bg-red-100 text-red-800';
      case 'MANAGER': return 'bg-blue-100 text-blue-800';
      case 'USER': return 'bg-green-100 text-green-800';
      case 'AUDITOR': return 'bg-purple-100 text-purple-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  }

  cancel(): void {
    this._dialogRef.close(false);
  }

  // Add this method to your component class
  getProfileNameById(profileId: number): string {
    const profile = this.profiles.find(p => p.id === profileId);
    return profile ? profile.nom : 'Unknown';
  }
}