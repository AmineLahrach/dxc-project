import { Component, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Exercice, ExerciceService } from './exercice.service';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { ConfirmDialogComponent } from '../shared/confirm-dialog-component/confirm-dialog-component';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatOptionModule } from '@angular/material/core';
import { CommonModule } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-exercice-list',
    imports: [CommonModule, MatInputModule, MatButtonModule,
        MatTableModule, MatPaginator, MatLabel,
        MatSort, FormsModule, ReactiveFormsModule,
        RouterModule, MatIconModule, MatOptionModule,
        MatButtonModule, MatFormFieldModule, MatMenuModule,
        MatIconModule, MatSelectModule,
    ],
  templateUrl: './exercice-list.component.html',
})
export class ExerciceListComponent implements OnInit {
  displayedColumns: string[] = ['select', 'annee', 'verrouille', 'actions'];
  dataSource = new MatTableDataSource<Exercice>([]);
  loading = true;
  searchControl = new FormControl('');
  statusFilter = new FormControl('all');
  selectedExercises: Exercice[] = [];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(
    private exerciceService: ExerciceService,
    private dialog: MatDialog,
    private _router: Router
  ) {}

  ngOnInit(): void {
    this.loadExercises();

    // Setup search filtering
    this.searchControl.valueChanges
      .pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => {
        this.applyFilters();
      });

    // Setup status filtering
    this.statusFilter.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadExercises(): void {
    this.loading = true;
    this.exerciceService.getAllExercices().subscribe({
      next: (exercices) => {
        this.dataSource.data = exercices;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading exercises:', error);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.dataSource.filterPredicate = (data: Exercice, filter: string) => {
      const searchStr = data.annee.toString().toLowerCase();
      const searchMatch = searchStr.includes(filter.toLowerCase());
      
      // Status filter
      let statusMatch = true;
      if (this.statusFilter.value === 'locked') {
        statusMatch = data.verrouille === true;
      } else if (this.statusFilter.value === 'unlocked') {
        statusMatch = data.verrouille === false;
      }
      
      return searchMatch && statusMatch;
    };
    
    this.dataSource.filter = this.searchControl.value || '';
  }

  createExercise(): void {
    this._router.navigate(['/exercices/create']);
  }

  editExercise(exercise: Exercice): void {
    this._router.navigate(['/exercices/edit', exercise.id]);
  }

  toggleLock(exercise: Exercice): void {
    const updatedExercise = {
      ...exercise,
      verrouille: !exercise.verrouille
    };
    
    this.exerciceService.updateExercice(updatedExercise).subscribe({
      next: () => {
        this.loadExercises();
      },
      error: (error) => console.error('Error toggling exercise lock status:', error)
    });
  }

  deleteExercise(exercise: Exercice): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete Exercise',
        message: `Are you sure you want to delete the exercise for year ${exercise.annee}?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.exerciceService.deleteExercice(exercise.id).subscribe({
          next: () => {
            this.loadExercises();
          },
          error: (error) => console.error('Error deleting exercise:', error)
        });
      }
    });
  }

  isSelected(exercise: Exercice): boolean {
    return this.selectedExercises.findIndex(e => e.id === exercise.id) !== -1;
  }

  toggleSelection(exercise: Exercice): void {
    const index = this.selectedExercises.findIndex(e => e.id === exercise.id);
    if (index === -1) {
      this.selectedExercises.push(exercise);
    } else {
      this.selectedExercises.splice(index, 1);
    }
  }

  selectAll(): void {
    this.selectedExercises = [...this.dataSource.data];
  }

  clearSelection(): void {
    this.selectedExercises = [];
  }

  deleteSelected(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete Selected Exercises',
        message: `Are you sure you want to delete ${this.selectedExercises.length} selected exercises?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const deleteObservables = this.selectedExercises.map(exercise => 
          this.exerciceService.deleteExercice(exercise.id)
        );
        
        // Process all delete requests and refresh the list when completed
        let completed = 0;
        deleteObservables.forEach(observable => {
          observable.subscribe({
            next: () => {
              completed++;
              if (completed === deleteObservables.length) {
                this.loadExercises();
                this.clearSelection();
              }
            },
            error: (error) => console.error('Error deleting exercise:', error)
          });
        });
      }
    });
  }
}