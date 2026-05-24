<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowDown,
  ArrowUp,
  CirclePlus,
  CopyDocument,
  Delete,
  EditPen,
  Refresh,
  WarningFilled
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api, type DictItem } from '../api'

const route = useRoute()
const router = useRouter()
const dict = reactive<Record<string, DictItem[]>>({
  companies: [],
  departments: [],
  employees: [],
  businessTypes: [],
  cities: [],
  projects: []
})
const open = reactive({
  base: true,
  trip: true,
  subsidy: true,
  total: true,
  allocation: true,
  remark: true
})
const form = reactive<any>(emptyForm())
const tripDialog = ref(false)
const tripEditingIndex = ref(-1)
const tripForm = reactive<any>(emptyTrip())
const subsidyDialog = ref(false)
const subsidyIndex = ref(-1)
const calendarDraft = ref<any[]>([])

const businessTree = computed(() => toTree(dict.businessTypes))
const activeSubsidy = computed(() => form.subsidies[subsidyIndex.value] || {})
const totals = computed(() => ({
  subsidyTotal: money(form.subsidyTotal),
  mealAllowance: money(form.mealAllowance),
  transportationAllowance: money(form.transportationAllowance),
  phoneAllowance: money(form.phoneAllowance)
}))

function emptyForm() {
  return {
    id: '',
    reimbursementNo: '',
    submitDate: new Date().toISOString().slice(0, 10),
    reimbursementTitle: '',
    reimburserId: '',
    reimburserNo: '',
    reimburserName: '',
    reimDepartmentId: '',
    reimDepartmentNo: '',
    reimDepartmentName: '',
    reimCompanyId: '',
    reimCompanyNo: '',
    reimCompanyName: '',
    businessTypeId: '',
    businessTypeNo: '',
    businessTypeName: '',
    businessTripReason: '',
    subsidyTotal: 0,
    mealAllowance: 0,
    transportationAllowance: 0,
    phoneAllowance: 0,
    remarks: '',
    trips: [],
    subsidies: [],
    allocations: []
  }
}

function emptyTrip() {
  return {
    id: '',
    travelerId: '',
    travelerNo: '',
    travelerName: '',
    fromCityNo: '',
    fromCityName: '',
    toCityNo: '',
    toCityName: '',
    dateRange: [],
    startDate: '',
    endDate: '',
    tripDescription: ''
  }
}

function toTree(items: DictItem[], parent = 'none'): any[] {
  return items
    .filter((item) => (item.parentId || 'none') === parent)
    .map((item) => ({ label: item.name, value: item.id, disabled: !item.leaf, children: toTree(items, item.id) }))
}

function fillSelected(target: any, prefix: string, item?: DictItem) {
  target[`${prefix}Id`] = item?.id || ''
  target[`${prefix}No`] = item?.no || ''
  target[`${prefix}Name`] = item?.name || ''
}

function onEmployeeChange(id: string) {
  const item = dict.employees.find((row) => row.id === id)
  fillSelected(form, 'reimburser', item)
}

function onDepartmentChange(id: string) {
  const item = dict.departments.find((row) => row.id === id)
  fillSelected(form, 'reimDepartment', item)
}

function onCompanyChange(id: string) {
  const item = dict.companies.find((row) => row.id === id)
  fillSelected(form, 'reimCompany', item)
  if (form.allocations.length === 1) Object.assign(form.allocations[0], companyFields(item))
}

function onBusinessChange(id: string) {
  const item = dict.businessTypes.find((row) => row.id === id)
  form.businessTypeId = item?.id || ''
  form.businessTypeNo = item?.no || ''
  form.businessTypeName = item?.name || ''
}

function companyFields(item?: DictItem) {
  return { reimCompanyId: item?.id || '', reimCompanyNo: item?.no || '', reimCompanyName: item?.name || '' }
}

function openTrip(index = -1, copy = false) {
  tripEditingIndex.value = copy ? -1 : index
  Object.assign(tripForm, emptyTrip())
  const source = index >= 0 ? form.trips[index] : null
  if (source) {
    Object.assign(tripForm, JSON.parse(JSON.stringify(source)))
    tripForm.dateRange = [source.startDate, source.endDate]
    if (copy) tripForm.id = ''
  } else if (form.reimburserId) {
    Object.assign(tripForm, {
      travelerId: form.reimburserId,
      travelerNo: form.reimburserNo,
      travelerName: form.reimburserName
    })
  }
  tripDialog.value = true
}

