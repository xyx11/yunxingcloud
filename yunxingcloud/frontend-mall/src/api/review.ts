import request from './request'

export function getMyReviews(page = 1, size = 10) {
  return request.get('/reviews/my', { params: { page, size } })
}

export function deleteReview(id: number) {
  return request.delete(`/reviews/${id}`)
}
