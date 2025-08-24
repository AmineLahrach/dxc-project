import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, takeUntil } from 'rxjs';
import { SharedModule } from '../shared/shared.module';
import { AuthService } from 'app/core/auth/auth.service';
import { UserService } from 'app/core/user/user.service';

export function matchNewPasswordValidator(newPasswordControlName: string): ValidatorFn {
  return (control: AbstractControl) => {
    if (!control.parent) return null; // avoid errors before form init

    const newPassword = control.parent.get(newPasswordControlName)?.value;
    const confirmPassword = control.value;

    if (!confirmPassword) {
      return null; // let Validators.required handle emptiness
    }

    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  };
}

@Component({
  selector: 'app-account',
  imports: [SharedModule],
  templateUrl: './account.component.html',
  styleUrl: './account.component.scss'
})
export class AccountComponent {
  userForm: FormGroup;
  isSubmitting = false;
  loading = false;
  currentUser: any;
  private _unsubscribeAll: Subject<any> = new Subject<any>();
  showPasswordUpdate = false;

  constructor(
    private fb: FormBuilder,
    private _userService: UserService,
    private _router: Router,
    private _snackBar: MatSnackBar,
    private _authService: AuthService,
    private cd: ChangeDetectorRef
  ) {
    this.currentUser = this._authService.getCurrentUserFromStorage();
    
    this.userForm = this.fb.group({
      email: [this.currentUser?.email || '', [Validators.required, Validators.email]],
      username: [this.currentUser?.username || '', [Validators.required, Validators.minLength(4), Validators.maxLength(20)]],
      currentPassword: [{ value: '********', disabled: true }],
      newPassword: [''],
      confirmPassword: ['']
    });
  }

  ngOnInit(): void {

  }

  ngOnDestroy(): void {
    this._unsubscribeAll.next(null);
    this._unsubscribeAll.complete();
  }

  onSubmit(): void {

    if (this.userForm.invalid) {
      this.userForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const userData = this.userForm.value;
    if (userData.currentPassword) {
      userData.motDePasse = userData.currentPassword;
    }

    if (this.currentUser) {
      this._userService.updateUserProfile(this.currentUser.id, userData)
        .pipe(takeUntil(this._unsubscribeAll))
        .subscribe({
          next: (response) => {
            this.loading = false;
            this._snackBar.open('Profile updated successfully', 'Close', { duration: 3000 });
            this.resetValues();
            this.currentUser.email = response.email;
            this.currentUser.username = response.username;
            this._authService.setCurrentUserInStorage(this.currentUser);
            if(response.refreshToken) {
              this._authService.setTokenInStorage(response.refreshToken);
            }
            this._router.navigate(['/account-settings']);
          },
          error: (error) => {
            console.error('Error updating profile:', error);
            this.loading = false;
            this._snackBar.open('Error updating profile', 'Close', { duration: 3000 });
          }
        });
    }
  }

  getFieldError(fieldName: string): string {
    const control = this.userForm.get(fieldName);
    if (control?.errors && control.touched) {
      if (control.errors['required']) return 'This field is required';
      if (control.errors['min']) return `Minimum value is ${control.errors['min'].min}`;
      if (control.errors['max']) return `Maximum value is ${control.errors['max'].max}`;
    }
    return '';
  }

resetValues(): void {
  this.showPasswordUpdate = false;
            this.userForm.get('newPassword')?.patchValue('');
            this.userForm.get('confirmPassword')?.patchValue('');
}

  getErrorMessage(fieldName: string): string {
    const control = this.userForm.get(fieldName);
    
    if (control?.errors && control.touched) {
      if (control.errors['required']) return `${this.getFieldLabel(fieldName)} is required`;
      if (control.errors['email']) return 'Please enter a valid email address';
      if (control.errors['minlength']) return `${this.getFieldLabel(fieldName)} is too short`;
      if (control.errors['maxlength']) return `${this.getFieldLabel(fieldName)} is too long`;
      if (control.errors['passwordMismatch']) return 'New password and current password must match';
    }

    return '';
  }

  private getFieldLabel(fieldName: string): string {
    switch (fieldName) {
      case 'currentPassword': return 'Current Password';
      case 'newPassword': return 'New Password';
      case 'confirmPassword': return 'Confirm Password';
      default: return fieldName;
    }
  }

  cancel(): void {
    this._router.navigate(['/account-settings']);
  }



  togglePasswordUpdate() {
    this.showPasswordUpdate = !this.showPasswordUpdate;
    if (this.showPasswordUpdate) {
      

      this.userForm.get('newPassword')?.enable();
        this.userForm.get('newPassword')?.setValidators([Validators.required]);
        this.userForm.get('newPassword')?.updateValueAndValidity();

        this.userForm.get('confirmPassword')?.enable();
        this.userForm.get('confirmPassword')?.setValidators([Validators.required, matchNewPasswordValidator('newPassword')]);
        this.userForm.get('confirmPassword')?.updateValueAndValidity();

        this.userForm.get('currentPassword')?.patchValue('');
        this.userForm.get('currentPassword')?.enable();
        this.userForm.get('currentPassword')?.setValidators([Validators.required]);
        this.userForm.get('currentPassword')?.updateValueAndValidity();
        

        this.cd.detectChanges();

      
    } else {
      this.userForm.get('newPassword')?.disable();

      this.userForm.get('confirmPassword')?.disable();

      this.userForm.get('currentPassword')?.patchValue('*******');
      this.userForm.get('currentPassword')?.disable();
    }
    this.userForm.updateValueAndValidity();

  }
}
