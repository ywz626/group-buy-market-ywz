package com.ywz.test.infrastructure.dao;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ywz.infrastructure.dao.IGroupBuyActivityDao;
import com.ywz.infrastructure.dao.po.GroupBuyActivityPO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/31 17:46
 */
@SpringBootTest
@Slf4j
public class GroupBuyActivityTest {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private ObjectMapper mapper;
    @Test
    public void testGroupBuyActivity() throws JsonProcessingException {
        List<GroupBuyActivityPO> list = groupBuyActivityDao.selectList(null);
        log.info("测试结果：{}", mapper.writeValueAsString(list));
    }
    @Test
    public void testOne() throws JsonProcessingException {
        GroupBuyActivityPO result = groupBuyActivityDao.selectById(1);
        log.info("测试结果：{}", mapper.writeValueAsString(result));
    }
}
