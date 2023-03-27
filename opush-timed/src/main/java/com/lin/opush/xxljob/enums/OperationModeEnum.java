package com.lin.opush.xxljob.enums;

/**
 * 运行模式枚举
 * 【主要分为BEAN和CLUE模式】
 * 【BEAN模式任务以JobHandler方式维护在执行器端】
 * 【任务以源码方式维护在调度中心【目前支持java、shell、python、php、nodejs、powershell】】
 */
public enum OperationModeEnum {
    /**
     * BEAN
     */
    BEAN,
    /**
     * GLUE_JAVA
     */
    GLUE_GROOVY,
    /**
     * GLUE_SHELL
     */
    GLUE_SHELL,
    /**
     * GLUE_PYTHON
     */
    GLUE_PYTHON,
    /**
     * GLUE_PHP
     */
    GLUE_PHP,
    /**
     * GLUE_NODEJS
     */
    GLUE_NODEJS,
    /**
     * GLUE_POWERSHELL
     */
    GLUE_POWERSHELL;

    OperationModeEnum() {}
}
