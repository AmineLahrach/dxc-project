import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { map } from 'rxjs/operators';

export interface Exercice {
  id: number;
  annee: number;
  verrouille: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ExerciceService {
  private apiUrl = `${environment.apiUrl}/exercices`;

  constructor(private http: HttpClient) {}

  /**
   * Retrieves all exercises
   * @returns Observable of Exercice array
   */
  getAllExercices(): Observable<Exercice[]> {
    return this.http.get<Exercice[]>(this.apiUrl);
  }

  /**
   * Gets a specific exercise by ID
   * @param id Exercise ID
   * @returns Observable of Exercice
   */
  getExerciceById(id: number): Observable<Exercice> {
    return this.http.get<Exercice>(`${this.apiUrl}/${id}`);
  }

  /**
   * Creates a new exercise
   * @param exercice Exercise data
   * @returns Observable of created Exercice
   */
  createExercice(exercice: Exercice): Observable<Exercice> {
    return this.http.post<Exercice>(this.apiUrl, exercice);
  }

  /**
   * Updates an existing exercise
   * @param exercice Exercise data with ID
   * @returns Observable of updated Exercice
   */
  updateExercice(exercice: Exercice): Observable<Exercice> {
    return this.http.put<Exercice>(`${this.apiUrl}/${exercice.id}`, exercice);
  }

  /**
   * Deletes an exercise
   * @param id Exercise ID to delete
   * @returns Observable of void
   */
  deleteExercice(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Gets all locked exercises (client-side filtering)
   */
  getLockedExercices(): Observable<Exercice[]> {
    return this.getAllExercices().pipe(
      map(exercices => exercices.filter(ex => ex.verrouille))
    );
  }
}