import request from './request'

export const getArticles = (category?: string) =>
  request.get('/articles', { params: category ? { category } : {} })

export const getArticleById = (id: number) => request.get(`/articles/${id}`)
