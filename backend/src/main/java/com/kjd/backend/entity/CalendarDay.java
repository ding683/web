package com.kjd.backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CalendarDay {
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
