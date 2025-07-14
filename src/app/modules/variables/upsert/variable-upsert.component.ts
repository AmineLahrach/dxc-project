import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { VariableService } from '../variable-service';
import { VariableAction } from 'app/models/business.models';
import { SharedModule } from 'app/modules/shared/shared.module';
import { UserService } from 'app/core/user/user.service';
import { User } from 'app/models/auth.models';
import { PlanService } from 'app/modules/plan-management/plan-service';
import { PlanAction } from 'app/models/plan.models';
import { VariableActionCreateRequest } from 'app/models/plan.models'

@Component({
  selector: 'app-variable-upsert',
  imports: [SharedModule, RouterModule],
  templateUrl: './variable-upsert.component.html',
  // styleUrls: ['./variable-upsert.component.scss']
})
export class VariableUpsertComponent implements OnInit {
  variableForm: FormGroup;
  variable: VariableAction;
  isEditMode: boolean = false;
  users: User[] = [];
  plans: PlanAction[] = []; // <-- Add this

  levelOptions = [
    { value: 1, label: 'Level 1 (Primary)' },
    { value: 2, label: 'Level 2 (Secondary)' },
    { value: 3, label: 'Level 3 (Tertiary)' }
  ];

  constructor(
    private fb: FormBuilder,
    private variableService: VariableService,
    private route: ActivatedRoute,
    public router: Router,
    private userService: UserService,
    private planService: PlanService
  ) {
    this.variableForm = this.fb.group({
      description: ['', Validators.required],
      poids: [0, [Validators.required, Validators.min(0)]],
      niveau: ['', Validators.required],
      responsable_id: ['', Validators.required],
      plan_action_id: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.userService.getUsers().subscribe(users => {
      this.users = users;
    });

    this.planService.getPlans().subscribe(plans => {
      this.plans = plans;
    });

    const variableId: number = Number(this.route.snapshot.paramMap.get('id'));
    if (variableId) {
      this.isEditMode = true;
      this.loadVariable(variableId);
    }
  }

  loadVariable(id: number): void {
    this.variableService.getVariableByIdForEdit(id).subscribe(variable => {
      this.variable = variable;
      this.variableForm.patchValue(variable);
    });
  }

  onSubmit(): void {
    if (this.variableForm.valid) {
      const formValue = this.variableForm.value;
      const variableData = {
        description: formValue.description,
        poids: formValue.poids,
        niveau: formValue.niveau,
        responsable: { id: formValue.responsable_id },
        planAction: { id: formValue.plan_action_id }
      };
      if (this.isEditMode) {
        this.variableService.updateVariable(this.variable.id, variableData).subscribe(() => {
          this.router.navigate(['/variables']);
        });
      } else {
        this.variableService.createVariable(variableData).subscribe(() => {
          this.router.navigate(['/variables']);
        });
      }
    }
  }

  get formControls() {
    return this.variableForm.controls;
  }
}