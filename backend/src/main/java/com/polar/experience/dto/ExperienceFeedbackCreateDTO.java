package com.polar.experience.dto;

import com.polar.experience.enums.NumbnessLevel;
import com.polar.experience.enums.TempFeeling;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 散场登记入参：选场次，写游客称呼、室温感觉、手脚发麻情况和登记人。
 */
@Data
public class ExperienceFeedbackCreateDTO {

    @NotNull(message = "请选择体验场次")
    private Long sessionId;

    @NotBlank(message = "请填写游客称呼")
    @Size(max = 50, message = "游客称呼不能超过50字")
    private String visitorName;

    @NotNull(message = "请选择室温感觉")
    private TempFeeling tempFeeling;

    @NotNull(message = "请选择手脚是否发麻")
    private NumbnessLevel numbness;

    @NotBlank(message = "请填写登记人")
    @Size(max = 50, message = "登记人不能超过50字")
    private String registrar;

    /** 登记日期，不传默认当天 */
    private LocalDate feedbackDate;
}
