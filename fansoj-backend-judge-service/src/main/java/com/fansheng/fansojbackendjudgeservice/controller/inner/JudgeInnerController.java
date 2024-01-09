package com.fansheng.fansojbackendjudgeservice.controller.inner;

import com.fansheng.fansojbackendjudgeservice.service.JudgeQuestionService;
import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import com.fansheng.fansojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {
    @Resource
    private JudgeQuestionService judgeQuestionService;

    @Override
    @PostMapping("/doJudge")
    public QuestionSubmit doJudge(Long questionSubmitId) {
        return judgeQuestionService.doJudge(questionSubmitId);
    }
}
