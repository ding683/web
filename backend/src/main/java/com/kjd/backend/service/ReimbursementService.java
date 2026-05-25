package com.kjd.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kjd.backend.entity.Reimbursement;
import com.kjd.backend.vo.ReimbursementVO;
import dto.ReimbursePageQueryDTO;

public interface ReimbursementService  extends IService<Reimbursement> {
    IPage<ReimbursementVO> queryReimbursePage(ReimbursePageQueryDTO reimbursePageQueryDTO);
}
