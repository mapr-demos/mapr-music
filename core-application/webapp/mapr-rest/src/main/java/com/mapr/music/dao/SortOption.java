package com.mapr.music.dao;

import java.util.Arrays;
import java.util.List;

public class SortOption {

    private List<String> fields;
    private Order order;

    public enum Order {
        ASC,
        DESC
    }

    public SortOption(Order order, String... fields) {
        this(order, Arrays.asList(fields));
    }

    public SortOption(Order order, List<String> fields) {
        this.order = order;
        this.fields = fields;
    }

    public static SortOption asc(String... fields) {
        return new SortOption(Order.ASC, fields);
    }

    public static SortOption desc(String... fields) {
        return new SortOption(Order.DESC, fields);
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
