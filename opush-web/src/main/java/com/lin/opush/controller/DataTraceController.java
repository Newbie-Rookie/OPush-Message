package com.lin.opush.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.lin.opush.constants.RegexConstant;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.service.DataTraceService;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.vo.DataTraceParam;
import com.lin.opush.vo.amis.EchartsDataVo;
import com.lin.opush.vo.amis.SmsDataVo;
import com.lin.opush.vo.amis.UserDataVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.lin.opush.constants.RegexConstant.PHONE_REGEX;

/**
 * 数据全链路追踪控制器
 */
@Slf4j
@RestController
@RequestMapping("/trace")
public class DataTraceController {
    @Autowired
    private DataTraceService dataTraceService;

    /**
     * 获取短信下发记录列表
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 短信下发记录对应VO
     */
    @PostMapping("/list")
    public SmsDataVo getSmsData(@RequestBody DataTraceParam dataTraceParam) {
        Page<SmsRecord> smsRecords = dataTraceService.querySmsDataTraceList(dataTraceParam);
        // 将分页对象转为短信下发记录列表
        List<SmsRecord> smsRecordList = smsRecords.toList();
        if (CollUtil.isEmpty(smsRecordList)) {
            return SmsDataVo.builder()
                            .items(Arrays.asList(SmsDataVo.ItemsVO.builder().build()))
                            .count(0L).build();
        }
        // 根据手机号+下发批次id分组出入Map<手机号+下发批次id, 短信下发记录列表>
        // 将同一条短信下发和短信回执放入同一个List
        Map<String, List<SmsRecord>> maps = smsRecordList.stream()
                .collect(Collectors.groupingBy((smsRecord) -> smsRecord.getPhone() + smsRecord.getSeriesId()));
        return Convert4Amis.getSmsDataVo(maps).setCount(smsRecords.getTotalElements()/2);
    }

    /**
     * userId的全链路追踪
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 用户链路追踪VO
     */
    @PostMapping("/user")
    public UserDataVo getUserData(@RequestBody DataTraceParam dataTraceParam) {
        return dataTraceService.getTraceUserInfo(dataTraceParam);
    }

    /**
     * 模板的全链路追踪
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 图表VO
     */
    @PostMapping("/messageTemplate")
    public EchartsDataVo getMessageTemplateData(@RequestBody DataTraceParam dataTraceParam) {
        EchartsDataVo echartsVo = EchartsDataVo.builder().build();
        if (StrUtil.isNotBlank(dataTraceParam.getBusinessId()) &&
                (dataTraceParam.getBusinessId().matches(RegexConstant.MESSAGE_TEMPLATE_REGEX) ||
                        dataTraceParam.getBusinessId().matches(RegexConstant.BUSINESSID_REGEX))) {
            echartsVo = dataTraceService.getTraceMessageTemplateInfo(dataTraceParam);
        }
        return echartsVo;
    }
}
