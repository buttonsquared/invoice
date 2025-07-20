import axios from 'axios';
import { Invoice, CreateInvoiceRequest } from '../types/invoice';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const invoiceApi = {
  createInvoice: async (request: CreateInvoiceRequest): Promise<Invoice> => {
    const response = await api.post('/invoices', request);
    return response.data;
  },

  getInvoice: async (id: number): Promise<Invoice> => {
    const response = await api.get(`/invoices/${id}`);
    return response.data;
  },

  getInvoicesByFreelancer: async (freelancerEmail: string): Promise<Invoice[]> => {
    const response = await api.get(`/invoices?userId=${encodeURIComponent(freelancerEmail)}`);
    return response.data;
  },

  markInvoiceAsPaid: async (id: number): Promise<Invoice> => {
    const response = await api.post(`/invoices/${id}/mark-paid`);
    return response.data;
  },
};

export default api;