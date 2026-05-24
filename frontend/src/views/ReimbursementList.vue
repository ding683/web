<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Delete, DocumentAdd, EditPen, MoreFilled, Search, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, type DictItem } from '../api'

const router = useRouter()
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const page = reactive({ current: 1, size: 10 })
const dict = reactive<Record<string, DictItem[]>>({
  companies: [],
  departments: [],
  employees: [],
  businessTypes: [],
  cities: [],
  projects: []
})
const query = reactive({
  reimbursementNo: '',
  reimbursementTitle: '',
  businessTripReason: '',
  reimCompanyId: '',
  reimDepartmentId: '',
  reimburserId: '',
  businessTypeId: ''
})

const businessTree = computed(() => toTree(dict.businessTypes))

function toTree(items: DictItem[], parent = 'none'): any[] {
  return items
    .filter((item) => (item.parentId || 'none') === parent)
    .map((item) => ({
      label: item.name,
      value: item.id,
      disabled: !item.leaf,
      children: toTree(items, item.id)
    }))
}

async function loadDict() {
  Object.assign(dict, await api.dict())
}

async function loadPage() {
  loading.value = true
  try {
    const data = await api.page({ ...query, page: page.current, size: page.size })
    rows.value = data.records
    total.value = data.total
  } catch (error: any) {
    ElMessage.error(error.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function reset() {
  Object.assign(query, {
    reimbursementNo: '',
    reimbursementTitle: '',
    businessTripReason: '',
    reimCompanyId: '',
    reimDepartmentId: '',
    reimburserId: '',
    businessTypeId: ''
  })
  page.current = 1
  loadPage()
}

function openDetail(id?: string) {
  router.push(id ? `/reimbursement/${id}` : '/reimbursement')
}

async function voidDocument(row: any) {
  await ElMessageBox.confirm('确定作废当前单据吗？', '提示', { type: 'warning' })
  await api.voidDocument(row.id)
  ElMessage.success('作废成功')
  loadPage()
}

onMounted(async () => {
  await loadDict()
  await loadPage()
})
</script>

<template>
  <main class="list-page">
    <section class="query-panel">
      <div class="query-grid">
        <label>
          <span>报销单号</span>
          <el-input v-model="query.reimbursementNo" placeholder="请输入" clearable />
        </label>
        <label>
          <span>标题</span>
          <el-input v-model="query.reimbursementTitle" placeholder="请输入" clearable />
        </label>
        <label>
          <span>事由</span>
          <el-input v-model="query.businessTripReason" placeholder="请输入" clearable />
        </label>
        <label>
          <span>费用归属公司</span>
          <el-select v-model="query.reimCompanyId" placeholder="请选择" clearable>
            <el-option v-for="item in dict.companies" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </label>
        <label>
          <span>报销部门</span>
          <el-select v-model="query.reimDepartmentId" placeholder="请选择" clearable>
            <el-option v-for="item in dict.departments" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </label>
        <label>
          <span>报销人</span>
          <el-select v-model="query.reimburserId" placeholder="请选择" clearable>
            <el-option
              v-for="item in dict.employees"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </label>
        <label>
          <span>业务类型</span>
          <el-tree-select
            v-model="query.businessTypeId"
            :data="businessTree"
            placeholder="请选择"
            clearable
            check-strictly
          />
        </label>
        <div class="query-actions">
          <el-button type="primary" plain :icon="DocumentAdd" @click="openDetail()">新增</el-button>
          <el-button type="primary" plain :icon="RefreshLeft" @click="reset">清除</el-button>
          <el-button type="primary" :icon="Search" @click="loadPage">搜索</el-button>
        </div>
      </div>
    </section>

    <section class="table-panel">
      <el-table v-loading="loading" :data="rows" border stripe height="calc(100vh - 230px)">
        <el-table-column width="55" type="index" />
        <el-table-column label="操作" width="112" fixed="left">
          <template #default="{ row }">
            <div class="row-actions">
              <el-icon class="muted" @click="openDetail(row.id)"><DocumentAdd /></el-icon>
              <el-icon class="theme-link" @click="openDetail(row.id)"><EditPen /></el-icon>
              <el-dropdown>
                <el-icon class="theme-link"><MoreFilled /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="openDetail(row.id)">查看</el-dropdown-item>
                    <el-dropdown-item :icon="Delete" @click="voidDocument(row)">作废</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reimbursementNo" label="报销单号" width="160">
          <template #default="{ row }">
            <span class="theme-link" @click="openDetail(row.id)">{{ row.reimbursementNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="statusName" label="单据状态" width="110">
          <template #default="{ row }">
            <span class="theme-link">{{ row.statusName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单据类型" width="110">
          <template #default>日常报销单</template>
        </el-table-column>
        <el-table-column label="报销人" width="150">
          <template #default="{ row }">{{ row.reimburserName }}[{{ row.reimburserNo }}]</template>
        </el-table-column>
        <el-table-column label="报销部门" width="170">
          <template #default="{ row }">[{{ row.reimDepartmentNo }}]{{ row.reimDepartmentName }}</template>
        </el-table-column>
        <el-table-column prop="reimCompanyName" label="费用归属公司" width="170" />
        <el-table-column prop="businessTypeName" label="业务类型" width="120" />
        <el-table-column prop="reimbursementTitle" label="报销标题" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="theme-link" @click="openDetail(row.id)">{{ row.reimbursementTitle }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="businessTripReason" label="报销事由" min-width="210" show-overflow-tooltip />
        <el-table-column prop="subsidyTotal" label="补助金额" width="120" align="right">
          <template #default="{ row }">{{ Number(row.subsidyTotal || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="130">
          <template #default="{ row }">{{ String(row.creationTime || '').slice(0, 10) }}</template>
        </el-table-column>
      </el-table>
    </section>

    <footer class="pager">
      <span>共{{ total }}条</span>
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="sizes, prev, pager, next, jumper"
        @size-change="loadPage"
        @current-change="loadPage"
      />
    </footer>
  </main>
</template>

<style scoped>
.list-page {
  min-height: 100vh;
  padding: 22px 22px 74px;
  background: #fff;
}

.query-panel {
  padding: 0 0 18px;
}

.query-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(230px, 1fr));
  gap: 18px 64px;
  align-items: center;
}

.query-grid label {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
  align-items: center;
  color: #697386;
}

.query-grid label span {
  text-align: right;
  padding-right: 12px;
}

.query-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.table-panel {
  border-top: 1px solid #ebeef5;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 13px;
  font-size: 16px;
}

.muted {
  color: #c1c7d0;
  cursor: pointer;
}

.pager {
  position: fixed;
  right: 22px;
  bottom: 22px;
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
}
</style>
