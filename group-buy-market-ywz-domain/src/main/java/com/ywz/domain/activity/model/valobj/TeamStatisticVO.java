package com.ywz.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description TODO
 * @create 2025-02-02 15:21
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamStatisticVO {

    // 开团队伍数量
    private Integer allTeamCount;
    // 成团队伍数量
    private Integer allTeamCompleteCount;
    // 参团人数总量 - 一个商品的总参团人数
    private Integer allTeamUserCount;

}
