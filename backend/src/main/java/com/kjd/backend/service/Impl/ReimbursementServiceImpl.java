package com.kjd.backend.service.Impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kjd.backend.entity.Reimbursement;
import com.kjd.backend.mapper.ReimbursementMapper;
import com.kjd.backend.service.ReimbursementService;
import com.kjd.backend.vo.ReimbursementVO;
import dto.ReimbursePageQueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReimbursementServiceImpl extends ServiceImpl<ReimbursementMapper, Reimbursement> implements ReimbursementService {

    @Autowired
    private ReimbursementMapper reimbursementMapper;
    @Override
    public IPage<ReimbursementVO> queryReimbursePage(ReimbursePageQueryDTO reimbursePageQueryDTO) {
        Page<ReimbursementVO> page=new Page<>(reimbursePageQueryDTO.getCurrent(),reimbursePageQueryDTO.getSize());
        return reimbursementMapper.queryReimbursePage(page,reimbursePageQueryDTO);
    }
}
