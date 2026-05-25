package com.kjd.backend.vo;

import java.util.List;

public class PageResultVO<T> {
    public long total;
    public int current;
    public int size;
    public List<T> records;

    public PageResultVO(long total, int current, int size, List<T> records) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.records = records;
    }
}
