import { Component, OnInit } from '@angular/core';
import { ServiceLine } from 'app/models/business.models';
import { ServiceLineService } from './service-line-service';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatPaginatorModule } from '@angular/material/paginator';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';

@Component({
  selector: 'app-service-line-list',
  imports: [CommonModule, MatIconModule, ReactiveFormsModule,
      MatFormFieldModule, MatTableModule, MatInputModule,
      MatPaginatorModule, RouterModule, MatButtonModule
    ],
  templateUrl: './service-line-list-component.html'
})
export class ServiceLineListComponent implements OnInit {
  serviceLines: ServiceLine[] = [];
  selectedServiceLine: ServiceLine | null = null;
  isEditing = false;

  constructor(private serviceLineService: ServiceLineService) {}

  ngOnInit(): void {
    this.loadServiceLines();
  }

  loadServiceLines(): void {
    this.serviceLineService.getServiceLines().subscribe(
      (data: ServiceLine[]) => {
        this.serviceLines = data;
      },
      (error) => {
        console.error('Error fetching service lines', error);
      }
    );
  }

  createServiceLine(): void {
    this.selectedServiceLine = { nom: ''};
    this.isEditing = false;
  }

  editServiceLine(serviceLine: ServiceLine): void {
    // Create a copy to avoid direct reference modification
    this.selectedServiceLine = { ...serviceLine };
    this.isEditing = true;
  }

  saveServiceLine(serviceLine: ServiceLine): void {
    if (this.isEditing && serviceLine.id) {
      this.serviceLineService.updateServiceLine(serviceLine).subscribe(
        () => {
          this.loadServiceLines();
          this.cancelEdit();
        },
        (error) => {
          console.error('Error updating service line', error);
        }
      );
    } else {
      this.serviceLineService.createServiceLine(serviceLine).subscribe(
        () => {
          this.loadServiceLines();
          this.cancelEdit();
        },
        (error) => {
          console.error('Error creating service line', error);
        }
      );
    }
  }

  deleteServiceLine(id: number): void {
    if (confirm('Are you sure you want to delete this service line?')) {
      this.serviceLineService.deleteServiceLine(id).subscribe(
        () => {
          this.loadServiceLines();
        },
        (error) => {
          console.error('Error deleting service line', error);
        }
      );
    }
  }

  cancelEdit(): void {
    this.selectedServiceLine = null;
  }
}