package com.lin.opush.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.dao.MessageTemplateDao;
import com.lin.opush.dao.SmsRecordDao;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.SmsRecord;
import com.lin.opush.service.DataTraceService;
import com.lin.opush.utils.Convert4Amis;
import com.lin.opush.utils.RedisUtils;
import com.lin.opush.vo.DataTraceParam;
import com.lin.opush.vo.amis.SmsDataVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据全链路追踪服务实现类
 */
@Service
public class DataTraceServiceImpl implements DataTraceService {
    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 消息模板 Dao
     */
    @Autowired
    private MessageTemplateDao messageTemplateDao;

    /**
     * 短信记录 Dao
     */
    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 获取短信下发记录列表
     * @param dataTraceParam 数据全链路追踪请求参数
     * @return 分页短信下发记录列表对应VO
     */
    @Override
    public Page<SmsRecord> querySmsDataTraceList(DataTraceParam dataTraceParam){
        // 判断页码和是否合理（page > 0, perPage >= 1）并生成分页请求对象【由于展示到前端为两条记录合并为一条，分页条数需乘2】
        PageRequest pageRequest = PageRequest.of(dataTraceParam.getPage() - 1, dataTraceParam.getPerPage() * 2);
        // root: 代表查询的根对象，即SmsRecord（短信下发记录）
        // criteriaQuery: 顶层查询对象，sql语句关键字，用于自定义查询方式（基本不用）
        // criteriaBuilder: 查询构造器，封装很多的查询条件
        return smsRecordDao.findAll((Specification<SmsRecord>) (root, criteriaQuery, criteriaBuilder) -> {
            // 搜索条件列表
            List<Predicate> predicateList = new ArrayList<>();
            // 添加搜索条件（接收者、下发时间、记录创建者）
            if (StrUtil.isNotBlank(dataTraceParam.getReceiver())) {
                predicateList.add(criteriaBuilder.like(root.get("phone").as(String.class), "%" + dataTraceParam.getReceiver() + "%"));
            }
            if (ObjectUtil.isNotNull(dataTraceParam.getDateTime())) {
                // 查询短信下发记录时间点【yyyyMMdd，查询用户指定时间的该天】
                Integer sendDate = Integer.valueOf(DateUtil.format(new Date(dataTraceParam.getDateTime() * 1000L), DatePattern.PURE_DATE_PATTERN));
                predicateList.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sendDate").as(Integer.class), sendDate));
            }
            predicateList.add(criteriaBuilder.equal(root.get("creator").as(String.class), dataTraceParam.getCreator()));
            Predicate[] predicate = new Predicate[predicateList.size()];
            criteriaQuery.where(criteriaBuilder.and(predicateList.toArray(predicate)));
            // 查询内容按更新时间排序
            criteriaQuery.orderBy(criteriaBuilder.desc(root.get("created")));
            // 返回与where子句限制相对应的predicates，如果未指定限制，则返回null
            return criteriaQuery.getRestriction();
        }, pageRequest);
//        // 将分页对象转为短信下发记录列表
//        List<SmsRecord> smsRecordList = smsRecords.toList();
//        if (CollUtil.isEmpty(smsRecordList)) {
//            return SmsDataVo.builder().items(Arrays.asList(SmsDataVo.ItemsVO.builder().build())).build();
//        }
//        // 根据手机号+下发批次id分组出入Map<手机号+下发批次id, 短信下发记录列表>
//        // 将同一条短信下发和短信回执拼装为一条短信记录
//        Map<String, List<SmsRecord>> maps = smsRecordList.stream().collect(Collectors.groupingBy((o) -> o.getPhone() + o.getSeriesId()));
//        return Convert4Amis.getSmsDataVo(maps);
    }

//    @Override
//    public UserTimeLineVo getTraceUserInfo(String receiver) {
//        List<String> userInfoList = redisUtils.lRange(receiver, 0, -1);
//        if (CollUtil.isEmpty(userInfoList)) {
//            return UserTimeLineVo.builder().items(new ArrayList<>()).build();
//        }
//
//        // 0. 按时间排序
//        List<SimpleAnchorInfo> sortAnchorList = userInfoList.stream().map(s -> JSON.parseObject(s, SimpleAnchorInfo.class)).sorted((o1, o2) -> Math.toIntExact(o1.getTimestamp() - o2.getTimestamp())).collect(Collectors.toList());
//
//        // 1. 对相同的businessId进行分类  {"businessId":[{businessId,state,timeStamp},{businessId,state,timeStamp}]}
//        Map<String, List<SimpleAnchorInfo>> map = MapUtil.newHashMap();
//        for (SimpleAnchorInfo simpleAnchorInfo : sortAnchorList) {
//            List<SimpleAnchorInfo> simpleAnchorInfos = map.get(String.valueOf(simpleAnchorInfo.getBusinessId()));
//            if (CollUtil.isEmpty(simpleAnchorInfos)) {
//                simpleAnchorInfos = new ArrayList<>();
//            }
//            simpleAnchorInfos.add(simpleAnchorInfo);
//            map.put(String.valueOf(simpleAnchorInfo.getBusinessId()), simpleAnchorInfos);
//        }
//
//        // 2. 封装vo 给到前端渲染展示
//        List<UserTimeLineVo.ItemsVO> items = new ArrayList<>();
//        for (Map.Entry<String, List<SimpleAnchorInfo>> entry : map.entrySet()) {
//            Long messageTemplateId = TaskInfoUtils.getMessageTemplateIdFromBusinessId(Long.valueOf(entry.getKey()));
//            MessageTemplate messageTemplate = messageTemplateDao.findById(messageTemplateId).orElse(null);
//            if (Objects.isNull(messageTemplate)) {
//                continue;
//            }
//
//            StringBuilder sb = new StringBuilder();
//            for (SimpleAnchorInfo simpleAnchorInfo : entry.getValue()) {
//                if (AnchorState.RECEIVE.getCode().equals(simpleAnchorInfo.getState())) {
//                    sb.append(StrPool.CRLF);
//                }
//                String startTime = DateUtil.format(new Date(simpleAnchorInfo.getTimestamp()), DatePattern.NORM_DATETIME_PATTERN);
//                String stateDescription = AnchorState.getDescriptionByCode(simpleAnchorInfo.getState());
//                sb.append(startTime).append(StrPool.C_COLON).append(stateDescription).append("==>");
//            }
//
//            for (String detail : sb.toString().split(StrPool.CRLF)) {
//                if (StrUtil.isNotBlank(detail)) {
//                    UserTimeLineVo.ItemsVO itemsVO = UserTimeLineVo.ItemsVO.builder()
//                            .businessId(entry.getKey())
//                            .sendType(ChannelType.getEnumByCode(messageTemplate.getSendChannel()).getDescription())
//                            .creator(messageTemplate.getCreator())
//                            .title(messageTemplate.getName())
//                            .detail(detail)
//                            .build();
//                    items.add(itemsVO);
//                }
//            }
//        }
//        return UserTimeLineVo.builder().items(items).build();
//    }

//    @Override
//    public EchartsVo getTraceMessageTemplateInfo(String businessId) {
//
//        // 获取businessId并获取模板信息
//        businessId = getRealBusinessId(businessId);
//        Optional<MessageTemplate> optional = messageTemplateDao.findById(TaskInfoUtils.getMessageTemplateIdFromBusinessId(Long.valueOf(businessId)));
//        if (!optional.isPresent()) {
//            return null;
//        }
//
//        /**
//         * 获取redis清洗好的数据
//         * key：state
//         * value:stateCount
//         */
//        Map<Object, Object> anchorResult = redisUtils.hGetAll(getRealBusinessId(businessId));
//
//        return Convert4Amis.getEchartsVo(anchorResult, optional.get().getName(), businessId);
//    }

    /**
     * 如果传入的是模板ID，则生成【当天】的businessId进行查询
     * 如果传入的是businessId，则按默认的businessId进行查询
     * 判断是否为businessId则判断长度是否为16位（businessId长度固定16)
     */
//    private String getRealBusinessId(String businessId) {
//        if (AustinConstant.BUSINESS_ID_LENGTH == businessId.length()) {
//            return businessId;
//        }
//        Optional<MessageTemplate> optional = messageTemplateDao.findById(Long.valueOf(businessId));
//        if (optional.isPresent()) {
//            MessageTemplate messageTemplate = optional.get();
//            return String.valueOf(TaskInfoUtils.generateBusinessId(messageTemplate.getId(), messageTemplate.getTemplateType()));
//        }
//        return businessId;
//    }
}
