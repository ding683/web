package com.kjd.backend.service;

import com.kjd.backend.model.DictionaryItem;
import com.kjd.backend.model.ReimbursementModels.Allocation;
import com.kjd.backend.model.ReimbursementModels.CalendarDay;
import com.kjd.backend.model.ReimbursementModels.Main;
import com.kjd.backend.model.ReimbursementModels.PageResult;
import com.kjd.backend.model.ReimbursementModels.Query;
import com.kjd.backend.model.ReimbursementModels.Subsidy;
import com.kjd.backend.model.ReimbursementModels.Trip;
import com.kjd.backend.repository.ReimbursementRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ReimbursementService {
    private final ReimbursementRepository repository;
    private final DictionaryService dictionaries;

    public ReimbursementService(ReimbursementRepository repository, DictionaryService dictionaries) {
        this.repository = repository;
        this.dictionaries = dictionaries;
    }

    public PageResult<Main> page(Query query) {
        return repository.page(query);
    }

    public Main find(String id) {
        return repository.find(id);
    }

    public Main save(Main item, boolean submit) {
        validateHeader(item, submit);
        rebuildSubsidies(item);
        rebuildTotals(item);
        validateTrips(item, submit);
        validateSubsidies(item);
        validateAllocations(item, submit);
        item.status = submit ? 1 : 0;
        return repository.save(item);
    }

    public void voidDocument(String id) {
        repository.deleteStatus(id, 2);
    }

    private void validateHeader(Main item, boolean submit) {
        if (!submit) return;
        require(item.reimbursementTitle, "报销标题不能为空");
        require(item.reimburserId, "报销人不能为空");
        require(item.reimDepartmentId, "报销部门不能为空");
        require(item.reimCompanyId, "费用归属公司不能为空");
        require(item.businessTypeId, "业务类型不能为空");
        require(item.businessTripReason, "出差事由不能为空");
    }

    private void validateTrips(Main item, boolean submit) {
        if (submit && item.trips.isEmpty()) throw new IllegalArgumentException("至少录入一条行程");
        Map<String, Set<LocalDate>> occupied = new HashMap<>();
        for (Trip trip : item.trips) {
            require(trip.travelerId, "出行人不能为空");
            require(trip.fromCityNo, "出发城市不能为空");
            require(trip.toCityNo, "到达城市不能为空");
            if (trip.startDate == null || trip.endDate == null) throw new IllegalArgumentException("出发到达日期不能为空");
            if (trip.endDate.isBefore(trip.startDate)) throw new IllegalArgumentException("到达日期不可早于出发日期");
            if (trip.endDate.isAfter(LocalDate.now())) throw new IllegalArgumentException("到达日期不可晚于当前日期");
            require(trip.tripDescription, "行程说明不能为空");
            Set<LocalDate> dates = occupied.computeIfAbsent(trip.travelerId, key -> new HashSet<>());
            for (LocalDate date = trip.startDate; !date.isAfter(trip.endDate); date = date.plusDays(1)) {
                if (!dates.add(date)) throw new IllegalArgumentException("补录行程中同一人员日期范围不可重复");
            }
        }
    }

    private void rebuildSubsidies(Main item) {
        Map<String, Subsidy> existing = new HashMap<>();
        for (Subsidy subsidy : item.subsidies) existing.put(subsidy.tripId, subsidy);
        item.subsidies.clear();
        for (Trip trip : item.trips) {
            if (!StringUtils.hasText(trip.id)) trip.id = uuid();
            Subsidy subsidy = existing.getOrDefault(trip.id, new Subsidy());
            subsidy.id = StringUtils.hasText(subsidy.id) ? subsidy.id : uuid();
            subsidy.tripId = trip.id;
            subsidy.travelerId = trip.travelerId;
            subsidy.travelerName = trip.travelerName;
            subsidy.tripDateRange = trip.startDate + " 至 " + trip.endDate;
            subsidy.days = trip.startDate == null || trip.endDate == null ? 0 : (int) ChronoUnit.DAYS.between(trip.startDate, trip.endDate) + 1;
            subsidy.route = trip.fromCityName + "-" + trip.toCityName;
            subsidy.subsidyCity = trip.toCityName;
            subsidy.calendar = rebuildCalendar(trip, subsidy);
            sumSubsidy(subsidy);
            item.subsidies.add(subsidy);
        }
    }

    private java.util.List<CalendarDay> rebuildCalendar(Trip trip, Subsidy subsidy) {
        Map<LocalDate, CalendarDay> oldDays = new HashMap<>();
        for (CalendarDay day : subsidy.calendar) oldDays.put(day.tripDate, day);
        java.util.List<CalendarDay> days = new java.util.ArrayList<>();
        if (trip.startDate == null || trip.endDate == null) return days;
        BigDecimal mealStandard = mealStandard(trip.toCityNo);
        for (LocalDate date = trip.startDate; !date.isAfter(trip.endDate); date = date.plusDays(1)) {
            CalendarDay day = oldDays.getOrDefault(date, new CalendarDay());
            day.tripDate = date;
            day.weekName = weekName(date.getDayOfWeek());
            day.subsidyCity = trip.toCityName;
            day.mealStandard = mealStandard;
            if (!day.mealSelected) day.mealAmount = BigDecimal.ZERO;
            if (!day.transportSelected) day.transportAmount = BigDecimal.ZERO;
            if (!day.phoneSelected) day.phoneAmount = BigDecimal.ZERO;
            if (day.mealSelected && day.mealAmount.compareTo(BigDecimal.ZERO) == 0) day.mealAmount = mealStandard;
            if (day.transportSelected && day.transportAmount.compareTo(BigDecimal.ZERO) == 0) day.transportAmount = day.transportStandard;
            if (day.phoneSelected && day.phoneAmount.compareTo(BigDecimal.ZERO) == 0) day.phoneAmount = day.phoneStandard;
            days.add(day);
        }
        return days;
    }

    private void validateSubsidies(Main item) {
        for (Subsidy subsidy : item.subsidies) {
            for (CalendarDay day : subsidy.calendar) {
                checkAmount(day.mealSelected, day.mealAmount, day.mealStandard, "餐费补助");
                checkAmount(day.transportSelected, day.transportAmount, day.transportStandard, "交通补助");
                checkAmount(day.phoneSelected, day.phoneAmount, day.phoneStandard, "通讯补助");
            }
            sumSubsidy(subsidy);
        }
    }

    private void validateAllocations(Main item, boolean submit) {
        if (item.allocations.isEmpty()) {
            Allocation first = new Allocation();
            first.id = uuid();
            first.reimCompanyId = item.reimCompanyId;
            first.reimCompanyNo = item.reimCompanyNo;
            first.reimCompanyName = item.reimCompanyName;
            first.allocationRatio = BigDecimal.ONE;
            first.allocationAmount = item.subsidyTotal;
            item.allocations.add(first);
        }
        BigDecimal ratio = BigDecimal.ZERO;
        BigDecimal amount = BigDecimal.ZERO;
        for (Allocation allocation : item.allocations) {
            if (!StringUtils.hasText(allocation.id)) allocation.id = uuid();
            if (submit) require(allocation.reimCompanyId, "费用归属不能为空");
            ratio = ratio.add(money(allocation.allocationRatio));
            amount = amount.add(money(allocation.allocationAmount));
        }
        if (submit && ratio.compareTo(BigDecimal.ONE) != 0) throw new IllegalArgumentException("分摊比例合计必须为100%");
        if (submit && amount.compareTo(item.subsidyTotal) != 0) throw new IllegalArgumentException("分摊金额合计必须等于费用合计中的补助总金额");
    }

    private void rebuildTotals(Main item) {
        item.mealAllowance = BigDecimal.ZERO;
        item.transportationAllowance = BigDecimal.ZERO;
        item.phoneAllowance = BigDecimal.ZERO;
        for (Subsidy subsidy : item.subsidies) {
            for (CalendarDay day : subsidy.calendar) {
                item.mealAllowance = item.mealAllowance.add(day.mealAmount);
                item.transportationAllowance = item.transportationAllowance.add(day.transportAmount);
                item.phoneAllowance = item.phoneAllowance.add(day.phoneAmount);
            }
        }
        item.subsidyTotal = item.mealAllowance.add(item.transportationAllowance).add(item.phoneAllowance).setScale(2, RoundingMode.HALF_UP);
    }

    private void sumSubsidy(Subsidy subsidy) {
        subsidy.applyAmount = BigDecimal.ZERO;
        subsidy.subsidyAmount = BigDecimal.ZERO;
        for (CalendarDay day : subsidy.calendar) {
            subsidy.applyAmount = subsidy.applyAmount
                    .add(day.mealSelected ? day.mealStandard : BigDecimal.ZERO)
                    .add(day.transportSelected ? day.transportStandard : BigDecimal.ZERO)
                    .add(day.phoneSelected ? day.phoneStandard : BigDecimal.ZERO);
            subsidy.subsidyAmount = subsidy.subsidyAmount.add(day.mealAmount).add(day.transportAmount).add(day.phoneAmount);
        }
        subsidy.applyAmount = subsidy.applyAmount.setScale(2, RoundingMode.HALF_UP);
        subsidy.subsidyAmount = subsidy.subsidyAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal mealStandard(String cityNo) {
        String cityType = dictionaries.city(cityNo).map(DictionaryItem::type).orElse("3");
        if ("1".equals(cityType)) return new BigDecimal("100.00");
        if ("2".equals(cityType)) return new BigDecimal("80.00");
        return new BigDecimal("50.00");
    }

    private static void checkAmount(boolean selected, BigDecimal amount, BigDecimal standard, String label) {
        if (!selected && money(amount).compareTo(BigDecimal.ZERO) != 0) throw new IllegalArgumentException(label + "未选中时金额必须为0");
        if (selected && (money(amount).compareTo(BigDecimal.ZERO) < 0 || money(amount).compareTo(standard) > 0)) {
            throw new IllegalArgumentException(label + "金额需为正数且不可大于标准金额");
        }
    }

    private static void require(String value, String message) {
        if (!StringUtils.hasText(value)) throw new IllegalArgumentException(message);
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String weekName(DayOfWeek week) {
        return switch (week) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }

    private static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