function saveTrip() {
  const traveler = dict.employees.find((item) => item.id === tripForm.travelerId)
  const fromCity = dict.cities.find((item) => item.id === tripForm.fromCityNo)
  const toCity = dict.cities.find((item) => item.id === tripForm.toCityNo)
  Object.assign(tripForm, {
    travelerNo: traveler?.no || '',
    travelerName: traveler?.name || '',
    fromCityName: fromCity?.name || '',
    toCityName: toCity?.name || '',
    startDate: tripForm.dateRange?.[0],
    endDate: tripForm.dateRange?.[1]
  })
  validateTrip(tripForm)
  const candidate = { ...tripForm }
  delete candidate.dateRange
  checkTripRepeat(candidate, tripEditingIndex.value)
  if (tripEditingIndex.value >= 0) form.trips.splice(tripEditingIndex.value, 1, candidate)
  else form.trips.push(candidate)
  tripDialog.value = false
  saveDraft(false)
}

function validateTrip(trip: any) {
  const required = [
    ['travelerId', '出行人不能为空'],
    ['fromCityNo', '出发城市不能为空'],
    ['toCityNo', '到达城市不能为空'],
    ['startDate', '出发到达日期不能为空'],
    ['tripDescription', '行程说明不能为空']
  ]
  for (const [key, message] of required) if (!trip[key]) throwMessage(message)
  if (trip.endDate < trip.startDate) throwMessage('到达日期不可早于出发日期')
  if (trip.endDate > new Date().toISOString().slice(0, 10)) throwMessage('到达日期不可晚于当前日期')
}

function checkTripRepeat(trip: any, skipIndex = -1) {
  const dates = rangeDates(trip.startDate, trip.endDate)
  form.trips.forEach((row: any, index: number) => {
    if (index === skipIndex || row.travelerId !== trip.travelerId) return
    const occupied = new Set(rangeDates(row.startDate, row.endDate))
    if (dates.some((date) => occupied.has(date))) throwMessage('同一出行人的行程日期不可重复')
  })
}

async function deleteTrip(index: number) {
  await ElMessageBox.confirm('确定删除当前行程吗？', '确认删除提示', { type: 'warning' })
  form.trips.splice(index, 1)
  await saveDraft(false)
}

function openSubsidy(index: number) {
  subsidyIndex.value = index
  calendarDraft.value = JSON.parse(JSON.stringify(form.subsidies[index].calendar || []))
  subsidyDialog.value = true
}

function saveSubsidy() {
  form.subsidies[subsidyIndex.value].calendar = calendarDraft.value
  recalcLocalTotals()
  subsidyDialog.value = false
}

function toggleDay(day: any, checked: boolean) {
  day.mealSelected = checked
  day.transportSelected = checked
  day.phoneSelected = checked
  day.mealAmount = checked ? day.mealStandard : 0
  day.transportAmount = checked ? day.transportStandard : 0
  day.phoneAmount = checked ? day.phoneStandard : 0
}

function toggleColumn(key: string, checked: boolean) {
  calendarDraft.value.forEach((day) => {
    day[`${key}Selected`] = checked
    day[`${key}Amount`] = checked ? day[`${key}Standard`] : 0
  })
}

function allSelected() {
  const checked = !calendarDraft.value.every((day) => day.mealSelected && day.transportSelected && day.phoneSelected)
  calendarDraft.value.forEach((day) => toggleDay(day, checked))
}

function dayChecked(day: any) {
  return day.mealSelected && day.transportSelected && day.phoneSelected
}

function columnChecked(key: string) {
  return calendarDraft.value.length > 0 && calendarDraft.value.every((day) => day[`${key}Selected`])
}

function recalcLocalTotals() {
  let meal = 0
  let transport = 0
  let phone = 0
  form.subsidies.forEach((subsidy: any) => {
    let apply = 0
    let amount = 0
    subsidy.calendar.forEach((day: any) => {
      meal += Number(day.mealAmount || 0)
      transport += Number(day.transportAmount || 0)
      phone += Number(day.phoneAmount || 0)
      apply += (day.mealSelected ? Number(day.mealStandard || 0) : 0)
      apply += (day.transportSelected ? Number(day.transportStandard || 0) : 0)
      apply += (day.phoneSelected ? Number(day.phoneStandard || 0) : 0)
      amount += Number(day.mealAmount || 0) + Number(day.transportAmount || 0) + Number(day.phoneAmount || 0)
    })
    subsidy.applyAmount = money(apply)
    subsidy.subsidyAmount = money(amount)
  })
  form.mealAllowance = money(meal)
  form.transportationAllowance = money(transport)
  form.phoneAllowance = money(phone)
  form.subsidyTotal = money(meal + transport + phone)
  recalcAllocation()
}

