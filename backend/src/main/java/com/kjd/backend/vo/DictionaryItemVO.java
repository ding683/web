package com.kjd.backend.vo;

public record DictionaryItemVO(
        String id,
        String no,
        String name,
        String parentId,
        String type,
        boolean leaf
) {
}
