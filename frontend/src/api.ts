import axios from 'axios'

export const http = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
})

http.interceptors.response.use((response) => {
  const body = response.data
  if (body && body.code !== 0) {
    return Promise.reject(new Error(body.message || '请求失败'))
  }
  return body.data
})

export interface DictItem {
  id: string
  no: string
  name: string
  parentId?: string
  type?: string
  leaf: boolean
}

export const api = {
  dict: () => http.get<any, Record<string, DictItem[]>>('/dict'),
  page: (data: any) => http.post<any, any>('/reimbursements/page', data),
  detail: (id: string) => http.get<any, any>(`/reimbursements/${id}`),
  save: (data: any) => http.post<any, any>('/reimbursements/save', data),
  submit: (data: any) => http.post<any, any>('/reimbursements/submit', data),
  voidDocument: (id: string) => http.post<any, any>(`/reimbursements/${id}/void`)
}
