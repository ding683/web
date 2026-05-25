package com.kjd.backend.entity;

import java.math.BigDecimal;

public class Allocation {
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
