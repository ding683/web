package com.kjd.backend.controller;

import com.kjd.backend.model.ApiResponse;
import com.kjd.backend.model.DictionaryItem;
import com.kjd.backend.model.ReimbursementModels.Main;
import com.kjd.backend.model.ReimbursementModels.PageResult;
import com.kjd.backend.model.ReimbursementModels.Query;
import com.kjd.backend.service.DictionaryService;
import com.kjd.backend.service.ReimbursementService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ReimbursementController {
    private final ReimbursementService reimbursementService;
    private final DictionaryService dictionaryService;

    public ReimbursementController(ReimbursementService reimbursementService, DictionaryService dictionaryService) {
        this.reimbursementService = reimbursementService;
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/dict")
    public ApiResponse<Map<String, List<DictionaryItem>>> dictionaries() {
        return ApiResponse.ok(dictionaryService.all());
    }

    @PostMapping("/reimbursements/page")
    public ApiResponse<PageResult<Main>> page(@RequestBody Query query) {
        return ApiResponse.ok(reimbursementService.page(query));
    }

    @GetMapping("/reimbursements/{id}")
    public ApiResponse<Main> detail(@PathVariable String id) {
        Main item = reimbursementService.find(id);
        if (item == null) return ApiResponse.fail("单据不存在");
        return ApiResponse.ok(item);
    }

    @PostMapping("/reimbursements/save")
    public ApiResponse<Main> save(@RequestBody Main item) {
        return ApiResponse.ok(reimbursementService.save(item, false));
    }

    @PostMapping("/reimbursements/submit")
    public ApiResponse<Main> submit(@RequestBody Main item) {
        return ApiResponse.ok(reimbursementService.save(item, true));
    }

    @PostMapping("/reimbursements/{id}/void")
    public ApiResponse<Void> voidDocument(@PathVariable String id) {
        reimbursementService.voidDocument(id);
        return ApiResponse.ok(null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> validation(Exception e) {
        return ApiResponse.fail(e.getMessage());
    }
}
