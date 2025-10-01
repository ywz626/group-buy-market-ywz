package com.ywz.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author 于汶泽
 * @Description: 回调任务
 * @DateTime: 2025/6/4 20:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotifyTask {


    @TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;

    private Long activityId;

    private String teamId;

    private String notifyType;

    private String notifyMq;

    private String notifyUrl;

    private Integer notifyCount;

    private Integer notifyStatus;

    private String parameterJson;

    private Date createTime;

    private Date updateTime;

}
