import request from './request'

export const getInvoices = () => request.get('/invoices')
export const applyInvoice = (data: Record<string, any>) => request.post('/invoices', data)