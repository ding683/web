# 差旅费用报销单项目
- 前端：Vue 3 + Vite + TypeScript + Element Plus
- 后端：Java 17 + Spring Boot + JDBC
- 数据库：MySQL

## 目录结构

```text
final_project
├─ frontend          # 前端项目
├─ backend           # 后端项目
└─ README.md
```

## 环境要求

使用前请先安装：

- Node.js 18 或更高版本
- JDK 17 或更高版本
- MySQL 8.x

数据库默认配置：

```text
地址：localhost:3306
数据库：travel_reimbursement
用户名：root
密码：123456
```

如需修改数据库账号密码，请编辑：

```text
backend/src/main/resources/application.properties
```

## 启动后端

进入后端目录：

```bash
cd backend
```

Windows：

```bash
mvnw.cmd spring-boot:run
```

macOS/Linux：

```bash
./mvnw spring-boot:run
```

后端默认启动地址：

```text
http://localhost:8080
```

说明：

- 启动时会自动创建数据库 `travel_reimbursement`
- 启动时会自动创建或补全 `fk_reim_main` 表字段
- 如果已有旧表结构，项目会尽量自动迁移兼容

## 启动前端

进入前端目录：

```bash
cd frontend
```

安装依赖：

```bash
npm install
```

启动开发服务：

```bash
npm run dev
```

前端默认访问地址：

```text
http://127.0.0.1:5173
```

前端接口默认请求：

```text
http://localhost:8080/api
```

因此请先启动后端，再打开前端页面。

## 功能说明

当前已实现：

- 报销单列表查询
- 报销单新增、编辑、提交、作废
- 基础信息维护
- 补录行程维护
- 行程人员 + 日期重复校验
- 自动生成补助信息和补助日历
- 餐费、交通、通讯补助选择与金额校验
- 费用合计自动计算
- 费用归属及分摊联动计算
- 备注信息维护
- 前后端接口联动

## 常用接口

```text
GET  /api/dict                         获取页面下拉数据
POST /api/reimbursements/page          分页查询报销单
GET  /api/reimbursements/{id}          查询报销单详情
POST /api/reimbursements/save          保存草稿
POST /api/reimbursements/submit        提交单据
POST /api/reimbursements/{id}/void     作废单据
```

## 构建前端

```bash
cd frontend
npm run build
```

构建产物位于：

```text
frontend/dist
```

## 移植到其他电脑

建议只拷贝：

```text
frontend/
backend/
README.md
```

不建议拷贝：

```text
frontend/node_modules
backend/target
.idea
```

换电脑后重新执行：

```bash
cd frontend
npm install
```

后端重新通过 Maven Wrapper 启动即可。

## 常见问题

### 1. 后端启动失败，提示数据库连接失败

检查 MySQL 是否已启动，并确认账号密码是否与配置一致：

```properties
spring.datasource.username=root
spring.datasource.password=123456
```

### 2. 前端页面没有数据

确认后端是否已启动：

```text
http://localhost:8080/api/dict
```

如果后端未启动，前端会无法获取列表和下拉数据。

### 3. 后端启动提示旧表字段问题

项目已内置表结构兼容迁移。请确认已经重新编译并启动最新代码，避免运行旧的 `target/classes`。

必要时可以删除旧库后重新启动：

```sql
DROP DATABASE travel_reimbursement;
```

然后重新启动后端，系统会自动创建数据库和表。
