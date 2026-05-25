package com.kjd.backend.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReimbursementVO {
    private Long id;
    private String reimbursementNo ;
    private String documentStatus;

    private String documentType;

    private String reimburserName;
    private String reimDepartmentName;
    private String reimCompanyName;
    private String businessTypeName;

    private String reimbursementTitle;
    private String reimbursementReason;

    private BigDecimal subsidyTotal;

    private LocalDateTime createTime;

    private String remarks;

}