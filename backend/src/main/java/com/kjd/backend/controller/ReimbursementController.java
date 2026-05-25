package com.kjd.backend.controller;

import com.kjd.backend.common.Result;
import com.kjd.backend.dto.ReimbursementQueryDTO;
import com.kjd.backend.entity.Reimbursement;
import com.kjd.backend.service.DictionaryService;
import com.kjd.backend.service.ReimbursementService;
import com.kjd.backend.vo.DictionaryItemVO;
import com.kjd.backend.vo.PageResultVO;
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
    public Result<Map<String, List<DictionaryItemVO>>> dictionaries() {
        return Result.ok(dictionaryService.all());
    }

    @PostMapping("/reimbursements/page")
    public Result<PageResultVO<Reimbursement>> page(@RequestBody ReimbursementQueryDTO query) {
        return Result.ok(reimbursementService.page(query));
    }

    @GetMapping("/reimbursements/{id}")
    public Result<Reimbursement> detail(@PathVariable String id) {
        Reimbursement item = reimbursementService.find(id);
        if (item == null) return Result.fail("单据不存在");
        return Result.ok(item);
    }

    @PostMapping("/reimbursements/save")
    public Result<Reimbursement> save(@RequestBody Reimbursement item) {
        return Result.ok(reimbursementService.save(item, false));
    }

    @PostMapping("/reimbursements/submit")
    public Result<Reimbursement> submit(@RequestBody Reimbursement item) {
        return Result.ok(reimbursementService.save(item, true));
    }

    @PostMapping("/reimbursements/{id}/void")
    public Result<Void> voidDocument(@PathVariable String id) {
        reimbursementService.voidDocument(id);
        return Result.ok(null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> validation(Exception e) {
        return Result.fail(e.getMessage());
    }
}
