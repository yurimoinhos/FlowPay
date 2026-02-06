import { ServiceType } from './service-type';

export interface CustomerRequest {
  name: string;
  email: string;
  serviceType: ServiceType;
}
