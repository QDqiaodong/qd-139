package com.polar.experience.enums;

/**
 * 耐寒送检单状态：只允许“待接单”和“已修复”两种，不设其他流转状态。
 */
public enum InspectionStatus {
    /** 待接单：馆务已建单送检，等待维修方接单修复 */
    PENDING,
    /** 已修复：维修完成，状态终态，不再流转 */
    REPAIRED
}
