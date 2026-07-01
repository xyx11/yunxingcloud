import request from './request'
export interface Feedback { type:string; content:string; contact?:string; images?:string }
export const submitFeedback = (data: Feedback) => request.post('/api/feedback', data)
export const fetchFeedback = () => request.get('/api/feedback')
export const replyFeedback = (id:number, reply:string) => request.put(`/api/feedback/${id}/reply`,{reply})
