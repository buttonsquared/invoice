import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import QRCode from 'qrcode.react';
import { invoiceApi } from '../services/api';
import { Invoice, InvoiceStatus } from '../types/invoice';

const InvoiceDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [invoice, setInvoice] = useState<Invoice | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  const fetchInvoice = async () => {
    if (!id) return;
    
    try {
      const data = await invoiceApi.getInvoice(parseInt(id));
      setInvoice(data);
    } catch (err) {
      setError('Invoice not found');
      console.error('Error fetching invoice:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoice();
  }, [id]);

  // Poll for payment status every 10 seconds
  useEffect(() => {
    if (!invoice || invoice.status === InvoiceStatus.PAID) return;

    const interval = setInterval(() => {
      fetchInvoice();
    }, 10000);

    return () => clearInterval(interval);
  }, [invoice]);

  const copyToClipboard = async (text: string) => {
    try {
      await navigator.clipboard.writeText(text);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Failed to copy:', err);
    }
  };

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

  if (loading) {
    return (
      <div className="text-center py-12">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        <p className="mt-4 text-gray-600">Loading invoice...</p>
      </div>
    );
  }

  if (error || !invoice) {
    return (
      <div className="text-center py-12">
        <p className="text-red-600 text-lg">{error || 'Invoice not found'}</p>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <div className="bg-white shadow-lg rounded-lg overflow-hidden">
        {/* Header */}
        <div className="bg-gray-50 px-6 py-4 border-b border-gray-200">
          <div className="flex items-center justify-between">
            <h1 className="text-2xl font-bold text-gray-900">
              Invoice #{invoice.id}
            </h1>
            <span
              className={`inline-flex px-3 py-1 text-sm font-medium rounded-full ${getStatusColor(
                invoice.status
              )}`}
            >
              {invoice.status}
            </span>
          </div>
        </div>

        <div className="p-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* Invoice Details */}
            <div className="space-y-6">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Invoice Details</h2>
                <dl className="space-y-3">
                  <div>
                    <dt className="text-sm font-medium text-gray-500">From</dt>
                    <dd className="text-sm text-gray-900">{invoice.freelancerEmail}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">To</dt>
                    <dd className="text-sm text-gray-900">{invoice.clientName}</dd>
                  </div>
                  {invoice.clientEmail && (
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Client Email</dt>
                      <dd className="text-sm text-gray-900">{invoice.clientEmail}</dd>
                    </div>
                  )}
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Amount</dt>
                    <dd className="text-lg font-semibold text-gray-900">
                      ${invoice.amount.toFixed(2)} USDC
                    </dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Description</dt>
                    <dd className="text-sm text-gray-900">{invoice.description}</dd>
                  </div>
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Created</dt>
                    <dd className="text-sm text-gray-900">
                      {new Date(invoice.createdAt).toLocaleString()}
                    </dd>
                  </div>
                  {invoice.paidAt && (
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Paid At</dt>
                      <dd className="text-sm text-gray-900">
                        {new Date(invoice.paidAt).toLocaleString()}
                      </dd>
                    </div>
                  )}
                  {invoice.expiresAt && (
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Expires</dt>
                      <dd className="text-sm text-gray-900">
                        {new Date(invoice.expiresAt).toLocaleString()}
                      </dd>
                    </div>
                  )}
                </dl>
              </div>

              {/* Payment Address */}
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-4">Payment Address</h3>
                <div className="bg-gray-50 p-4 rounded-lg">
                  <p className="text-xs text-gray-600 mb-2">Send USDC to this address:</p>
                  <div className="flex items-center space-x-2">
                    <code className="flex-1 text-sm bg-white p-2 rounded border font-mono break-all">
                      {invoice.walletAddress}
                    </code>
                    <button
                      onClick={() => copyToClipboard(invoice.walletAddress)}
                      className="px-3 py-2 bg-blue-600 text-white text-sm rounded hover:bg-blue-700 transition-colors"
                    >
                      {copied ? 'Copied!' : 'Copy'}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            {/* QR Code */}
            <div className="flex flex-col items-center space-y-4">
              <h3 className="text-lg font-semibold text-gray-900">Payment QR Code</h3>
              <div className="bg-white p-4 rounded-lg border">
                <QRCode 
                  value={invoice.walletAddress} 
                  size={200}
                  level="H"
                />
              </div>
              <p className="text-sm text-gray-600 text-center max-w-xs">
                Scan this QR code with your crypto wallet to send payment
              </p>
              
              {invoice.status === InvoiceStatus.PENDING && (
                <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
                  <div className="flex items-center">
                    <div className="animate-pulse w-3 h-3 bg-yellow-400 rounded-full mr-2"></div>
                    <p className="text-sm text-yellow-800">
                      Waiting for payment... This page will update automatically when payment is received.
                    </p>
                  </div>
                </div>
              )}

              {invoice.status === InvoiceStatus.PAID && (
                <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded-lg">
                  <div className="flex items-center">
                    <div className="w-3 h-3 bg-green-400 rounded-full mr-2"></div>
                    <p className="text-sm text-green-800 font-medium">
                      Payment received! Thank you.
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default InvoiceDetail;