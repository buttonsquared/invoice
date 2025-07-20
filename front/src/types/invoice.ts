export interface Invoice {
  id?: number;
  freelancerEmail: string;
  clientName: string;
  clientEmail?: string;
  amount: number;
  description: string;
  walletAddress: string;
  status: InvoiceStatus;
  createdAt: string;
  paidAt?: string;
  expiresAt?: string;
  paymentUrl: string;
}

export enum InvoiceStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  EXPIRED = 'EXPIRED',
  CANCELLED = 'CANCELLED'
}

export interface CreateInvoiceRequest {
  freelancerEmail: string;
  clientName: string;
  clientEmail?: string;
  amount: number;
  description: string;
}