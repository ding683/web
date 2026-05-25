package dto;

import lombok.Data;

/**
 * 报销单分页查询 DTO
 *
 * @author yourname
 * @date 2026-05-25
 */
@Data
public class ReimbursePageQueryDTO {

    /**
     * 当前页，默认1
     */
    private Integer current ;

    /**
     * 每页大小，默认10
     */
    private Integer size ;

    /**
     * 报销单号
     */
    private String reimburseNo;

    /**
     * 报销标题
     */
    private String reimburseTitle;

    /**
     * 报销事由
     */
    private String reimburseReason;

    /**
     * 费用归属公司
     */
    private String expenseBelongCompName;

    /**
     * 报销部门
     */
    private String reimburseDeptName;

    /**
     * 报销人
     */
    private String reimburseUserName;

    /**
     * 业务类型
     */
    private String businessTypeName;
}