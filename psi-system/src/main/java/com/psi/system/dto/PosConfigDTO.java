package com.psi.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PosConfigDTO implements Serializable {

    private Long id;

    private String dataUuid;

    private Long tenantId;

    private String posSn;

    private String shopCode;

    private String posId;

    private String posName;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;

    private Long updateBy;

    private LocalDateTime updateTime;
}