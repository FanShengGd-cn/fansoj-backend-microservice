package com.fansheng.fansojbackendjudgeservice.judge.strategy;


import com.fansheng.fansojbackendmodel.model.dto.codesandbox.JudgeInfo;
import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

@Service
public class JudgeStrategyManager {
    public JudgeInfo doJudge(JudgeContext judgeContext){
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy;
        if("java".equals(language)){
            judgeStrategy = new JavaJudgeStrategyImpl();
        }else {
            judgeStrategy = new DefaultJudgeStrategyImpl();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
