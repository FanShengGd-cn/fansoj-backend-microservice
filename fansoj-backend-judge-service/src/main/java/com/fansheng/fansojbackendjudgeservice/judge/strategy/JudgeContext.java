package com.fansheng.fansojbackendjudgeservice.judge.strategy;


import com.fansheng.fansojbackendmodel.model.dto.codesandbox.JudgeInfo;
import com.fansheng.fansojbackendmodel.model.dto.question.JudgeCase;
import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import com.fansheng.fansojbackendmodel.model.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class JudgeContext {
    private JudgeInfo judgeInfo;
    private List<String> inputList;
    private List<String> outputList;
    private List<JudgeCase> judgeCaseList;
    private Question question;
    private QuestionSubmit questionSubmit;
}
