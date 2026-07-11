import request from '@/api/request'

export function fetchWarehouses() { return request.get('/api/warehouses') }
export function createWarehouse(data: Record<string, unknown>) { return request.post('/api/warehouses', data) }
export function fetchInventory(warehouseId?: number) { return request.get('/api/inventory' + (warehouseId ? `?warehouseId=${warehouseId}` : '')) }
export function stockIn(data: Record<string, unknown>) { return request.post('/api/inventory/stock-in', data) }
export function stockOut(data: Record<string, unknown>) { return request.post('/api/inventory/stock-out', data) }
export function fetchAlerts() { return request.get('/api/inventory/alerts') }
export function fetchLogs(productId?: number) { return request.get('/api/inventory/logs' + (productId ? `?productId=${productId}` : '')) }
