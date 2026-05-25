package com.kjd.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reimbursement {
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
