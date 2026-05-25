package com.kjd.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.kjd.backend.service.ReimbursementService;
import com.kjd.backend.vo.ReimbursementVO;
import com.kjd.backend.vo.Result;
import dto.ReimbursePageQueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class ReimbursementController {

    @Autowired
    private ReimbursementService reimbursementService;

    @PostMapping("/tr/querylReimbursePage")
    public Result<IPage<ReimbursementVO>> queryReimbursePage(@RequestBody ReimbursePageQueryDTO reimbursePageQueryDTO){
        IPage<ReimbursementVO> page = reimbursementService.queryReimbursePage(reimbursePageQueryDTO);
        return Result.success(page);
    }
}