function addAllocation() {
  form.allocations.push({
    id: '',
    reimCompanyId: '',
    reimCompanyNo: '',
    reimCompanyName: '',
    projectId: '',
    projectNo: '',
    projectName: '',
    allocationRatio: 0,
    allocationAmount: 0
  })
  recalcAllocation()
}

async function deleteAllocation(index: number) {
  if (form.allocations.length === 1) return ElMessage.warning('至少保留一条分摊信息')
  await ElMessageBox.confirm('确定删除当前分摊信息吗？', '提示', { type: 'warning' })
  form.allocations.splice(index, 1)
  recalcAllocation()
}

function averageAllocation() {
  const total = Number(form.subsidyTotal || 0)
  const count = form.allocations.length
  if (!count) return
  const base = Math.floor((10000 / count)) / 10000
  let used = 0
  form.allocations.forEach((row: any, index: number) => {
    row.allocationRatio = index === 0 ? 0 : base
    used += Number(row.allocationRatio)
  })
  form.allocations[0].allocationRatio = moneyRatio(1 - used)
  form.allocations.forEach((row: any, index: number) => {
    row.allocationAmount = index === 0 ? 0 : money(total * Number(row.allocationRatio || 0))
  })
  form.allocations[0].allocationAmount = money(total - form.allocations.slice(1).reduce((sum: number, row: any) => sum + Number(row.allocationAmount || 0), 0))
}

function recalcAllocation(changedIndex = -1) {
  const total = Number(form.subsidyTotal || 0)
  if (form.allocations.length === 0) addAllocation()
  if (form.allocations.length === 1) {
    form.allocations[0].allocationRatio = 1
    form.allocations[0].allocationAmount = money(total)
    return
  }
  let otherRatio = 0
  form.allocations.forEach((row: any, index: number) => {
    if (index > 0) otherRatio += Number(row.allocationRatio || 0)
  })
  if (otherRatio > 1) {
    const row = form.allocations[changedIndex]
    if (row) row.allocationRatio = 0
    ElMessage.warning('分摊比例不可超过100%')
    return recalcAllocation()
  }
  form.allocations[0].allocationRatio = moneyRatio(1 - otherRatio)
  form.allocations.forEach((row: any, index: number) => {
    row.allocationAmount = index === 0 ? 0 : money(total * Number(row.allocationRatio || 0))
  })
  form.allocations[0].allocationAmount = money(total - form.allocations.slice(1).reduce((sum: number, row: any) => sum + Number(row.allocationAmount || 0), 0))
}

function onAllocationCompany(row: any) {
  const item = dict.companies.find((company) => company.id === row.reimCompanyId)
  Object.assign(row, companyFields(item))
}

function onProject(row: any) {
  const item = dict.projects.find((project) => project.id === row.projectId)
  row.projectNo = item?.no || ''
  row.projectName = item?.name || ''
}

async function saveDraft(showMessage = true) {
  try {
    const saved = await api.save(form)
    Object.assign(form, saved)
    if (showMessage) ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  }
}

async function submit() {
  try {
    const saved = await api.submit(form)
    Object.assign(form, saved)
    await ElMessageBox.alert('提交成功', '提示')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '提交失败')
  }
}

async function closePage() {
  await ElMessageBox.confirm('确定关闭当前页面吗？', '提示')
  router.push('/')
}

async function clearRemark() {
  await ElMessageBox.confirm('确定删除备注信息吗？', '提示', { type: 'warning' })
  form.remarks = ''
}

function rangeDates(start: string, end: string) {
  const dates: string[] = []
  const cursor = new Date(start)
  const last = new Date(end)
  while (cursor <= last) {
    dates.push(cursor.toISOString().slice(0, 10))
    cursor.setDate(cursor.getDate() + 1)
  }
  return dates
}

function money(value: any) {
  return Number(value || 0).toFixed(2)
}

function moneyRatio(value: any) {
  return Number(Number(value || 0).toFixed(4))
}

