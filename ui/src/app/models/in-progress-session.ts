import { ServiceType } from './service-type';

export interface InProgressSessionResponse {
  sessionId: number;
  customerName: string;
  customerEmail: string;
  serviceType: ServiceType;
  startedAt: string;
}
