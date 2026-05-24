package com.kjd.backend.model;

public record DictionaryItem(
        String id,
        String no,
        String name,
        String parentId,
        String type,
        boolean leaf
) {
}
