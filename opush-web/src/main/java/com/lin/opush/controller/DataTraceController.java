package com.lin.opush.controller;

import cn.hutool.core.collection.CollUtil;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.service.DataTraceService;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.vo.DataTraceParam;
import com.lin.opush.vo.amis.SmsDataVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

//    @PostMapping("/user")
//    @ApiOperation("/获取【当天】用户接收消息的全链路数据")
//    public UserTimeLineVo getUserData(@RequestBody DataParam dataParam) {
//        return dataService.getTraceUserInfo(dataParam.getReceiver());
//    }

//    @PostMapping("/messageTemplate")
//    @ApiOperation("/获取消息模板全链路数据")
//    public EchartsVo getMessageTemplateData(@RequestBody DataParam dataParam) {
//        EchartsVo echartsVo = EchartsVo.builder().build();
//        if (StrUtil.isNotBlank(dataParam.getBusinessId())) {
//            echartsVo = dataService.getTraceMessageTemplateInfo(dataParam.getBusinessId());
//        }
//        return echartsVo;
//    }
}
