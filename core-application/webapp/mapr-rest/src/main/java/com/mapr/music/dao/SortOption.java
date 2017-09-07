package com.mapr.music.dao;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents sort option with list of fields and order.
 */
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

    public SortOption(String orderName, List<String> fields) {

        if (orderName == null || orderName.isEmpty()) {
            throw new IllegalArgumentException("Order name can not be empty");
        }

        String orderNameUppercase = orderName.toUpperCase();
        if (!Order.ASC.toString().equals(orderNameUppercase) && !Order.DESC.toString().equals(orderNameUppercase)) {
            throw new IllegalArgumentException("Order name must be correspond either to '" + Order.ASC + "' or to '" +
                    Order.DESC + "'");
        }

        this.fields = fields;
    }

    /**
     * Creates new instance of {@link SortOption} according to the specified string, which contains comma-separated
     * list of sort fields and sort type. Note that sort type must occur in string only once. Here is example of valid
     * string: 'name,asc,_id'.
     *
     * @param commaSeparatedFieldsAndOrder string, which contains comma-separated
     *                                     list of sort fields and sort type.
     * @return instance of {@link SortOption}, which is created according to the specified string
     */
    public static SortOption valueOf(String commaSeparatedFieldsAndOrder) {

        if (commaSeparatedFieldsAndOrder == null || commaSeparatedFieldsAndOrder.isEmpty()) {
            throw new IllegalArgumentException("Comma separated fields-order string can not be empty");
        }

        String[] fieldsAndOrderArray = commaSeparatedFieldsAndOrder.split(",");

        Order order = null;
        List<String> fields = new ArrayList<>();
        for (String fieldOrOrderEntry : fieldsAndOrderArray) {

            if (fieldOrOrderEntry == null || fieldOrOrderEntry.isEmpty()) {
                continue;
            }

            // Check whether entry is order or field name
            String fieldOrOrderEntryUppercase = fieldOrOrderEntry.toUpperCase();
            if (Order.ASC.toString().equals(fieldOrOrderEntryUppercase)
                    || Order.DESC.toString().equals(fieldOrOrderEntryUppercase)) {

                if (order != null) {
                    throw new IllegalArgumentException("Comma separated fields-order string contains multiple orders");
                }

                order = Order.valueOf(fieldOrOrderEntryUppercase);
            } else {
                fields.add(fieldOrOrderEntry);
            }

        }

        if (order == null || fields.isEmpty()) {
            throw new IllegalArgumentException("Comma separated fields-order string does not contain order or fields");
        }

        return new SortOption(order, fields);
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("order", order)
                .append("fields", fields)
                .toString();
    }
}
