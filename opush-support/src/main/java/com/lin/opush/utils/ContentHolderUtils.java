package com.lin.opush.utils;

import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.PropertyPlaceholderHelper;

import java.text.MessageFormat;
import java.util.Map;

/**
 * 消息内容的占位符替换
 * opush占位符格式{$var}
 */
public class ContentHolderUtils {
    /**
     * 占位符前缀
     */
    public static final String PLACE_HOLDER_PREFIX = "{$";
    /**
     * 占位符后缀
     */
    public static final String PLACE_HOLDER_SUFFIX = "}";
    /**
     * 评估上下文
     */
    private static final StandardEvaluationContext EVALUATION_CONTEXT;
    /**
     * Spring内置的占位符解析器
     */
    private static final PropertyPlaceholderHelper PROPERTY_PLACEHOLDER_HELPER = new PropertyPlaceholderHelper(
                                                                        PLACE_HOLDER_PREFIX, PLACE_HOLDER_SUFFIX);

    static {
        EVALUATION_CONTEXT = new StandardEvaluationContext();
        // 添加属性访问器(Map)
        EVALUATION_CONTEXT.addPropertyAccessor(new MapAccessor());
    }

    /**
     * 替换占位符
     * 如果消息内容参数值不含形为 {$...} 的占位符，则直接返回消息内容参数，即发送消息模板中已填写好的内容
     * 如果消息内容参数值含形为 {$...} 的占位符，则解析出...的内容，并用该内容匹配下发参数列表对应参数值返回
     * @param msgContentParamValue 消息内容参数
     * @param variables 下发参数列表
     * @return
     */
    public static String replacePlaceHolder(final String msgContentParamValue, final Map<String, String> variables) {
        String replacedPushContent = PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(msgContentParamValue,
                                            new CustomPlaceholderResolver(msgContentParamValue, variables));
        return replacedPushContent;
    }

    /**
     * 自定义占位符解析器
     */
    private static class CustomPlaceholderResolver implements PropertyPlaceholderHelper.PlaceholderResolver {
        /**
         * 消息内容参数值
         */
        private final String msgContentParamValue;
        /**
         * 下发参数列表
         */
        private final Map<String, String> variables;

        public CustomPlaceholderResolver(String msgContentParamValue, Map<String, String> variables) {
            super();
            this.msgContentParamValue = msgContentParamValue;
            this.variables = variables;
        }

        /**
         * 传入{$...}中的...内容，用该内容匹配下发参数列表对应参数值返回【找不到则抛出不合法参数异常】
         * @param placeholderName {$...}中的...内容
         * @return
         */
        @Override
        public String resolvePlaceholder(String placeholderName) {
            String value = variables.get(placeholderName);
            // 找不到解析{$...}中...内容对应的参数
            if (null == value) {
                String errorStr = MessageFormat.format("template:{0} require param:{1},but not exist! paramMap:{2}",
                        msgContentParamValue, placeholderName, variables.toString());
                throw new IllegalArgumentException(errorStr);
            }
            return value;
        }
    }
}
