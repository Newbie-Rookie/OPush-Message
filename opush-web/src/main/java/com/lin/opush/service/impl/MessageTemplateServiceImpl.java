package com.lin.opush.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lin.opush.constants.CommonConstant;
import com.lin.opush.constants.OpushConstant;
import com.lin.opush.dao.MessageTemplateDao;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.sms.SmsReceipt;
import com.lin.opush.enums.AuditStatus;
import com.lin.opush.enums.MessageStatus;
import com.lin.opush.enums.MessageTemplateType;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.mq.SendMqService;
import com.lin.opush.service.MessageTemplateService;
import com.lin.opush.vo.BasicResultVO;
import com.lin.opush.vo.MessageTemplateParam;
import com.lin.opush.xxljob.domain.XxlJobInfo;
import com.lin.opush.xxljob.service.XxlJobService;
import com.lin.opush.xxljob.utils.XxlJobUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 消息模板管理服务接口实现
 */
@Service
public class MessageTemplateServiceImpl implements MessageTemplateService {
    /**
     * 消息模板 Dao
     */
    @Autowired
    private MessageTemplateDao messageTemplateDao;

    /**
     * 发送MQ服务
     */
    @Autowired
    private SendMqService sendMqService;

    /**
     * UniSMS短信回执发送MQ的Topic
     */
    @Value("${opush.business.receipt.topic.name}")
    private String sendReceiptTopic;

    /**
     * 定时任务服务
     */
    @Autowired
    private XxlJobService xxlJobService;

    /**
     * 定时任务工具类
     */
    @Autowired
    private XxlJobUtils xxlJobUtils;

    /**
     * 查询消息模板列表
     * @param param 消息模板参数对象
     * @return 分页消息模板列表
     */
    @Override
    public Page<MessageTemplate> queryList(MessageTemplateParam param) {
        // 判断页码和是否合理（page > 0, perPage >= 1）并生成分页请求对象
        PageRequest pageRequest = PageRequest.of(param.getPage() - 1, param.getPerPage());
        // 分页查询
        // root: 代表查询的根对象，即MessageTemplate（消息模板）
        // criteriaQuery: 顶层查询对象，sql语句关键字，用于自定义查询方式（基本不用）
        // criteriaBuilder: 查询构造器，封装很多的查询条件
        return messageTemplateDao.findAll((Specification<MessageTemplate>) (root, criteriaQuery, criteriaBuilder) -> {
            // 搜索条件列表
            List<Predicate> predicateList = new ArrayList<>();
            // 添加搜索条件（模板名、模板类型、发送渠道、消息类型、未删除、创建者）
            if (StrUtil.isNotBlank(param.getName())) {
                predicateList.add(criteriaBuilder.like(root.get("name").as(String.class), "%" + param.getName() + "%"));
            }
            if (StrUtil.isNotBlank(param.getTemplateType())) {
                predicateList.add(criteriaBuilder.equal(root.get("templateType").as(String.class), param.getTemplateType()));
            }
            if (StrUtil.isNotBlank(param.getSendChannel())) {
                predicateList.add(criteriaBuilder.equal(root.get("sendChannel").as(String.class), param.getSendChannel()));
            }
            if (StrUtil.isNotBlank(param.getMsgType())) {
                predicateList.add(criteriaBuilder.equal(root.get("msgType").as(String.class), param.getMsgType()));
            }
            predicateList.add(criteriaBuilder.equal(root.get("isDeleted").as(Integer.class), CommonConstant.FALSE));
            predicateList.add(criteriaBuilder.equal(root.get("creator").as(String.class), param.getCreator()));
            Predicate[] predicate = new Predicate[predicateList.size()];
            // 执行分页查询
            // or查询：criteriaBuilder.or(predicate);
            criteriaQuery.where(criteriaBuilder.and(predicateList.toArray(predicate)));
            // 查询内容按更新时间排序
            criteriaQuery.orderBy(criteriaBuilder.desc(root.get("updated")));
            // 返回与where子句限制相对应的predicates，如果未指定限制，则返回null
            return criteriaQuery.getRestriction();
        }, pageRequest);
    }

