import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CustomerRequest } from '../models/customer-request';
import { InProgressSessionResponse } from '../models/in-progress-session';
import { SessionMetrics } from '../models/session-metrics';
import { ServiceType } from '../models/service-type';

@Injectable({
  providedIn: 'root',
})
export class CustomerService {
  private apiUrl = 'http://localhost:8080/api/customer';

  constructor(private http: HttpClient) {}

  createCustomer(request: CustomerRequest): Observable<void> {
    return new Observable((observer) => {
      fetch(this.apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
        redirect: 'manual',
      })
        .then((response) => {
          if (response.ok || response.type === 'opaqueredirect') {
            observer.next();
            observer.complete();
          } else {
            observer.error(new Error(`HTTP ${response.status}`));
          }
        })
        .catch((err) => observer.error(err));
    });
  }

  getQueuePosition(email: string): Observable<any> {
    return new Observable(observer => {
      const eventSource = new EventSource(`${this.apiUrl}/${email}/queue`);
      
      eventSource.addEventListener('queue-update', (event: any) => {
        observer.next(JSON.parse(event.data));
      });

      eventSource.onerror = (error) => {
        observer.error(error);
        eventSource.close();
      };

      return () => eventSource.close();
    });
  }

  getInProgressSessions(serviceType: ServiceType): Observable<InProgressSessionResponse> {
    return new Observable((observer) => {
      const eventSource = new EventSource(`${this.apiUrl}/in-progress/${serviceType}`);

      eventSource.addEventListener('in-progress-update', (event: any) => {
        observer.next(JSON.parse(event.data));
      });

      eventSource.onerror = (error) => {
        observer.error(error);
        eventSource.close();
      };

      return () => eventSource.close();
    });
  }

  getMetrics(): Observable<SessionMetrics> {
    return new Observable((observer) => {
      const eventSource = new EventSource(`${this.apiUrl}/metrics`);

      eventSource.addEventListener('metrics-update', (event: any) => {
        observer.next(JSON.parse(event.data));
      });

      eventSource.onerror = (error) => {
        observer.error(error);
        eventSource.close();
      };

      return () => eventSource.close();
    });
  }

  finishSession(email: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${email}`, {});
  }
}
