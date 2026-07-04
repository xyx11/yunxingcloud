import request from './request'

export function fetchSalesOverview() { return request.get('/api/analytics/sales-overview') }
export function fetchOrderTrend() { return request.get('/api/analytics/order-trend') }
export function fetchTopProducts(limit = 10) { return request.get('/api/analytics/top-products', { params: { limit } }) }
export function fetchRevenueOverview() { return request.get('/api/revenue/overview') }
export function fetchDailyRevenue() { return request.get('/api/revenue/daily') }
