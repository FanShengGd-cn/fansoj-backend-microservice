package com.fansheng.fansojbackendserviceclient.service;


import com.fansheng.fansojbackendmodel.model.entity.Question;
import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
* @author fansheng
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-12-22 09:17:51
*/
@FeignClient(name = "fansoj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {

    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam(value = "questionId") Long questionId);

    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam(value = "questionSubmitId") Long questionSubmitId);

    @PostMapping("/question_submit/update")
    Boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);


}