    /**
     * 保存/修改消息模板
     * @param messageTemplate 消息模板
     * @return
     */
    @Override
    public MessageTemplate saveOrUpdate(MessageTemplate messageTemplate) {
        // 判断消息模板id是否存在
        if (Objects.isNull(messageTemplate.getId())) {
            // 初始化消息模板状态
            initStatus(messageTemplate);
        } else {
            // 更新消息模板状态和该模板对应定时任务信息【若为定时类型模板并启动过任务】
            updateStatusAndTimedJobInfo(messageTemplate);
        }
        // 更新时间
        messageTemplate.setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        // 保存消息模板
        return messageTemplateDao.save(messageTemplate);
    }

    /**
     * 初始化消息模板状态
     * @param messageTemplate
     */
    private void initStatus(MessageTemplate messageTemplate) {
                 // 工单id【暂时为""，即无需审核】
        messageTemplate.setFlowId(StrUtil.EMPTY)
                // 设置消息模板状态和消息模板审核状态
                .setMsgStatus(MessageStatus.INIT.getCode())
                .setAuditStatus(AuditStatus.WAIT_AUDIT.getCode())
                // 设置业务方【未填则默认为opush公众号】
                .setTeam(StrUtil.isBlank(messageTemplate.getTeam()) ?
                                            OpushConstant.DEFAULT_TEAM : messageTemplate.getTeam())
                // 设置审核者【暂时为opush，即无需审核】
                .setAuditor(StrUtil.isBlank(messageTemplate.getAuditor()) ?
                                            OpushConstant.DEFAULT_AUDITOR : messageTemplate.getAuditor())
                // 设置创建时间【当前时间】
                .setCreated(Math.toIntExact(DateUtil.currentSeconds()))
                // 设置为未删除【0】
                .setIsDeleted(CommonConstant.FALSE);
    }

    /**
     * 更新消息模板状态并修改定时任务信息【若为定时类型模板并启动过任务】
     * @param messageTemplate 需更新的消息模板
     */
    private void updateStatusAndTimedJobInfo(MessageTemplate messageTemplate) {
        // 更新者、模板状态、审核状态
        messageTemplate.setUpdator(messageTemplate.getUpdator())
                        .setMsgStatus(MessageStatus.INIT.getCode())
                        .setAuditStatus(AuditStatus.WAIT_AUDIT.getCode());
        // 若该模板为定时类型模板并模板对应定时任务已存在调度中心，则更新定时任务信息
        if (MessageTemplateType.TIMING.getCode().equals(messageTemplate.getTemplateType()) &&
                                            Objects.nonNull(messageTemplate.getTimedJobId())) {
            // 创建任务信息
            XxlJobInfo xxlJobInfo = xxlJobUtils.buildXxlJobInfo(messageTemplate);
            // 保存并暂停定时任务
            xxlJobService.saveTimedJob(xxlJobInfo);
            xxlJobService.stopTimedJob(messageTemplate.getTimedJobId());
        }
    }

    /**
     * 查找id对应消息模板
     * @param id 消息模板id
     * @return 消息模板
     */
    @Override
    public MessageTemplate queryById(Long id) {
        // 模板不存在则返回null
        return messageTemplateDao.findById(id).orElse(null);
    }

    /**
     * 保存UniSMS短信回执
     * @param receipt UniSMS短信回执
     * @return 状态码
     */
    @Override
    public void saveUniSMSReceipt(SmsReceipt receipt) {
        // 将回执发送到MQ
        sendMqService.send(sendReceiptTopic, JSON.toJSONString(receipt,
                                    new SerializerFeature[]{SerializerFeature.WriteClassName}));
    }

