import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { VariableAction } from 'app/models/business.models';
import { PlanAction } from 'app/models/plan.models';
import { User } from 'app/models/auth.models';
import { SharedModule } from 'app/modules/shared/shared.module';
import { UserService } from 'app/core/user/user.service';
import { PlanService } from 'app/modules/plan-management/plan-service';
import { VariableService } from '../variable-service';

@Component({
  selector: 'app-variable-form',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './variable-form.component.html'
})
export class VariableFormComponent implements OnInit, OnChanges {
  @Input() variableId: number;
  @Input() planActionId: number;
  @Input() isEditMode: boolean = false;
  @Output() formSubmit = new EventEmitter<any>();
  @Output() formCancel = new EventEmitter<void>();
  @Output() formValidityChange = new EventEmitter<boolean>();

  variableForm: FormGroup;
  users: User[] = [];
  plans: PlanAction[] = [];
  variableActions: VariableAction[] = [];
  auditLogs: any[] = [];
  logsToShow = 4;
  
  levelOptions = [
    { value: 1, label: 'Level 1 (Primary)' },
    { value: 2, label: 'Level 2 (Secondary)' },
    { value: 3, label: 'Level 3 (Tertiary)' }
  ];
  
  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private planService: PlanService,
    private variableService: VariableService
  ) {
    this.createForm();
  }
  
  createForm(): void {
    this.variableForm = this.fb.group({
      description: ['', Validators.required],
      poids: [0, [Validators.min(0)]],
      fige: [false],
      niveau: [''],
      responsable_id: ['', Validators.required],
      plan_action_id: [{ value: '' }, Validators.required],
      va_mere_id: ['']
    });
  }
  
  ngOnInit(): void {
    
    this.userService.getUsers().subscribe(users => {
      this.users = users;
    });

    this.planService.getPlans().subscribe(plans => {
      this.plans = plans;
    });

    if(this.planActionId){
      this.variableForm.get('plan_action_id').setValue(this.planActionId)
      this.variableForm.get('plan_action_id').disable();
    }

    // Add event listener for plan action changes
    this.variableForm.get('plan_action_id').valueChanges.subscribe(planId => {
      if (planId) {
        this.loadVariableActions(planId);
      } else {
        this.variableActions = [];
        this.variableForm.get('va_mere_id').setValue('');
      }
    });
    
    // Subscribe to form status changes to emit validity
    this.variableForm.statusChanges.subscribe(status => {
      this.formValidityChange.emit(status === 'VALID');
    });
    
    // Emit initial form validity
    this.formValidityChange.emit(this.variableForm.valid);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['variableId'] && changes['variableId'].currentValue) {
      const variableId = changes['variableId'].currentValue;
      this.auditLogs = variableId.auditLogs ?? [];
        this.loadVariable(variableId);
      
      if (variableId.planActionId) {
        this.loadVariableActions(variableId.planActionId);
      }
    }
  }

  loadVariable(id: number): void {
    this.variableService.getVariableByIdForEdit(id).subscribe(variable => {   
     this.auditLogs = variable.auditLogs || [];   
      this.variableForm.patchValue({
        description: variable.description,
        poids: variable.poids,
        niveau: variable.niveau,
        fige: variable.fige,
        responsable_id: variable.responsableId,
        plan_action_id: variable.planActionId,
        va_mere_id: variable.vaMereId
      });
    });
  }

  loadVariableActions(planId: number): void {
    this.variableService.getVariableActionsDropdown(planId).subscribe(actions => {
      this.variableActions = actions;
    });
  }

  onSubmit(): void {
    if (this.variableForm.valid) {
      const formValue = this.variableForm.getRawValue();
      const variableData = {
        description: formValue.description,
        poids: formValue.poids,
        fige: formValue.fige,
        niveau: formValue.niveau,
        responsableId: formValue.responsable_id,
        planActionId: formValue.plan_action_id,
        vaMereId: formValue.va_mere_id || null,
      };
      this.formSubmit.emit(variableData);
    }
  }

  onCancel(): void {
    this.formCancel.emit();
  }

  viewAuditLogs() {
    if (this.auditLogs.length > this.logsToShow) {
      this.logsToShow += 4;
    }
  }

  collapseAuditLogs() {
    this.logsToShow = 4;
  }

  get formControls() {
    return this.variableForm.controls;
  }

  // Public method to trigger form submission from parent
  public submitForm(): void {
    this.onSubmit();
  }

  hasFixedVariables(): boolean {
    return this.variableActions.some(varAction => varAction.fige);
  }
}