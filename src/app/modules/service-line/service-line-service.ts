import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ServiceLine } from "app/models/business.models";
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ServiceLineService {
  private apiUrl = `${environment.apiUrl}/servicelines`;

  constructor(private http: HttpClient) {}

  getServiceLines(): Observable<ServiceLine[]> {
    return this.http.get<ServiceLine[]>(`${this.apiUrl}/with-audits`)
      .pipe(catchError(this.handleError));
  }

  getServiceLineById(id: number): Observable<ServiceLine | undefined> {
    return this.http.get<ServiceLine>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  createServiceLine(serviceLine: ServiceLine): Observable<ServiceLine> {
    return this.http.post<ServiceLine>(this.apiUrl, serviceLine)
      .pipe(catchError(this.handleError));
  }

  updateServiceLine(updatedServiceLine: ServiceLine): Observable<ServiceLine> {
    return this.http.put<ServiceLine>(`${this.apiUrl}/${updatedServiceLine.id}`, updatedServiceLine)
      .pipe(catchError(this.handleError));
  }

  deleteServiceLine(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`)
      .pipe(catchError(this.handleError));
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred';
    
    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code
      errorMessage = `Error Code: ${error.status}, Message: ${error.message}`;
    }
    
    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}