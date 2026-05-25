package com.kjd.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fk_reim_main")
public class Reimbursement {

    /**
     * 自增主键ID
     */
    @TableId(type= IdType.AUTO)
    private Long id;

    /**
     * 报销单号
     */
    private String reimbursementNo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 报销标题
     */
    private String reimbursementTitle;

    /**
     * 单据状态：0草稿/1已完成/2已作废
     */
    private Integer documentStatus;

    /**
     * 单据类型
     */
    private String documentType;

    /**
     * 报销人ID
     */
    private String reimburserId;

    /**
     * 报销人工号
     */
    private String reimburserNo;

    /**
     * 报销人姓名
     */
    private String reimburserName;

    /**
     * 报销部门ID
     */
    private String reimDepartmentId;

    /**
     * 报销部门编号
     */
    private String reimDepartmentNo;

    /**
     * 报销部门名称
     */
    private String reimDepartmentName;

    /**
     * 费用归属公司ID
     */
    private String reimCompanyId;

    /**
     * 费用归属公司编号
     */
    private String reimCompanyNo;

    /**
     * 费用归属公司名称
     */
    private String reimCompanyName;

    /**
     * 业务类型ID
     */
    private String businessTypeId;

    /**
     * 业务类型编号
     */
    private String businessTypeNo;

    /**
     * 业务类型名称
     */
    private String businessTypeName;

    /**
     * 报销事由
     */
    private String reimbursementReason;

    /**
     * 补助总金额
     */
    private BigDecimal subsidyTotal;

    /**
     * 餐费补助
     */
    private BigDecimal mealAllowance;

    /**
     * 交通补助
     */
    private BigDecimal transportationAllowance;

    /**
     * 通讯补助
     */
    private BigDecimal phoneAllowance;

    /**
     * 备注信息
     */
    private String remarks;
}