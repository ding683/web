package com.kjd.backend.service;

import com.kjd.backend.vo.DictionaryItemVO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DictionaryService {
    Map<String, List<DictionaryItemVO>> all();

    Optional<DictionaryItemVO> city(String no);
}