function throwMessage(message: string): never {
  ElMessage.error(message)
  throw new Error(message)
}

function disabledFuture(date: Date) {
  return date.getTime() > Date.now()
}

async function init() {
  Object.assign(dict, await api.dict())
  const id = route.params.id as string | undefined
  if (id) Object.assign(form, await api.detail(id))
  if (!form.allocations.length) {
    form.allocations.push({
      id: '',
      ...companyFields(dict.companies[0]),
      projectId: '',
      projectNo: '',
      projectName: '',
      allocationRatio: 1,
      allocationAmount: 0
    })
  }
  recalcAllocation()
}

onMounted(init)
</script>

<template>
  <main class="detail-page">
    <header class="doc-header">
      <div></div>
      <h1>差旅费用报销单</h1>
      <div>提单日期&nbsp;&nbsp;{{ form.submitDate }}</div>
    </header>

    <div class="doc-shell">
      <section class="section">
        <div class="section-title" @click="open.base = !open.base">
          <span>基础信息</span>
          <el-icon><component :is="open.base ? ArrowUp : ArrowDown" /></el-icon>
        </div>
        <div v-show="open.base" class="base-form">
          <label class="wide"><span>报销标题</span><el-input v-model="form.reimbursementTitle" maxlength="500" /></label>
          <label><span>报销人</span><el-select v-model="form.reimburserId" @change="onEmployeeChange"><el-option v-for="item in dict.employees" :key="item.id" :label="item.name" :value="item.id" /></el-select></label>
          <label><span>报销部门</span><el-select v-model="form.reimDepartmentId" @change="onDepartmentChange"><el-option v-for="item in dict.departments" :key="item.id" :label="item.name" :value="item.id" /></el-select></label>
          <label><span>费用归属公司<i>*</i></span><el-select v-model="form.reimCompanyId" @change="onCompanyChange"><el-option v-for="item in dict.companies" :key="item.id" :label="item.name" :value="item.id" /></el-select></label>
          <label><span>业务类型<i>*</i></span><el-tree-select v-model="form.businessTypeId" :data="businessTree" check-strictly @change="onBusinessChange" /></label>
          <label class="wide"><span>出差事由</span><el-input v-model="form.businessTripReason" type="textarea" maxlength="500" rows="2" placeholder="请输入" /></label>
        </div>
      </section>

      <section class="section">
        <div class="section-title">
          <span>补录行程</span>
          <button class="title-action" @click="openTrip()"><el-icon><CirclePlus /></el-icon>补录行程</button>
        </div>
        <el-table :data="form.trips" border>
          <el-table-column type="index" label="序号" width="64" />
          <el-table-column label="出行人员" width="250"><template #default="{ row }">{{ row.travelerName }}/{{ row.travelerNo }}</template></el-table-column>
          <el-table-column label="出差日期" width="250"><template #default="{ row }">{{ row.startDate }} 至 {{ row.endDate }}</template></el-table-column>
          <el-table-column label="行程"><template #default="{ row }">{{ row.fromCityName }} - {{ row.toCityName }}</template></el-table-column>
          <el-table-column prop="tripDescription" label="行程说明" />
          <el-table-column label="操作" width="130">
            <template #default="{ $index }">
              <div class="row-actions">
                <el-icon @click="deleteTrip($index)"><Delete /></el-icon>
                <el-icon @click="openTrip($index)"><EditPen /></el-icon>
                <el-icon @click="openTrip($index, true)"><CopyDocument /></el-icon>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="section">
        <div class="section-title" @click="open.subsidy = !open.subsidy">
          <span>补助信息 <b>{{ totals.subsidyTotal }}</b>（{{ form.reimburserName || '出行人' }}:{{ form.subsidies[0]?.days || 0 }}天）</span>
          <el-icon><component :is="open.subsidy ? ArrowUp : ArrowDown" /></el-icon>
        </div>
        <div v-show="open.subsidy">
          <el-tooltip content="1、请根据实际出差日期选择补助 2、出差期间当日有用餐安排的请自行核减当日餐补 3、出差期间当日有用车的，请自行核减当日交补" placement="top">
            <div class="tip"><el-icon><WarningFilled /></el-icon>1、请根据实际出差日期选择补助2、出差期间当日有用餐安排的请自行核减当日餐补3、出差期间当日有用车的，请自行核减当日交补</div>
          </el-tooltip>
          <el-table :data="form.subsidies" border>
            <el-table-column type="index" label="序号" width="64" />
            <el-table-column prop="travelerName" label="出行人" />
            <el-table-column prop="tripDateRange" label="出差日期" width="220" />
            <el-table-column prop="days" label="补助天数" width="100" />
            <el-table-column prop="route" label="行程" />
            <el-table-column prop="subsidyCity" label="补助城市" />
            <el-table-column label="申请金额" align="right"><template #default="{ row }">{{ money(row.applyAmount) }}</template></el-table-column>
            <el-table-column label="补助金额" align="right"><template #default="{ row }">{{ money(row.subsidyAmount) }}</template></el-table-column>
            <el-table-column label="操作" width="80"><template #default="{ $index }"><el-icon class="theme-link" @click="openSubsidy($index)"><EditPen /></el-icon></template></el-table-column>
          </el-table>
        </div>
      </section>

      <section class="section">
        <div class="section-title" @click="open.total = !open.total">
          <span>费用合计</span>
          <el-icon><component :is="open.total ? ArrowUp : ArrowDown" /></el-icon>
        </div>
        <div v-show="open.total" class="total-line">
          <span>补助总金额 <b>{{ totals.subsidyTotal }}</b></span>
          <span>餐费补助 <b>{{ totals.mealAllowance }}</b></span>
          <span>交通补助 <b>{{ totals.transportationAllowance }}</b></span>
          <span>通讯补助 <b>{{ totals.phoneAllowance }}</b></span>
        </div>
      </section>

      <section class="section">
        <div class="section-title" @click="open.allocation = !open.allocation">
          <span>费用归属及分摊（分摊金额：{{ totals.subsidyTotal }}）</span>
          <el-icon><component :is="open.allocation ? ArrowUp : ArrowDown" /></el-icon>
        </div>
        <div v-show="open.allocation">
          <el-table :data="form.allocations" border show-summary :summary-method="() => ['合计', '', '', `${(form.allocations.reduce((s: number, r: any) => s + Number(r.allocationRatio || 0), 0) * 100).toFixed(2)}%`, `CNY ${money(form.allocations.reduce((s: number, r: any) => s + Number(r.allocationAmount || 0), 0))}`, '']">
            <el-table-column type="index" label="序号" width="64" />
            <el-table-column label="费用归属*" width="260">
              <template #default="{ row }"><el-select v-model="row.reimCompanyId" @change="onAllocationCompany(row)"><el-option v-for="item in dict.companies" :key="item.id" :label="item.name" :value="item.id" /></el-select></template>
            </el-table-column>
            <el-table-column label="项目" width="260">
              <template #default="{ row }"><el-select v-model="row.projectId" placeholder="请选择" clearable @change="onProject(row)"><el-option v-for="item in dict.projects" :key="item.id" :label="item.name" :value="item.id" /></el-select></template>
            </el-table-column>
            <el-table-column label="分摊比例*" width="230" align="right">
              <template #header>分摊比例 <el-icon class="theme-link" @click.stop="averageAllocation"><Refresh /></el-icon> *</template>
              <template #default="{ row, $index }"><el-input-number v-model="row.allocationRatio" :disabled="$index === 0" :min="0" :max="1" :step="0.01" :precision="4" @change="recalcAllocation($index)" /><span class="percent">{{ (Number(row.allocationRatio || 0) * 100).toFixed(2) }}%</span></template>
            </el-table-column>
            <el-table-column label="分摊金额*" align="right" width="230"><template #default="{ row }"><el-input v-model="row.allocationAmount" disabled /></template></el-table-column>
            <el-table-column label="操作" width="90"><template #default="{ $index }"><el-icon class="theme-link" @click="deleteAllocation($index)"><Delete /></el-icon></template></el-table-column>
          </el-table>
          <button class="add-row" @click="addAllocation"><el-icon><CirclePlus /></el-icon>添加一行</button>
        </div>
      </section>

      <section class="section remark-section">
        <div class="section-title">
          <span>备注信息</span>
          <button class="title-action" @click="clearRemark"><el-icon><Delete /></el-icon>删除备注</button>
        </div>
        <el-input v-model="form.remarks" type="textarea" maxlength="1000" rows="5" placeholder="请输入" />
      </section>
    </div>

    <footer class="doc-footer">
      <el-button @click="closePage">关闭</el-button>
      <el-button type="primary" @click="submit">提交</el-button>
    </footer>

    <el-dialog v-model="tripDialog" title="补录行程" width="824px" align-center>
      <div class="dialog-tip"><el-icon><WarningFilled /></el-icon>仅可补录未从申请单带入或未产生费用的行程信息<br />跨天跨城行程填写说明： 出发城市-到达城市：武汉-北京; 出发日期-到达日期：1号-5号; 1号~5号补助按北京匹配;</div>
      <div class="trip-form">
        <label><span>出行人<i>*</i></span><el-select v-model="tripForm.travelerId"><el-option v-for="item in dict.employees" :key="item.id" :label="item.name" :value="item.id" /></el-select></label>
        <label><span>出发城市<i>*</i></span><el-select v-model="tripForm.fromCityNo"><el-option v-for="item in dict.cities" :key="item.id" :label="item.name" :value="item.id" /></el-select></label>
        <label><span>到达城市<i>*</i></span><el-select v-model="tripForm.toCityNo"><el-option v-for="item in dict.cities" :key="item.id" :label="item.name" :value="item.id" /></el-select></label>
        <label><span>出发到达日期<i>*</i></span><el-date-picker v-model="tripForm.dateRange" type="daterange" value-format="YYYY-MM-DD" :disabled-date="disabledFuture" start-placeholder="出发日期" end-placeholder="到达日期" /></label>
        <label class="wide"><span>行程说明<i>*</i></span><el-input v-model="tripForm.tripDescription" type="textarea" maxlength="500" rows="3" /></label>
      </div>
      <template #footer>
        <el-button @click="tripDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTrip">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="subsidyDialog" title="补助日历" width="1320px" align-center class="calendar-dialog">
      <div class="calendar-layout">
        <aside>
          <div class="type-line">出差类型 <b>{{ form.businessTypeName }}</b></div>
          <div class="timeline">
            <p>开始日期 <span>{{ form.trips[subsidyIndex]?.startDate }}</span></p>
            <p class="active">行程天数 <span>{{ activeSubsidy.route }} <b>{{ activeSubsidy.days }}天</b></span></p>
            <p>结束日期 <span>{{ form.trips[subsidyIndex]?.endDate }}</span></p>
          </div>
          <div class="amount-box">
            <p>补助金额 <span>CNY <b>{{ money(activeSubsidy.subsidyAmount) }}</b></span></p>
            <p>标准总额 <span>CNY <b>{{ money(activeSubsidy.applyAmount) }}</b></span></p>
            <p>补助金额 <span>CNY <b>{{ money(activeSubsidy.subsidyAmount) }}</b></span></p>
          </div>
        </aside>
        <section class="calendar-table">
          <div class="all-check"><el-checkbox :model-value="calendarDraft.length && calendarDraft.every(dayChecked)" @change="allSelected">全选</el-checkbox></div>
          <el-table :data="calendarDraft" border>
            <el-table-column label="出差日期" width="190">
              <template #default="{ row }">{{ row.tripDate }}<br />{{ row.weekName }} <el-checkbox :model-value="dayChecked(row)" @change="(v: any) => toggleDay(row, v)" /></template>
            </el-table-column>
            <el-table-column prop="subsidyCity" label="补助城市" width="170" />
            <el-table-column label="餐费补助" align="center">
              <template #header>餐费补助 <el-checkbox :model-value="columnChecked('meal')" @change="(v: any) => toggleColumn('meal', v)" /></template>
              <template #default="{ row }"><span class="standard">CNY {{ money(row.mealStandard) }} /天</span><div><el-checkbox v-model="row.mealSelected" @change="row.mealAmount = row.mealSelected ? row.mealStandard : 0" /><el-input-number v-model="row.mealAmount" :disabled="!row.mealSelected" :min="0" :max="row.mealStandard" :precision="2" /></div></template>
            </el-table-column>
            <el-table-column label="交通补助" align="center">
              <template #header>交通补助 <el-checkbox :model-value="columnChecked('transport')" @change="(v: any) => toggleColumn('transport', v)" /></template>
              <template #default="{ row }"><span class="standard">CNY {{ money(row.transportStandard) }} /天</span><div><el-checkbox v-model="row.transportSelected" @change="row.transportAmount = row.transportSelected ? row.transportStandard : 0" /><el-input-number v-model="row.transportAmount" :disabled="!row.transportSelected" :min="0" :max="row.transportStandard" :precision="2" /></div></template>
            </el-table-column>
            <el-table-column label="通讯补助" align="center">
              <template #header>通讯补助 <el-checkbox :model-value="columnChecked('phone')" @change="(v: any) => toggleColumn('phone', v)" /></template>
              <template #default="{ row }"><span class="standard">CNY {{ money(row.phoneStandard) }} /天</span><div><el-checkbox v-model="row.phoneSelected" @change="row.phoneAmount = row.phoneSelected ? row.phoneStandard : 0" /><el-input-number v-model="row.phoneAmount" :disabled="!row.phoneSelected" :min="0" :max="row.phoneStandard" :precision="2" /></div></template>
            </el-table-column>
          </el-table>
        </section>
      </div>
      <template #footer>
        <el-button @click="subsidyDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSubsidy">确认</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<style scoped>
