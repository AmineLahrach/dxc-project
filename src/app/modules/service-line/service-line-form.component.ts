import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { ServiceLine } from 'app/models/business.models';
import { FormsModule, NgForm, ReactiveFormsModule } from '@angular/forms';
import { ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatTableModule } from '@angular/material/table';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-service-line-form',
  imports: [CommonModule, MatIconModule, ReactiveFormsModule,
      MatFormFieldModule, MatTableModule, MatInputModule,
      MatPaginatorModule, RouterModule, MatButtonModule,
      FormsModule
    ],
  templateUrl: './service-line-form.component.html'
})
export class ServiceLineFormComponent implements OnInit {
  @Input() serviceLine: ServiceLine = { nom: '', description: '' };
  @Output() save = new EventEmitter<ServiceLine>();
  @Output() cancel = new EventEmitter<void>();
  
  @ViewChild('serviceLineForm') form!: NgForm;

  constructor() {}

  ngOnInit(): void {
    // Initialize the serviceLine if it's not already set
    if (!this.serviceLine) {
      this.serviceLine = { nom: '', description: '' };
    }
  }

  onSubmit(): void {
    if (this.form.valid) {
      this.save.emit(this.serviceLine);
    }
  }

  onCancel(): void {
    this.cancel.emit();
  }
}