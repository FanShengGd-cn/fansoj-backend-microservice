package com.fansheng.fansojbackendmodel.model.dto.codesandbox;

import lombok.Data;

import java.util.List;

@Data
public class ExecuteQuestionResponse {
    private String message;
    private Integer status;
    private List<String> OutputList;
    private JudgeInfo judgeInfo;
}
