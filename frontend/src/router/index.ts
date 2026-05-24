import { createRouter, createWebHistory } from 'vue-router'
import ReimbursementList from '../views/ReimbursementList.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'reimbursement-list',
      component: ReimbursementList
    },
    {
      path: '/reimbursement/:id?',
      name: 'reimbursement-detail',
      component: () => import('../views/ReimbursementDetail.vue')
    }
  ]
})

export default router
