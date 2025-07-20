import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import CreateInvoice from './pages/CreateInvoice';
import InvoiceDetail from './pages/InvoiceDetail';
import './App.css';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <nav className="bg-white shadow">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex justify-between h-16">
              <div className="flex items-center">
                <h1 className="text-xl font-semibold text-gray-900">
                  Invoice App
                </h1>
              </div>
            </div>
          </div>
        </nav>
        
        <main className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/create" element={<CreateInvoice />} />
            <Route path="/invoice/:id" element={<InvoiceDetail />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;