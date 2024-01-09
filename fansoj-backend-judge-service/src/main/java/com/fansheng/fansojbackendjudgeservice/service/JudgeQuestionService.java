package com.fansheng.fansojbackendjudgeservice.service;


import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;

public interface JudgeQuestionService {
    QuestionSubmit doJudge(Long questionSubmitId);
}
