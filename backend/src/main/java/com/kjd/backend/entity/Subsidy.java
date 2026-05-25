package com.kjd.backend.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Subsidy {
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
