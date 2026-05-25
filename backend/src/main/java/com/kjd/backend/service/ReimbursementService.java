package com.kjd.backend.service;

import com.kjd.backend.dto.ReimbursementQueryDTO;
import com.kjd.backend.entity.Reimbursement;
import com.kjd.backend.vo.PageResultVO;

public interface ReimbursementService {
    PageResultVO<Reimbursement> page(ReimbursementQueryDTO query);

    Reimbursement find(String id);

    Reimbursement save(Reimbursement item, boolean submit);

    void voidDocument(String id);
}
