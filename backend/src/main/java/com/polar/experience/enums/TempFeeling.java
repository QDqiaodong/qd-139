package com.polar.experience.enums;

/**
 * 室温感觉：游客散场时随口反馈的冷不冷，只分四档，不做更细的量化。
 */
public enum TempFeeling {
    /** 很冷：明显受不了，班组要重点关注 */
    VERY_COLD,
    /** 有点冷：能感觉到冷，但还能坚持 */
    COLD,
    /** 适中：不冷也不热 */
    COMFORTABLE,
    /** 不冷：全程没觉得冷 */
    NOT_COLD
}
