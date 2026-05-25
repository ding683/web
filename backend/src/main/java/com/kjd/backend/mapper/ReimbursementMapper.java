package com.kjd.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kjd.backend.entity.Reimbursement;
import com.kjd.backend.vo.ReimbursementVO;
import dto.ReimbursePageQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReimbursementMapper  extends BaseMapper<Reimbursement> {

    IPage<ReimbursementVO> queryReimbursePage(Page<ReimbursementVO> page, @Param("dto") ReimbursePageQueryDTO dto);
}