.detail-page {
  min-height: 100vh;
  padding: 54px 0 88px;
  background: #f5f6f8;
}

.doc-header,
.doc-footer {
  position: fixed;
  left: 0;
  right: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  align-items: center;
  background: #fff;
}

.doc-header {
  top: 0;
  height: 54px;
  padding: 0 36px;
}

.doc-header h1 {
  margin: 0;
  text-align: center;
  font-size: 20px;
  font-weight: 700;
}

.doc-header div:last-child {
  text-align: right;
}

.doc-footer {
  bottom: 0;
  height: 58px;
  display: flex;
  justify-content: center;
  gap: 10px;
  border-top: 1px solid #edf0f2;
}

.doc-shell {
  width: 1200px;
  min-height: calc(100vh - 142px);
  margin: 18px auto 0;
  padding: 16px 20px 56px;
  background: #fff;
}

.section {
  margin-bottom: 26px;
}

.section-title {
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  background: #f0f2f5;
  font-size: 16px;
  cursor: pointer;
}

.section-title span {
  border-left: 3px solid #008bd2;
  padding-left: 8px;
}

.section-title b,
.amount-box b,
.standard {
  color: #ff6b00;
  font-weight: 400;
}

.title-action,
.add-row {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 0;
  background: transparent;
  color: #007adf;
  cursor: pointer;
}

