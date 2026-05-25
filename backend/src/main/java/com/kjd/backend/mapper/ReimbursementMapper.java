package com.kjd.backend.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kjd.backend.dto.ReimbursementQueryDTO;
import com.kjd.backend.entity.Allocation;
import com.kjd.backend.entity.CalendarDay;
import com.kjd.backend.entity.Reimbursement;
import com.kjd.backend.entity.Subsidy;
import com.kjd.backend.entity.Trip;
import com.kjd.backend.vo.PageResultVO;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class ReimbursementMapper {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public ReimbursementMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        createTables();
        migrateTables();
        Integer count = jdbcTemplate.queryForObject("select count(1) from fk_reim_main", Integer.class);
        if (count != null && count == 0) {
            seed();
        }
    }

    private void createTables() {
        jdbcTemplate.execute("""
                create table if not exists fk_reim_main (
                  id varchar(32) primary key,
                  creation_time varchar(32),
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
                  subsidy_total varchar(20),
                  meal_allowance varchar(20),
                  transportation_allowance varchar(20),
                  phone_allowance varchar(20),
                  remarks varchar(1000),
                  status int,
                  reim_no varchar(32),
                  submit_date varchar(20),
                  allocations_json text
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists fk_reim_itinerary (
                  id varchar(32) primary key,
                  main_id varchar(32) not null,
                  traveler_id varchar(20),
                  traveler_no varchar(32),
                  traveler_name varchar(20),
                  departure_date varchar(20),
                  arrival_date varchar(20),
                  departure_city varchar(20),
                  departure_city_no varchar(20),
                  arriving_city varchar(20),
                  arriving_city_no varchar(20),
                  itinerary_instructions varchar(500)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists fk_reim_subsidy (
                  id varchar(32) primary key,
                  main_id varchar(32) not null,
                  traveler_id varchar(20),
                  traveler_no varchar(20),
                  traveler_name varchar(20),
                  departure_date varchar(20),
                  arrival_date varchar(20),
                  subsidy_days varchar(20),
                  departure_city varchar(20),
                  departure_city_no varchar(20),
                  arriving_city varchar(20),
                  arriving_city_no varchar(20),
                  application_amount varchar(20),
                  subsidy_amount varchar(20),
                  meal_allowance varchar(20),
                  transportation_allowance varchar(20),
                  phone_allowance varchar(20),
                  business_type_id varchar(32),
                  business_type_no varchar(20),
                  business_type_name varchar(40),
                  trip_id varchar(32)
                )
                """);
        jdbcTemplate.execute("""
                create table if not exists fk_subsidy_calendar (
                  id varchar(32) primary key,
                  main_id varchar(32) not null,
                  travel_date varchar(20) not null,
                  travel_date_week varchar(32),
                  subsidized_cities varchar(32),
                  subsidized_city_number varchar(32),
                  remark varchar(100),
                  standard_meal_expenses_amount varchar(32),
                  standard_traffic_amount varchar(32),
                  standard_communication_amount varchar(32),
                  meal_expenses_amount varchar(32),
                  traffic_amount varchar(32),
                  communication_amount varchar(32),
                  is_reimbursed varchar(32)
                )
                """);
    }

    private void migrateTables() {
        addMainColumns();
        addItineraryColumns();
        addSubsidyColumns();
        addCalendarColumns();
        dropUniqueIndex("fk_reim_main", "reim_no");
        modifyColumn("fk_reim_main", "reim_no", "varchar(32) null");
        relaxLegacyRequiredColumns("fk_reim_main");
        relaxLegacyRequiredColumns("fk_reim_itinerary");
        relaxLegacyRequiredColumns("fk_reim_subsidy");
        relaxLegacyRequiredColumns("fk_subsidy_calendar");
        if (columnExists("fk_reim_main", "reimbursement_no")) {
            jdbcTemplate.execute("update fk_reim_main set reim_no = reimbursement_no where (reim_no = '' or reim_no is null) and reimbursement_no is not null and reimbursement_no <> ''");
        }
        jdbcTemplate.execute("update fk_reim_main set reim_no = id where reim_no = '' or reim_no is null");
    }

    private void addMainColumns() {
        addColumn("fk_reim_main", "creation_time", "varchar(32)");
        addColumn("fk_reim_main", "reimbursement_title", "varchar(500)");
        addColumn("fk_reim_main", "reimburser_id", "varchar(32)");
        addColumn("fk_reim_main", "reimburser_no", "varchar(20)");
        addColumn("fk_reim_main", "reimburser_name", "varchar(20)");
        addColumn("fk_reim_main", "reim_department_id", "varchar(32)");
        addColumn("fk_reim_main", "reim_department_no", "varchar(20)");
        addColumn("fk_reim_main", "reim_department_name", "varchar(40)");
        addColumn("fk_reim_main", "reim_company_id", "varchar(32)");
        addColumn("fk_reim_main", "reim_company_no", "varchar(20)");
        addColumn("fk_reim_main", "reim_company_name", "varchar(40)");
        addColumn("fk_reim_main", "business_type_id", "varchar(32)");
        addColumn("fk_reim_main", "business_type_no", "varchar(20)");
        addColumn("fk_reim_main", "business_type_name", "varchar(40)");
        addColumn("fk_reim_main", "business_trip_reason", "varchar(500)");
        addColumn("fk_reim_main", "subsidy_total", "varchar(20)");
        addColumn("fk_reim_main", "meal_allowance", "varchar(20)");
        addColumn("fk_reim_main", "transportation_allowance", "varchar(20)");
        addColumn("fk_reim_main", "phone_allowance", "varchar(20)");
        addColumn("fk_reim_main", "remarks", "varchar(1000)");
        addColumn("fk_reim_main", "status", "int");
        addColumn("fk_reim_main", "reim_no", "varchar(32)");
        addColumn("fk_reim_main", "submit_date", "varchar(20)");
        addColumn("fk_reim_main", "allocations_json", "text");
    }

    private void addItineraryColumns() {
        addColumn("fk_reim_itinerary", "main_id", "varchar(32)");
        addColumn("fk_reim_itinerary", "traveler_id", "varchar(20)");
        addColumn("fk_reim_itinerary", "traveler_no", "varchar(32)");
        addColumn("fk_reim_itinerary", "traveler_name", "varchar(20)");
        addColumn("fk_reim_itinerary", "departure_date", "varchar(20)");
        addColumn("fk_reim_itinerary", "arrival_date", "varchar(20)");
        addColumn("fk_reim_itinerary", "departure_city", "varchar(20)");
        addColumn("fk_reim_itinerary", "departure_city_no", "varchar(20)");
        addColumn("fk_reim_itinerary", "arriving_city", "varchar(20)");
        addColumn("fk_reim_itinerary", "arriving_city_no", "varchar(20)");
        addColumn("fk_reim_itinerary", "itinerary_instructions", "varchar(500)");
    }

    private void addSubsidyColumns() {
        addColumn("fk_reim_subsidy", "main_id", "varchar(32)");
        addColumn("fk_reim_subsidy", "traveler_id", "varchar(20)");
        addColumn("fk_reim_subsidy", "traveler_no", "varchar(20)");
        addColumn("fk_reim_subsidy", "traveler_name", "varchar(20)");
        addColumn("fk_reim_subsidy", "departure_date", "varchar(20)");
        addColumn("fk_reim_subsidy", "arrival_date", "varchar(20)");
        addColumn("fk_reim_subsidy", "subsidy_days", "varchar(20)");
        addColumn("fk_reim_subsidy", "departure_city", "varchar(20)");
        addColumn("fk_reim_subsidy", "departure_city_no", "varchar(20)");
        addColumn("fk_reim_subsidy", "arriving_city", "varchar(20)");
        addColumn("fk_reim_subsidy", "arriving_city_no", "varchar(20)");
        addColumn("fk_reim_subsidy", "application_amount", "varchar(20)");
        addColumn("fk_reim_subsidy", "subsidy_amount", "varchar(20)");
        addColumn("fk_reim_subsidy", "meal_allowance", "varchar(20)");
        addColumn("fk_reim_subsidy", "transportation_allowance", "varchar(20)");
        addColumn("fk_reim_subsidy", "phone_allowance", "varchar(20)");
        addColumn("fk_reim_subsidy", "business_type_id", "varchar(32)");
        addColumn("fk_reim_subsidy", "business_type_no", "varchar(20)");
        addColumn("fk_reim_subsidy", "business_type_name", "varchar(40)");
        addColumn("fk_reim_subsidy", "trip_id", "varchar(32)");
    }

    private void addCalendarColumns() {
        addColumn("fk_subsidy_calendar", "main_id", "varchar(32)");
        addColumn("fk_subsidy_calendar", "travel_date", "varchar(20)");
        addColumn("fk_subsidy_calendar", "travel_date_week", "varchar(32)");
        addColumn("fk_subsidy_calendar", "subsidized_cities", "varchar(32)");
        addColumn("fk_subsidy_calendar", "subsidized_city_number", "varchar(32)");
        addColumn("fk_subsidy_calendar", "remark", "varchar(100)");
        addColumn("fk_subsidy_calendar", "standard_meal_expenses_amount", "varchar(32)");
        addColumn("fk_subsidy_calendar", "standard_traffic_amount", "varchar(32)");
        addColumn("fk_subsidy_calendar", "standard_communication_amount", "varchar(32)");
        addColumn("fk_subsidy_calendar", "meal_expenses_amount", "varchar(32)");
        addColumn("fk_subsidy_calendar", "traffic_amount", "varchar(32)");
        addColumn("fk_subsidy_calendar", "communication_amount", "varchar(32)");
        addColumn("fk_subsidy_calendar", "is_reimbursed", "varchar(32)");
    }

    public PageResultVO<Reimbursement> page(ReimbursementQueryDTO query) {
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" where 1=1");
        like(where, args, "reim_no", query.reimbursementNo);
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
        List<Reimbursement> records = jdbcTemplate.query(
                "select * from fk_reim_main" + where + " order by creation_time desc limit ?,?",
                mainRowMapper(false),
                args.toArray()
        );
        return new PageResultVO<>(total == null ? 0 : total, page, size, records);
    }

    public Reimbursement find(String id) {
        List<Reimbursement> list = jdbcTemplate.query("select * from fk_reim_main where id=?", mainRowMapper(true), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Reimbursement save(Reimbursement item) {
        boolean exists = StringUtils.hasText(item.id) && find(item.id) != null;
        if (!StringUtils.hasText(item.id)) item.id = uuid();
        if (!StringUtils.hasText(item.reimbursementNo)) item.reimbursementNo = nextNo();
        if (item.creationTime == null) item.creationTime = LocalDateTime.now();
        if (item.submitDate == null) item.submitDate = LocalDate.now();
        item.statusName = statusName(item.status);
        if (exists) updateMain(item); else insertMain(item);
        rebuildDetails(item);
        return find(item.id);
    }

    public void updateStatus(String id, int status) {
        jdbcTemplate.update("update fk_reim_main set status=? where id=?", status, id);
    }

    private void insertMain(Reimbursement item) {
        jdbcTemplate.update("""
                insert into fk_reim_main (id,reim_no,creation_time,submit_date,status,reimbursement_title,
                reimburser_id,reimburser_no,reimburser_name,reim_department_id,reim_department_no,reim_department_name,
                reim_company_id,reim_company_no,reim_company_name,business_type_id,business_type_no,business_type_name,
                business_trip_reason,subsidy_total,meal_allowance,transportation_allowance,phone_allowance,remarks,allocations_json)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, mainValues(item));
    }

    private void updateMain(Reimbursement item) {
        Object[] values = mainValues(item);
        Object[] updateValues = new Object[values.length];
        System.arraycopy(values, 1, updateValues, 0, values.length - 1);
        updateValues[updateValues.length - 1] = item.id;
        jdbcTemplate.update("""
                update fk_reim_main set reim_no=?,creation_time=?,submit_date=?,status=?,reimbursement_title=?,
                reimburser_id=?,reimburser_no=?,reimburser_name=?,reim_department_id=?,reim_department_no=?,reim_department_name=?,
                reim_company_id=?,reim_company_no=?,reim_company_name=?,business_type_id=?,business_type_no=?,business_type_name=?,
                business_trip_reason=?,subsidy_total=?,meal_allowance=?,transportation_allowance=?,phone_allowance=?,remarks=?,
                allocations_json=? where id=?
                """, updateValues);
    }

    private Object[] mainValues(Reimbursement item) {
        return new Object[]{
                item.id, item.reimbursementNo, text(item.creationTime), text(item.submitDate), item.status, item.reimbursementTitle,
                item.reimburserId, item.reimburserNo, item.reimburserName, item.reimDepartmentId, item.reimDepartmentNo,
                item.reimDepartmentName, item.reimCompanyId, item.reimCompanyNo, item.reimCompanyName, item.businessTypeId,
                item.businessTypeNo, item.businessTypeName, item.businessTripReason, moneyText(item.subsidyTotal),
                moneyText(item.mealAllowance), moneyText(item.transportationAllowance), moneyText(item.phoneAllowance),
                item.remarks, json(item.allocations)
        };
    }

    private void rebuildDetails(Reimbursement item) {
        jdbcTemplate.update("delete from fk_subsidy_calendar where main_id in (select id from fk_reim_subsidy where main_id=?)", item.id);
        jdbcTemplate.update("delete from fk_reim_subsidy where main_id=?", item.id);
        jdbcTemplate.update("delete from fk_reim_itinerary where main_id=?", item.id);
        for (Trip trip : item.trips) {
            if (!StringUtils.hasText(trip.id)) trip.id = uuid();
            insertTrip(item.id, trip);
        }
        for (Subsidy subsidy : item.subsidies) {
            if (!StringUtils.hasText(subsidy.id)) subsidy.id = uuid();
            insertSubsidy(item, subsidy);
            for (CalendarDay day : subsidy.calendar) {
                insertCalendar(subsidy, day);
            }
        }
    }

    private void insertTrip(String mainId, Trip trip) {
        jdbcTemplate.update("""
                insert into fk_reim_itinerary (id,main_id,traveler_id,traveler_no,traveler_name,departure_date,arrival_date,
                departure_city,departure_city_no,arriving_city,arriving_city_no,itinerary_instructions)
                values (?,?,?,?,?,?,?,?,?,?,?,?)
                """, trip.id, mainId, trip.travelerId, trip.travelerNo, trip.travelerName, text(trip.startDate), text(trip.endDate),
                trip.fromCityName, trip.fromCityNo, trip.toCityName, trip.toCityNo, trip.tripDescription);
    }

    private void insertSubsidy(Reimbursement item, Subsidy subsidy) {
        Trip trip = item.trips.stream().filter(row -> row.id.equals(subsidy.tripId)).findFirst().orElse(null);
        BigDecimal meal = BigDecimal.ZERO;
        BigDecimal traffic = BigDecimal.ZERO;
        BigDecimal phone = BigDecimal.ZERO;
        for (CalendarDay day : subsidy.calendar) {
            meal = meal.add(money(day.mealAmount));
            traffic = traffic.add(money(day.transportAmount));
            phone = phone.add(money(day.phoneAmount));
        }
        jdbcTemplate.update("""
                insert into fk_reim_subsidy (id,main_id,traveler_id,traveler_no,traveler_name,departure_date,arrival_date,
                subsidy_days,departure_city,departure_city_no,arriving_city,arriving_city_no,application_amount,subsidy_amount,
                meal_allowance,transportation_allowance,phone_allowance,business_type_id,business_type_no,business_type_name,trip_id)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, subsidy.id, item.id, subsidy.travelerId, trip == null ? "" : trip.travelerNo, subsidy.travelerName,
                trip == null ? "" : text(trip.startDate), trip == null ? "" : text(trip.endDate), String.valueOf(subsidy.days),
                trip == null ? "" : trip.fromCityName, trip == null ? "" : trip.fromCityNo,
                trip == null ? "" : trip.toCityName, trip == null ? "" : trip.toCityNo,
                moneyText(subsidy.applyAmount), moneyText(subsidy.subsidyAmount), moneyText(meal), moneyText(traffic), moneyText(phone),
                item.businessTypeId, item.businessTypeNo, item.businessTypeName, subsidy.tripId);
    }

    private void insertCalendar(Subsidy subsidy, CalendarDay day) {
        jdbcTemplate.update("""
                insert into fk_subsidy_calendar (id,main_id,travel_date,travel_date_week,subsidized_cities,subsidized_city_number,
                remark,standard_meal_expenses_amount,standard_traffic_amount,standard_communication_amount,
                meal_expenses_amount,traffic_amount,communication_amount,is_reimbursed)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, uuid(), subsidy.id, text(day.tripDate), day.weekName, day.subsidyCity, "", selectedRemark(day),
                moneyText(day.mealStandard), moneyText(day.transportStandard), moneyText(day.phoneStandard),
                moneyText(day.mealAmount), moneyText(day.transportAmount), moneyText(day.phoneAmount), selectedRemark(day));
    }

    private RowMapper<Reimbursement> mainRowMapper(boolean loadDetails) {
        return (rs, rowNum) -> {
            Reimbursement item = new Reimbursement();
            item.id = rs.getString("id");
            item.reimbursementNo = rs.getString("reim_no");
            item.creationTime = parseDateTime(rs.getString("creation_time"));
            item.submitDate = parseDate(rs.getString("submit_date"));
            item.status = rs.getObject("status") == null ? 0 : rs.getInt("status");
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
            item.subsidyTotal = decimal(rs.getString("subsidy_total"));
            item.mealAllowance = decimal(rs.getString("meal_allowance"));
            item.transportationAllowance = decimal(rs.getString("transportation_allowance"));
            item.phoneAllowance = decimal(rs.getString("phone_allowance"));
            item.remarks = rs.getString("remarks");
            item.allocations = readList(rs.getString("allocations_json"), Allocation.class);
            if (loadDetails) {
                item.trips = loadTrips(item.id);
                item.subsidies = loadSubsidies(item.id);
            }
            return item;
        };
    }

    private List<Trip> loadTrips(String mainId) {
        return jdbcTemplate.query("select * from fk_reim_itinerary where main_id=? order by departure_date,id", (rs, rowNum) -> {
            Trip trip = new Trip();
            trip.id = rs.getString("id");
            trip.travelerId = rs.getString("traveler_id");
            trip.travelerNo = rs.getString("traveler_no");
            trip.travelerName = rs.getString("traveler_name");
            trip.startDate = parseDate(rs.getString("departure_date"));
            trip.endDate = parseDate(rs.getString("arrival_date"));
            trip.fromCityName = rs.getString("departure_city");
            trip.fromCityNo = rs.getString("departure_city_no");
            trip.toCityName = rs.getString("arriving_city");
            trip.toCityNo = rs.getString("arriving_city_no");
            trip.tripDescription = rs.getString("itinerary_instructions");
            return trip;
        }, mainId);
    }

    private List<Subsidy> loadSubsidies(String mainId) {
        return jdbcTemplate.query("select * from fk_reim_subsidy where main_id=? order by departure_date,id", (rs, rowNum) -> {
            Subsidy subsidy = new Subsidy();
            subsidy.id = rs.getString("id");
            subsidy.tripId = rs.getString("trip_id");
            subsidy.travelerId = rs.getString("traveler_id");
            subsidy.travelerName = rs.getString("traveler_name");
            subsidy.tripDateRange = rs.getString("departure_date") + " 至 " + rs.getString("arrival_date");
            subsidy.days = parseInt(rs.getString("subsidy_days"));
            subsidy.route = rs.getString("departure_city") + "-" + rs.getString("arriving_city");
            subsidy.subsidyCity = rs.getString("arriving_city");
            subsidy.applyAmount = decimal(rs.getString("application_amount"));
            subsidy.subsidyAmount = decimal(rs.getString("subsidy_amount"));
            subsidy.calendar = loadCalendar(subsidy.id);
            return subsidy;
        }, mainId);
    }

    private List<CalendarDay> loadCalendar(String subsidyId) {
        return jdbcTemplate.query("select * from fk_subsidy_calendar where main_id=? order by travel_date", (rs, rowNum) -> {
            CalendarDay day = new CalendarDay();
            day.tripDate = parseDate(rs.getString("travel_date"));
            day.weekName = rs.getString("travel_date_week");
            day.subsidyCity = rs.getString("subsidized_cities");
            day.mealStandard = decimal(rs.getString("standard_meal_expenses_amount"));
            day.transportStandard = decimal(rs.getString("standard_traffic_amount"));
            day.phoneStandard = decimal(rs.getString("standard_communication_amount"));
            day.mealAmount = decimal(rs.getString("meal_expenses_amount"));
            day.transportAmount = decimal(rs.getString("traffic_amount"));
            day.phoneAmount = decimal(rs.getString("communication_amount"));
            String selected = rs.getString("is_reimbursed");
            day.mealSelected = selected != null && selected.contains("meal");
            day.transportSelected = selected != null && selected.contains("transport");
            day.phoneSelected = selected != null && selected.contains("phone");
            return day;
        }, subsidyId);
    }

    private void addColumn(String table, String column, String definition) {
        if (!columnExists(table, column)) {
            jdbcTemplate.execute("alter table " + table + " add column `" + column + "` " + definition);
        }
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """, Integer.class, table, column);
        return count != null && count > 0;
    }

    private void modifyColumn(String table, String column, String definition) {
        Integer count = jdbcTemplate.queryForObject("""
                select count(1)
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                """, Integer.class, table, column);
        if (count != null && count > 0) {
            jdbcTemplate.execute("alter table " + table + " modify column `" + column + "` " + definition);
        }
    }

    private void dropUniqueIndex(String table, String column) {
        List<String> indexes = jdbcTemplate.queryForList("""
                select distinct index_name
                from information_schema.statistics
                where table_schema = database()
                  and table_name = ?
                  and column_name = ?
                  and non_unique = 0
                  and index_name <> 'PRIMARY'
                """, String.class, table, column);
        for (String index : indexes) {
            jdbcTemplate.execute("alter table " + table + " drop index `" + index + "`");
        }
    }

    private void relaxLegacyRequiredColumns(String table) {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList("""
                select column_name, column_type
                from information_schema.columns
                where table_schema = database()
                  and table_name = ?
                  and is_nullable = 'NO'
                  and column_default is null
                  and extra not like '%auto_increment%'
                  and column_name <> 'id'
                """, table);
        for (Map<String, Object> column : columns) {
            String name = String.valueOf(column.get("column_name"));
            String type = String.valueOf(column.get("column_type"));
            jdbcTemplate.execute("alter table " + table + " modify column `" + name + "` " + type + " null");
        }
    }

    private void seed() {
        for (int i = 0; i < 10; i++) {
            Reimbursement item = new Reimbursement();
            item.id = uuid();
            item.reimbursementNo = "RCBX202605" + String.format("%04d", 15002 - i * 37);
            item.creationTime = LocalDateTime.now().minusDays(i + 3L);
            item.submitDate = LocalDate.now().minusDays(i + 2L);
            item.status = i == 8 || i == 9 ? 2 : (i == 4 || i == 5 || i == 6 || i == 7 ? 0 : 1);
            item.reimbursementTitle = i < 4 ? "日常报销单模板 - 副本，" + (40 + i * 83) + ".00CNY，..." : (i < 7 ? "测试" : "");
            item.businessTripReason = i == 7 || i == 9 ? "这个法人公司的名字可能会有点长是我..." : "";
            item.reimburserId = "13AB3A3F72409002";
            item.reimburserNo = "202101497";
            item.reimburserName = "徐年年";
            item.reimDepartmentId = "13C7E2BAE0393001";
            item.reimDepartmentNo = "072006";
            item.reimDepartmentName = "运营事业部";
            item.reimCompanyId = "1C54557F1782E000";
            item.reimCompanyName = "胜意科技北京分公司";
            item.businessTypeId = "1B5FEB7DD4396000";
            item.businessTypeName = i == 7 || i == 9 ? "项目出差" : "项目出差";
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

    private String json(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> List<T> readList(String json, Class<T> type) {
        if (!StringUtils.hasText(json)) return new ArrayList<>();
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, type));
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }

    private static BigDecimal decimal(String value) {
        if (!StringUtils.hasText(value)) return BigDecimal.ZERO;
        return new BigDecimal(value);
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String moneyText(BigDecimal value) {
        return money(value).toPlainString();
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) return null;
        return LocalDate.parse(value.length() > 10 ? value.substring(0, 10) : value);
    }

    private static LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) return null;
        if (value.length() == 10) return LocalDate.parse(value).atStartOfDay();
        return LocalDateTime.parse(value.replace(' ', 'T'));
    }

    private static int parseInt(String value) {
        if (!StringUtils.hasText(value)) return 0;
        return Integer.parseInt(value);
    }

    private static String selectedRemark(CalendarDay day) {
        List<String> selected = new ArrayList<>();
        if (day.mealSelected) selected.add("meal");
        if (day.transportSelected) selected.add("transport");
        if (day.phoneSelected) selected.add("phone");
        return String.join(",", selected);
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
