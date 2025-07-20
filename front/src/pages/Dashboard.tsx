import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { invoiceApi } from '../services/api';
import { Invoice, InvoiceStatus } from '../types/invoice';

const Dashboard: React.FC = () => {
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [freelancerEmail, setFreelancerEmail] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchInvoices = async () => {
    if (!freelancerEmail) return;
    
    setLoading(true);
    try {
      const data = await invoiceApi.getInvoicesByFreelancer(freelancerEmail);
      setInvoices(data);
    } catch (error) {
      console.error('Failed to fetch invoices:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoices();
  }, [freelancerEmail]);

  const getStatusColor = (status: InvoiceStatus) => {
    switch (status) {
      case InvoiceStatus.PAID:
        return 'text-green-800 bg-green-100';
      case InvoiceStatus.PENDING:
        return 'text-yellow-800 bg-yellow-100';
      case InvoiceStatus.EXPIRED:
        return 'text-red-800 bg-red-100';
      case InvoiceStatus.CANCELLED:
        return 'text-gray-800 bg-gray-100';
      default:
        return 'text-gray-800 bg-gray-100';
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">Invoice Dashboard</h1>
        <Link
          to="/create"
          className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
        >
          Create Invoice
        </Link>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <div className="mb-4">
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
            Your Email Address
          </label>
          <input
            type="email"
            id="email"
            value={freelancerEmail}
            onChange={(e) => setFreelancerEmail(e.target.value)}
            placeholder="freelancer@example.com"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>
      </div>

      {loading ? (
        <div className="text-center py-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-2 text-gray-600">Loading invoices...</p>
        </div>
      ) : (
        <div className="bg-white shadow overflow-hidden sm:rounded-md">
          {invoices.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500">
                {freelancerEmail ? 'No invoices found.' : 'Enter your email to view invoices.'}
              </p>
            </div>
          ) : (
            <ul className="divide-y divide-gray-200">
              {invoices.map((invoice) => (
                <li key={invoice.id}>
                  <Link
                    to={`/invoice/${invoice.id}`}
                    className="block hover:bg-gray-50 px-4 py-4 sm:px-6"
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="text-sm font-medium text-blue-600 truncate">
                            {invoice.clientName}
                          </p>
                          <div className="ml-2 flex-shrink-0">
                            <span
                              className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(
                                invoice.status
                              )}`}
                            >
                              {invoice.status}
                            </span>
                          </div>
                        </div>
                        <div className="mt-2 flex justify-between">
                          <div className="sm:flex">
                            <p className="flex items-center text-sm text-gray-500">
                              ${invoice.amount.toFixed(2)} USDC
                            </p>
                            <p className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0 sm:ml-6">
                              {new Date(invoice.createdAt).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                        <p className="mt-2 text-sm text-gray-500 truncate">
                          {invoice.description}
                        </p>
                      </div>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
};

export default Dashboard;