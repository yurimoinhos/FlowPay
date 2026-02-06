export interface SessionMetrics {
  averageSessionsPerCustomer: number;
  averageServiceDurationSeconds: number;
  pendingCount: number;
  inProgressCount: number;
  completedCount: number;
  canceledCount: number;
  totalSessions: number;
  completionRate: number;
  cancellationRate: number;
}
