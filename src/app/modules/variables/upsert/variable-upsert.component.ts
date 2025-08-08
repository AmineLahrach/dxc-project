import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { VariableService } from '../variable-service';
import { VariableAction } from 'app/models/business.models';
import { SharedModule } from 'app/modules/shared/shared.module';
import { VariableFormComponent } from '../variable-form/variable-form.component';

@Component({
  selector: 'app-variable-upsert',
  imports: [SharedModule, RouterModule, VariableFormComponent],
  standalone: true,
  templateUrl: './variable-upsert.component.html'
})
export class VariableUpsertComponent implements OnInit {
  variableId: number;
  variable: VariableAction;
  isEditMode: boolean = false;
  isFormValid: boolean = false;
  
  @ViewChild(VariableFormComponent) variableForm: VariableFormComponent;
  
  constructor(
    private variableService: VariableService,
    private route: ActivatedRoute,
    public router: Router
  ) {}
  
  ngOnInit(): void {
    this.variableId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.variableId) {
      this.isEditMode = true;
    }
  }

  // New method to trigger form submission from the header button
  triggerFormSubmit(): void {
    if (this.variableForm) {
      this.variableForm.submitForm();
    }
  }

  onFormSubmit(variableData: any): void {
    if (!variableData) {
      return; // Prevent submitting null data
    }
    
    if (this.isEditMode) {
      this.variableService.updateVariable(this.variableId, variableData).subscribe({
        next: (RE) => {
          this.router.navigate(['/variables']);
        },
        error: (err) => {
          console.error('Error updating variable:', err);
        }
      });
    } else {
      this.variableService.createVariable(variableData).subscribe({
        next: () => {
          this.router.navigate(['/variables']);
        },
        error: (err) => {
          console.error('Error creating variable:', err);
        }
      });
    }
  }

  onFormCancel(): void {
    this.router.navigate(['/variables']);
  }

  onFormValidityChange(isValid: boolean): void {
    this.isFormValid = isValid;
  }
}