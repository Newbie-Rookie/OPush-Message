package com.lin.opush.action;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Throwables;

import com.lin.opush.constants.CommonConstant;
import com.lin.opush.dao.MessageTemplateDao;
import com.lin.opush.domain.MessageParam;
import com.lin.opush.domain.MessageTemplate;
import com.lin.opush.domain.TaskInfo;
import com.lin.opush.dto.model.ContentModel;
import com.lin.opush.enums.BusinessCode;
import com.lin.opush.enums.ChannelType;
import com.lin.opush.enums.RespStatusEnum;
import com.lin.opush.chain.ExecutionAction;
import com.lin.opush.chain.ExecutionChainContext;
import com.lin.opush.domain.SendTaskModel;
import com.lin.opush.utils.ContentHolderUtils;
import com.lin.opush.utils.TaskInfoUtils;
import com.lin.opush.vo.BasicResultVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 组装参数
 */
@Slf4j
@Service
public class AssembleParamAction implements ExecutionAction<SendTaskModel> {

    private static final String LINK_NAME = "url";

    @Autowired
    private MessageTemplateDao messageTemplateDao;

    @Override
    public void execute(ExecutionChainContext<SendTaskModel> context) {
        // 发送消息任务模型
        SendTaskModel sendTaskModel = context.getExecutionChainDataModel();
        // 模板id
        Long messageTemplateId = sendTaskModel.getMessageTemplateId();
        try {
            // 根据模板id查询数据库中完整模板信息
            Optional<MessageTemplate> messageTemplate = messageTemplateDao.findById(messageTemplateId);
            // 模板id对应模板是否存在（messageTemplate不为空且isDeleted属性为0(未删除)）
            if (!messageTemplate.isPresent() || messageTemplate.get().getIsDeleted().equals(CommonConstant.TRUE)) {
                context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.TEMPLATE_NOT_FOUND));
                return;
            }
            // 根据消息业务类型(send / recall)进行不同处理
            if (BusinessCode.SEND.getCode().equals(context.getCode())) {
                // 组装发送任务列表
                List<TaskInfo> taskInfos = assembleTaskInfo(sendTaskModel, messageTemplate.get());
                sendTaskModel.setTaskInfo(taskInfos);
            } else if (BusinessCode.RECALL.getCode().equals(context.getCode())) {
                // 设置消息模板
                sendTaskModel.setMessageTemplate(messageTemplate.get());
            }
        } catch (Exception e) {
            context.setNeedBreak(true).setResponse(BasicResultVO.fail(RespStatusEnum.SERVICE_ERROR));
            log.error("assemble task fail! templateId:{}, e:{}", messageTemplateId, Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 组装TaskInfo任务消息列表【send】
     * 同一个模板可能发多条消息【请求参数列表(接收者[多个用逗号隔开] + 消息参数[Map]) → 任务消息列表】
     * @param sendTaskModel 数据模型
     * @param messageTemplate 模板详细信息
     */
    private List<TaskInfo> assembleTaskInfo(SendTaskModel sendTaskModel, MessageTemplate messageTemplate) {
        // 请求参数列表
        List<MessageParam> messageParamList = sendTaskModel.getMessageParamList();
        // 任务消息列表
        List<TaskInfo> taskInfoList = new ArrayList<>();
        // 遍历请求参数列表
        for (MessageParam messageParam : messageParamList) {
            TaskInfo taskInfo = TaskInfo.builder()
                    // 模板id
                    .messageTemplateId(messageTemplate.getId())
                    // 生成业务id（消息模板类型【前2位】 + 消息模板id【3 ~ 8位】 + 当天日期【后8位】)
                    .businessId(TaskInfoUtils.generateBusinessId(messageTemplate.getId(), messageTemplate.getTemplateType()))
                    // 接收者（多个接收者用逗号隔开）
                    .receiver(new HashSet<>(Arrays.asList(messageParam.getReceiver().split(CommonConstant.COMMA))))
                    // 接收者id类型、发送渠道、模板类型、消息类型、渠道账号（标识同一渠道下的不同账号）
                    .idType(messageTemplate.getIdType())
                    .sendChannel(messageTemplate.getSendChannel())
                    .templateType(messageTemplate.getTemplateType())
                    .msgType(messageTemplate.getMsgType())
                    .sendAccount(messageTemplate.getSendAccount())
                    // 组装发送文案模型（不同发送渠道有不同的发送文案模型）
                    .contentModel(getContentModelValue(messageTemplate, messageParam))
                    .creator(sendTaskModel.getCreator()).build();
            taskInfoList.add(taskInfo);
        }
        return taskInfoList;
    }

    /**
     * 获取发送文案模型，将请求参数替换模板msgContent中的对应占位符信息
     * @param messageTemplate 消息模板详细信息
     * @param messageParam 请求参数
     * @return
     */
    private static ContentModel getContentModelValue(MessageTemplate messageTemplate, MessageParam messageParam) {
        // 获取发送渠道，每个发送渠道有对应的发送文案模型
        Integer sendChannel = messageTemplate.getSendChannel();
        Class<? extends ContentModel> contentModelClass = ChannelType.getChanelModelClassByCode(sendChannel);
        // 获取请求参数和模板中的发送内容(msgContent)
        Map<String, String> variables = messageParam.getVariables();
        JSONObject jsonObject = JSON.parseObject(messageTemplate.getMsgContent());
        // 通过反射组装发送文案模型
        // 获取发送文案模型中的所有属性
        Field[] fields = ReflectUtil.getFields(contentModelClass);
        // 创建发送文案模型对象
        ContentModel contentModel = ReflectUtil.newInstance(contentModelClass);
        for (Field field : fields) {
            String msgContentParamValue = jsonObject.getString(field.getName());
            // 模型属性名对应模板发送内容的参数不为空
            if (StrUtil.isNotBlank(msgContentParamValue)) {
                // 获取赋值给模型属性的属性值（模板发送内容 / 请求参数，解析模板发送内容是否存在占位符，存在则替换）
                String resultValue = ContentHolderUtils.replacePlaceHolder(msgContentParamValue, variables);
                // 赋值内容是否为JSON字符串，若为JSON字符串则转为JSON对象
                Object resultObj = JSONUtil.isTypeJSONObject(resultValue) ? JSONUtil.toBean(resultValue, field.getType()) : resultValue;
                ReflectUtil.setFieldValue(contentModel, field, resultObj);
            }
        }
        // 若消息内容中存在url字段，则在url拼接对应的埋点参数
        String url = (String) ReflectUtil.getFieldValue(contentModel, LINK_NAME);
        if (StrUtil.isNotBlank(url)) {
            String resultUrl = TaskInfoUtils.generateUrl(url, messageTemplate.getId(), messageTemplate.getTemplateType());
            ReflectUtil.setFieldValue(contentModel, LINK_NAME, resultUrl);
        }
        return contentModel;
    }
}