    /**
     * 复制id对应的消息模板
     * @param id 要复制的消息模板对应id
     */
    @Override
    public void copy(Long id) {
        // 模板不存在则返回null
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).orElse(null);
        if (Objects.nonNull(messageTemplate)) {
            // 通过工具包方法拷贝深对象，并将消息模板id和定时任务id置为null
            MessageTemplate clone = ObjectUtil.clone(messageTemplate).setId(null).setTimedJobId(null);
            messageTemplateDao.save(clone);
        }
    }

    /**
     * 单条/批量删除消息模板
     * 【若消息模板对应的定时任务已创建，需一起删除】
     * @param ids 单条/批量删除（删除消息模板对应id列表）
     */
    @Override
    public void deleteByIds(List<Long> ids) {
        Iterable<MessageTemplate> messageTemplates = messageTemplateDao.findAllById(ids);
        // 将所有消息模板的isDeleted属性置为1，表示该消息模板已删除（软删除）
        messageTemplates.forEach(messageTemplate -> messageTemplate.setIsDeleted(CommonConstant.TRUE));
        for (MessageTemplate messageTemplate : messageTemplates) {
            // 若模板为定时任务类型且存在定时任务【定时任务id不为空】，需删除对应定时任务
            if (Objects.nonNull(messageTemplate.getTimedJobId()) && messageTemplate.getTimedJobId() > 0) {
                xxlJobService.deleteTimedJob(messageTemplate.getTimedJobId());
            }
        }
        messageTemplateDao.saveAll(messageTemplates);
    }

    /**
     * 启动模板对应的定时任务
     * @param id 消息模板id
     * @return 启动结果【成功/失败】
     */
    @Override
    public BasicResultVO startTimedJob(Long id) {
        // 根据模板id获取消息模板信息
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).orElse(null);
        if (Objects.isNull(messageTemplate)) {
            return BasicResultVO.fail();
        }
        // 动态创建或更新定时任务【定时任务id为空则为创建定时任务，不为空则为更新定时任务】
        XxlJobInfo xxlJobInfo = xxlJobUtils.buildXxlJobInfo(messageTemplate);
        Integer timedJobId = messageTemplate.getTimedJobId();
        // 保存定时任务【若为第一次启动定时任务，会返回定时任务id】
        BasicResultVO basicResultVO = xxlJobService.saveTimedJob(xxlJobInfo);
        if (Objects.isNull(timedJobId) &&
                RespStatusEnum.SUCCESS.getCode().equals(basicResultVO.getStatus()) &&
                Objects.nonNull(basicResultVO.getData())) {
            timedJobId = Integer.valueOf(String.valueOf(basicResultVO.getData()));
        }
        // 启动定时任务
        if (Objects.nonNull(timedJobId)) {
            xxlJobService.startTimedJob(timedJobId);
            MessageTemplate clone = ObjectUtil.clone(messageTemplate)
                                            .setMsgStatus(MessageStatus.RUN.getCode())
                                            .setTimedJobId(timedJobId)
                                            .setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
            messageTemplateDao.save(clone);
            return BasicResultVO.success();
        }
        return BasicResultVO.fail();
    }

    /**
     * 暂停模板对应的定时任务
     * @param id 消息模板id
     * @return 暂停结果
     */
    @Override
    public BasicResultVO stopTimedJob(Long id) {
        // 修改模板状态
        MessageTemplate messageTemplate = messageTemplateDao.findById(id).orElse(null);
        if (Objects.isNull(messageTemplate)) {
            return BasicResultVO.fail();
        }
        MessageTemplate clone = ObjectUtil.clone(messageTemplate)
                                        .setMsgStatus(MessageStatus.STOP.getCode())
                                        .setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        messageTemplateDao.save(clone);
        // 暂停定时任务
        return xxlJobService.stopTimedJob(clone.getTimedJobId());
    }
}
