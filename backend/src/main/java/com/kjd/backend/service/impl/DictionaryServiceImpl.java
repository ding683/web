package com.kjd.backend.service.impl;

import com.kjd.backend.service.DictionaryService;
import com.kjd.backend.vo.DictionaryItemVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DictionaryServiceImpl implements DictionaryService {
    public final List<DictionaryItemVO> companies = List.of(
            new DictionaryItemVO("1C54557F1782E000", "0407", "胜意科技北京分公司", null, null, true),
            new DictionaryItemVO("19218A262C976000", "0408", "胜意科技上海分公司", null, null, true),
            new DictionaryItemVO("1C61686865DA8000", "0409", "胜意科技武汉分公司", null, null, true),
            new DictionaryItemVO("1717271D1DA15000", "0410", "胜意科技杭州分公司", null, null, true),
            new DictionaryItemVO("16AE93CC7EF92002", "0411", "胜意科技荆州分公司", null, null, true)
    );

    public final List<DictionaryItemVO> departments = List.of(
            new DictionaryItemVO("13AB8D7B52A9B002", "072001", "客户成功事业部", null, null, true),
            new DictionaryItemVO("13BFD31C6029A002", "072002", "企业消费事业部", null, null, true),
            new DictionaryItemVO("14515BB4BFB92003", "072003", "企业费控事业部", null, null, true),
            new DictionaryItemVO("19206611C47A6000", "072004", "集采事业部", null, null, true),
            new DictionaryItemVO("19D32F9FE9647000", "072005", "航旅事业部", null, null, true),
            new DictionaryItemVO("13C7E2BAE0393001", "072006", "运营事业部", null, null, true),
            new DictionaryItemVO("14055D22BB808001", "072007", "营销事业部", null, null, true)
    );

    public final List<DictionaryItemVO> employees = List.of(
            new DictionaryItemVO("13AB3A3F72409002", "74541", "徐年年", null, null, true),
            new DictionaryItemVO("13AB498CC6409002", "74008", "郑雨雪", null, null, true),
            new DictionaryItemVO("13AB4A56BB009002", "21552", "邹薇", null, null, true),
            new DictionaryItemVO("13AB591FE8009002", "80681", "王成军", null, null, true),
            new DictionaryItemVO("13AB77281A408001", "89899", "潘展飞", null, null, true),
            new DictionaryItemVO("13AB7925EB808001", "10503", "姜林", null, null, true)
    );

    public final List<DictionaryItemVO> businessTypes = List.of(
            new DictionaryItemVO("18F0916A8C2C4000", "1001001", "员工差旅活动", "none", null, false),
            new DictionaryItemVO("18F091913EEC4000", "100100101", "境内出差", "18F0916A8C2C4000", null, false),
            new DictionaryItemVO("1B5FEB7DD4396000", "10010010101", "项目出差", "18F091913EEC4000", null, true),
            new DictionaryItemVO("1A92E43082EFC000", "10010010102", "市场拓展出差", "18F091913EEC4000", null, true),
            new DictionaryItemVO("13AB3A4138008001", "100100102", "境外出差", "18F0916A8C2C4000", null, false),
            new DictionaryItemVO("13AB3A4248008002", "10010010201", "国外考察", "13AB3A4138008001", null, true),
            new DictionaryItemVO("13AB3A4154008001", "10010010202", "售后维护出差", "13AB3A4138008001", null, true),
            new DictionaryItemVO("13AB3A4172008001", "1001002", "人力资源", "none", null, false),
            new DictionaryItemVO("13AB3A418F808001", "100100201", "个人团队培训", "13AB3A4172008001", null, true),
            new DictionaryItemVO("13AB3A41AC408001", "100100202", "招聘会", "13AB3A4172008001", null, true),
            new DictionaryItemVO("13AB3A41CD808002", "1001003", "员工福利", "none", null, false),
            new DictionaryItemVO("13AB3A41ED408002", "100100301", "员工旅游", "13AB3A41CD808002", null, true),
            new DictionaryItemVO("13AB3A420CC08002", "100100302", "员工团建", "13AB3A41CD808002", null, true),
            new DictionaryItemVO("13AB3A422A808001", "100100303", "员工体检", "13AB3A41CD808002", null, true)
    );

    public final List<DictionaryItemVO> cities = List.of(
            new DictionaryItemVO("10119", "10119", "北京", null, "1", true),
            new DictionaryItemVO("10621", "10621", "上海", null, "1", true),
            new DictionaryItemVO("10458", "10458", "武汉", null, "2", true),
            new DictionaryItemVO("10216", "10216", "杭州", null, "2", true),
            new DictionaryItemVO("10455", "10455", "荆州", null, "3", true)
    );

    public final List<DictionaryItemVO> projects = List.of(
            new DictionaryItemVO("12BC248B25083001", "nonProjectRelated", "非项目类费用归集", null, null, true),
            new DictionaryItemVO("1C811ABF96195000", "centralChina", "华中客户定制化项目", null, null, true),
            new DictionaryItemVO("1C5931735AC4A000", "southChina", "华南客户定制化项目", null, null, true),
            new DictionaryItemVO("1771EC45F2443000", "northChina", "华北客户定制化项目", null, null, true),
            new DictionaryItemVO("1762792DB4E9A002", "eastChina", "华东客户定制化项目", null, null, true),
            new DictionaryItemVO("17071065FC29A002", "southWest", "西南客户定制化项目", null, null, true),
            new DictionaryItemVO("162664EBE9ABE001", "northWest", "西北客户定制化项目", null, null, true),
            new DictionaryItemVO("162664B8526BE002", "northEast", "东北客户定制化项目", null, null, true)
    );

    @Override
    public Map<String, List<DictionaryItemVO>> all() {
        return Map.of(
                "companies", companies,
                "departments", departments,
                "employees", employees,
                "businessTypes", businessTypes,
                "cities", cities,
                "projects", projects
        );
    }

    @Override
    public Optional<DictionaryItemVO> city(String no) {
        return cities.stream().filter(item -> item.no().equals(no) || item.id().equals(no)).findFirst();
    }
}
