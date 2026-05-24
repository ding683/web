package com.kjd.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReimbursementModels {
    public static class Main {
        public String id;
        public String reimbursementNo;
        public Integer status = 0;
        public String statusName = "草稿";
        public LocalDateTime creationTime;
        public LocalDate submitDate;
        public String reimbursementTitle;
        public String reimburserId;
        public String reimburserNo;
        public String reimburserName;
        public String reimDepartmentId;
        public String reimDepartmentNo;
        public String reimDepartmentName;
        public String reimCompanyId;
        public String reimCompanyNo;
        public String reimCompanyName;
        public String businessTypeId;
        public String businessTypeNo;
        public String businessTypeName;
        public String businessTripReason;
        public BigDecimal subsidyTotal = BigDecimal.ZERO;
        public BigDecimal mealAllowance = BigDecimal.ZERO;
        public BigDecimal transportationAllowance = BigDecimal.ZERO;
        public BigDecimal phoneAllowance = BigDecimal.ZERO;
        public String remarks;
        public List<Trip> trips = new ArrayList<>();
        public List<Subsidy> subsidies = new ArrayList<>();
        public List<Allocation> allocations = new ArrayList<>();
    }

    public static class Trip {
        public String id;
        public String travelerId;
        public String travelerNo;
        public String travelerName;
        public String fromCityNo;
        public String fromCityName;
        public String toCityNo;
        public String toCityName;
        public LocalDate startDate;
        public LocalDate endDate;
        public String tripDescription;
    }

    public static class Subsidy {
        public String id;
        public String tripId;
        public String travelerId;
        public String travelerName;
        public String tripDateRange;
        public Integer days = 0;
        public String route;
        public String subsidyCity;
        public BigDecimal applyAmount = BigDecimal.ZERO;
        public BigDecimal subsidyAmount = BigDecimal.ZERO;
        public List<CalendarDay> calendar = new ArrayList<>();
    }

    public static class CalendarDay {
        public LocalDate tripDate;
        public String weekName;
        public String subsidyCity;
        public boolean mealSelected;
        public boolean transportSelected;
        public boolean phoneSelected;
        public BigDecimal mealStandard = BigDecimal.ZERO;
        public BigDecimal transportStandard = new BigDecimal("40.00");
        public BigDecimal phoneStandard = new BigDecimal("40.00");
        public BigDecimal mealAmount = BigDecimal.ZERO;
        public BigDecimal transportAmount = BigDecimal.ZERO;
        public BigDecimal phoneAmount = BigDecimal.ZERO;
    }

    public static class Allocation {
        public String id;
        public String reimCompanyId;
        public String reimCompanyNo;
        public String reimCompanyName;
        public String projectId;
        public String projectNo;
        public String projectName;
        public BigDecimal allocationRatio = BigDecimal.ZERO;
        public BigDecimal allocationAmount = BigDecimal.ZERO;
    }

    public static class Query {
        public String reimbursementNo;
        public String reimbursementTitle;
        public String businessTripReason;
        public String reimCompanyId;
        public String reimDepartmentId;
        public String reimburserId;
        public String businessTypeId;
        public int page = 1;
        public int size = 10;
    }

    public static class PageResult<T> {
        public long total;
        public int current;
        public int size;
        public List<T> records;

        public PageResult(long total, int current, int size, List<T> records) {
            this.total = total;
            this.current = current;
            this.size = size;
            this.records = records;
        }
    }
}
