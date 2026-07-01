import request from './request'

export interface Article {
  id?: number; title: string; content?: string; category?: string
  coverUrl?: string; tags?: string; status?: string; publishAt?: string
}
export const fetchArticles = () => request.get('/api/articles')
export const getArticle = (id: number) => request.get(`/api/articles/${id}`)
export const createArticle = (data: Article) => request.post('/api/articles', data)
export const updateArticle = (id: number, data: Article) => request.put(`/api/articles/${id}`, data)