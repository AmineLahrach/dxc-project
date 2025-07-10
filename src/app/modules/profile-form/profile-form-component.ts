import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Profile } from '../../models/business.models';
import { SharedModule } from '../shared/shared.module';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';


@Component({
  selector: 'app-profile-form',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
  templateUrl: './profile-form-component.html',
  standalone: true
})
export class ProfileFormComponent implements OnInit {
  @Input() profile: Profile = { nom: '' };
  @Output() save = new EventEmitter<Profile>();
  @Output() cancel = new EventEmitter<void>();
  @Output() delete = new EventEmitter<number>();
  
  profileForm: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder, 
    private _snackBar: MatSnackBar
  ) {
    this.profileForm = this.fb.group({
      nom: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    // Initialize form with profile data
    if (this.profile) {
      this.profileForm.patchValue({
        nom: this.profile.nom
      });
    }
  }

  onSubmit(): void {
    if (this.profileForm.valid) {
      this.loading = true;
      const updatedProfile: Profile = {
        ...this.profile,
        ...this.profileForm.value
      };
      
      this.save.emit(updatedProfile);
      
      // Show success message
      this._snackBar.open('Profile saved successfully', 'Close', { 
        duration: 3000
      });
      
      this.loading = false;
    } else {
      this.profileForm.markAllAsTouched();
      this._snackBar.open('Please fix the errors in the form', 'Close', { 
        duration: 3000
      });
    }
  }

  onCancel(): void {
    this.cancel.emit();
  }  
  
  getFieldError(fieldName: string): string {
    const control = this.profileForm.get(fieldName);
    if (control?.errors && control.touched) {
      if (control.errors['required']) return 'This field is required';
    }
    return '';
  }
}