package com.kjd.backend.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kjd.backend.model.ReimbursementModels.Allocation;
import com.kjd.backend.model.ReimbursementModels.Main;
import com.kjd.backend.model.ReimbursementModels.PageResult;
import com.kjd.backend.model.ReimbursementModels.Query;
import com.kjd.backend.model.ReimbursementModels.Subsidy;
import com.kjd.backend.model.ReimbursementModels.Trip;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ReimbursementRepository {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ReimbursementRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("""
                create table if not exists fk_reim_main (
                  id varchar(32) primary key,
                  reimbursement_no varchar(32) not null,
                  creation_time datetime,
                  submit_date date,
                  status int not null default 0,
                  reimbursement_title varchar(500),
                  reimburser_id varchar(32),
                  reimburser_no varchar(20),
                  reimburser_name varchar(20),
                  reim_department_id varchar(32),
                  reim_department_no varchar(20),
                  reim_department_name varchar(40),
                  reim_company_id varchar(32),
                  reim_company_no varchar(20),
                  reim_company_name varchar(40),
                  business_type_id varchar(32),
                  business_type_no varchar(20),
                  business_type_name varchar(40),
                  business_trip_reason varchar(500),
                  subsidy_total decimal(18,2),
                  meal_allowance decimal(18,2),
                  transportation_allowance decimal(18,2),
                  phone_allowance decimal(18,2),
                  remarks varchar(1000),
                  trips_json text,
                  subsidies_json text,
                  allocations_json text
                )
                """);
        migrateColumns();
        Integer count = jdbcTemplate.queryForObject("select count(1) from fk_reim_main", Integer.class);
        if (count != null && count == 0) {
            seed();
        }
    }

    private void migrateColumns() {
        addColumn("creation_time", "datetime");
        addColumn("reimbursement_no", "varchar(32) not null default ''");
        addColumn("submit_date", "date");
        addColumn("status", "int not null default 0");
        addColumn("reimbursement_title", "varchar(500)");
        addColumn("reimburser_id", "varchar(32)");
        addColumn("reimburser_no", "varchar(20)");
        addColumn("reimburser_name", "varchar(20)");
        addColumn("reim_department_id", "varchar(32)");
        addColumn("reim_department_no", "varchar(20)");
        addColumn("reim_department_name", "varchar(40)");
        addColumn("reim_company_id", "varchar(32)");
        addColumn("reim_company_no", "varchar(20)");
        addColumn("reim_company_name", "varchar(40)");
        addColumn("business_type_id", "varchar(32)");
        addColumn("business_type_no", "varchar(20)");
        addColumn("business_type_name", "varchar(40)");
        addColumn("business_trip_reason", "varchar(500)");
        addColumn("subsidy_total", "decimal(18,2) default 0");
        addColumn("meal_allowance", "decimal(18,2) default 0");
        addColumn("transportation_allowance", "decimal(18,2) default 0");
        addColumn("phone_allowance", "decimal(18,2) default 0");
        addColumn("remarks", "varchar(1000)");
        addColumn("trips_json", "text");
        addColumn("subsidies_json", "text");
        addColumn("allocations_json", "text");
        addColumn("reim_no", "varchar(32) default ''");
        modifyColumn("reimbursement_no", "varchar(32) not null default ''");
        modifyColumn("reim_no", "varchar(32) default ''");
        modifyColumn("reimbursement_title", "varchar(500)");
        modifyColumn("reim_department_id", "varchar(32)");
        modifyColumn("reim_department_name", "varchar(40)");
        modifyColumn("reim_company_id", "varchar(32)");
        modifyColumn("reim_company_name", "varchar(40)");
        modifyColumn("business_type_id", "varchar(32)");
        modifyColumn("business_type_name", "varchar(40)");
        modifyColumn("business_trip_reason", "varchar(500)");
        modifyColumn("remarks", "varchar(1000)");
        relaxLegacyRequiredColumns();
        jdbcTemplate.execute("update fk_reim_main set reimbursement_no = id where reimbursement_no = '' or reimbursement_no is null");
    }

    private void addColumn(String column, String definition) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'fk_reim_main'
                  and column_name = ?
                """, Integer.class, column);
        if (count == null || count == 0) {
            jdbcTemplate.execute("alter table fk_reim_main add column " + column + " " + definition);
        }
    }

    private void modifyColumn(String column, String definition) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'fk_reim_main'
                  and column_name = ?
                """, Integer.class, column);
        if (count != null && count > 0) {
            jdbcTemplate.execute("alter table fk_reim_main modify column " + column + " " + definition);
        }
    }

    private void relaxLegacyRequiredColumns() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList("""
                select column_name, column_type
                from information_schema.columns
                where table_schema = database()
                  and table_name = 'fk_reim_main'
                  and is_nullable = 'NO'
                  and column_default is null
                  and extra not like '%auto_increment%'
                  and column_name <> 'id'
                """);
        for (Map<String, Object> column : columns) {
            String name = String.valueOf(column.get("column_name"));
            String type = String.valueOf(column.get("column_type"));
            jdbcTemplate.execute("alter table fk_reim_main modify column `" + name + "` " + type + " null");
        }
    }

    public PageResult<Main> page(Query query) {
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where 1=1");
        like(where, args, "reimbursement_no", query.reimbursementNo);
        like(where, args, "reimbursement_title", query.reimbursementTitle);
        like(where, args, "business_trip_reason", query.businessTripReason);
        eq(where, args, "reim_company_id", query.reimCompanyId);
        eq(where, args, "reim_department_id", query.reimDepartmentId);
        eq(where, args, "reimburser_id", query.reimburserId);
        eq(where, args, "business_type_id", query.businessTypeId);

        Long total = jdbcTemplate.queryForObject("select count(1) from fk_reim_main" + where, Long.class, args.toArray());
        int page = Math.max(query.page, 1);
        int size = Math.max(query.size, 1);
        args.add((page - 1) * size);
        args.add(size);
        List<Main> records = jdbcTemplate.query("select * from fk_reim_main" + where + " order by creation_time desc limit ?,?",
                mapper(), args.toArray());
        return new PageResult<>(total == null ? 0 : total, page, size, records);
    }

    public Main find(String id) {
        List<Main> list = jdbcTemplate.query("select * from fk_reim_main where id=?", mapper(), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Main save(Main item) {
        boolean exists = StringUtils.hasText(item.id) && find(item.id) != null;
        if (!StringUtils.hasText(item.id)) item.id = uuid();
        if (!StringUtils.hasText(item.reimbursementNo)) item.reimbursementNo = nextNo();
        if (item.creationTime == null) item.creationTime = LocalDateTime.now();
        if (item.submitDate == null) item.submitDate = LocalDate.now();
        item.statusName = statusName(item.status);
        if (exists) update(item); else insert(item);
        return find(item.id);
    }

    public void deleteStatus(String id, int status) {
        jdbcTemplate.update("update fk_reim_main set status=? where id=?", status, id);
    }

    private void insert(Main item) {
        jdbcTemplate.update("""
                insert into fk_reim_main (id,reimbursement_no,creation_time,submit_date,status,reimbursement_title,
                reimburser_id,reimburser_no,reimburser_name,reim_department_id,reim_department_no,reim_department_name,
                reim_company_id,reim_company_no,reim_company_name,business_type_id,business_type_no,business_type_name,
                business_trip_reason,subsidy_total,meal_allowance,transportation_allowance,phone_allowance,remarks,
                trips_json,subsidies_json,allocations_json)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, values(item));
    }

    private void update(Main item) {
        Object[] values = values(item);
        Object[] updateValues = new Object[values.length];
        System.arraycopy(values, 1, updateValues, 0, values.length - 1);
        updateValues[updateValues.length - 1] = item.id;
        jdbcTemplate.update("""
                update fk_reim_main set reimbursement_no=?,creation_time=?,submit_date=?,status=?,reimbursement_title=?,
                reimburser_id=?,reimburser_no=?,reimburser_name=?,reim_department_id=?,reim_department_no=?,reim_department_name=?,
                reim_company_id=?,reim_company_no=?,reim_company_name=?,business_type_id=?,business_type_no=?,business_type_name=?,
                business_trip_reason=?,subsidy_total=?,meal_allowance=?,transportation_allowance=?,phone_allowance=?,remarks=?,
                trips_json=?,subsidies_json=?,allocations_json=? where id=?
                """, updateValues);
    }

    private Object[] values(Main item) {
        return new Object[]{
                item.id, item.reimbursementNo, item.creationTime, item.submitDate, item.status, item.reimbursementTitle,
                item.reimburserId, item.reimburserNo, item.reimburserName, item.reimDepartmentId, item.reimDepartmentNo,
                item.reimDepartmentName, item.reimCompanyId, item.reimCompanyNo, item.reimCompanyName, item.businessTypeId,
                item.businessTypeNo, item.businessTypeName, item.businessTripReason, money(item.subsidyTotal),
                money(item.mealAllowance), money(item.transportationAllowance), money(item.phoneAllowance), item.remarks,
                json(item.trips), json(item.subsidies), json(item.allocations)
        };
    }

    private RowMapper<Main> mapper() {
        return (rs, rowNum) -> {
            Main item = new Main();
            item.id = rs.getString("id");
            item.reimbursementNo = rs.getString("reimbursement_no");
            Timestamp creation = rs.getTimestamp("creation_time");
            item.creationTime = creation == null ? null : creation.toLocalDateTime();
            item.submitDate = date(rs, "submit_date");
            item.status = rs.getInt("status");
            item.statusName = statusName(item.status);
            item.reimbursementTitle = rs.getString("reimbursement_title");
            item.reimburserId = rs.getString("reimburser_id");
            item.reimburserNo = rs.getString("reimburser_no");
            item.reimburserName = rs.getString("reimburser_name");
            item.reimDepartmentId = rs.getString("reim_department_id");
            item.reimDepartmentNo = rs.getString("reim_department_no");
            item.reimDepartmentName = rs.getString("reim_department_name");
            item.reimCompanyId = rs.getString("reim_company_id");
            item.reimCompanyNo = rs.getString("reim_company_no");
            item.reimCompanyName = rs.getString("reim_company_name");
            item.businessTypeId = rs.getString("business_type_id");
            item.businessTypeNo = rs.getString("business_type_no");
            item.businessTypeName = rs.getString("business_type_name");
            item.businessTripReason = rs.getString("business_trip_reason");
            item.subsidyTotal = money(rs.getBigDecimal("subsidy_total"));
            item.mealAllowance = money(rs.getBigDecimal("meal_allowance"));
            item.transportationAllowance = money(rs.getBigDecimal("transportation_allowance"));
            item.phoneAllowance = money(rs.getBigDecimal("phone_allowance"));
            item.remarks = rs.getString("remarks");
            item.trips = readList(rs, "trips_json", Trip.class);
            item.subsidies = readList(rs, "subsidies_json", Subsidy.class);
            item.allocations = readList(rs, "allocations_json", Allocation.class);
            return item;
        };
    }

    private void seed() {
        String[] statuses = {"审批中", "审批通过", "草稿", "已作废"};
        for (int i = 0; i < 10; i++) {
            Main item = new Main();
            item.id = uuid();
            item.reimbursementNo = "RCBX202605" + String.format("%04d", 15002 - i * 37);
            item.creationTime = LocalDateTime.now().minusDays(i + 3L);
            item.submitDate = LocalDate.now().minusDays(i + 2L);
            item.status = i == 8 || i == 9 ? 2 : (i == 4 || i == 5 || i == 6 || i == 7 ? 0 : 1);
            item.statusName = statuses[Math.min(item.status, statuses.length - 1)];
            item.reimbursementTitle = i < 4 ? "日常报销单模板 - 副本，" + (40 + i * 83) + ".00CNY，..." : (i < 7 ? "测试" : "");
            item.businessTripReason = i == 7 || i == 9 ? "这个法人公司的名字可能会有点长是我..." : "";
            item.reimburserId = "13AB3A3F72409002";
            item.reimburserNo = "202101497";
            item.reimburserName = "徐年年";
            item.reimDepartmentId = "13AB8D7B52A9B002";
            item.reimDepartmentNo = "CS001";
            item.reimDepartmentName = "测试部";
            item.reimCompanyId = "1C54557F1782E000";
            item.reimCompanyName = "初始化测试";
            item.businessTypeId = "1B5FEB7DD4396000";
            item.businessTypeName = i == 7 || i == 9 ? "客户招待" : "日常办公";
            item.subsidyTotal = BigDecimal.ZERO;
            save(item);
        }
    }

    private static void like(StringBuilder where, List<Object> args, String column, String value) {
        if (StringUtils.hasText(value)) {
            where.append(" and ").append(column).append(" like ?");
            args.add("%" + value.trim() + "%");
        }
    }

    private static void eq(StringBuilder where, List<Object> args, String column, String value) {
        if (StringUtils.hasText(value)) {
            where.append(" and ").append(column).append("=?");
            args.add(value);
        }
    }

    private static LocalDate date(ResultSet rs, String column) throws SQLException {
        java.sql.Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    private String json(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> List<T> readList(ResultSet rs, String column, Class<T> type) throws SQLException {
        String json = rs.getString(column);
        if (!StringUtils.hasText(json)) return new ArrayList<>();
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String statusName(Integer status) {
        if (status == null || status == 0) return "草稿";
        if (status == 1) return "已完成";
        if (status == 2) return "已作废";
        return "未知";
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String nextNo() {
        return "CLBX" + LocalDate.now().toString().replace("-", "") + System.currentTimeMillis() % 100000;
    }
}