.base-form {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px 62px;
  padding: 16px 46px 6px;
}

.base-form label,
.trip-form label {
  display: grid;
  grid-template-columns: 92px minmax(0, 1fr);
  align-items: center;
}

.base-form .wide,
.trip-form .wide {
  grid-column: 1 / -1;
}

label span {
  text-align: right;
  padding-right: 12px;
}

i {
  color: #f56c6c;
  font-style: normal;
}

.row-actions {
  display: flex;
  gap: 14px;
  color: #1677ff;
  cursor: pointer;
}

.tip,
.dialog-tip {
  margin: 10px 0 12px;
  padding: 10px 14px;
  color: #303133;
  background: #fff6e6;
  border-radius: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.total-line {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  padding: 16px 42px;
}

.total-line b {
  margin-left: 16px;
  color: #001a3a;
  font-weight: 400;
}

.percent {
  margin-left: 8px;
}

.add-row {
  width: 100%;
  height: 40px;
  justify-content: center;
  border: 1px solid #ebeef5;
  border-top: 0;
}

.remark-section :deep(.el-textarea__inner) {
  margin-top: 16px;
  min-height: 96px;
}

.trip-form {
  display: grid;
  gap: 18px;
  width: 650px;
  margin-left: 46px;
}

.calendar-layout {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 20px;
  min-height: 570px;
}

.type-line {
  margin-bottom: 10px;
  font-size: 16px;
}

.type-line b {
  margin-left: 16px;
  color: #ff6b00;
}

.timeline,
.amount-box {
  border: 1px solid #e5e7eb;
  padding: 12px 16px;
  margin-bottom: 34px;
}

.timeline p,
.amount-box p {
  display: flex;
  justify-content: space-between;
  margin: 12px 0;
}

.timeline .active {
  padding: 9px;
  color: #fff;
  background: #008bd2;
}

.amount-box span {
  min-width: 118px;
  display: inline-flex;
  justify-content: space-between;
}

.calendar-table {
  position: relative;
}

.all-check {
  position: absolute;
  right: 0;
  top: -32px;
}

.calendar-table :deep(.el-input-number) {
  width: 104px;
}
</style>
