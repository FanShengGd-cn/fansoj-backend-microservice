package com.fansheng.fansojbackendquestionservice.controller;


import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fansheng.fansojbackendcommon.annotation.AuthCheck;
import com.fansheng.fansojbackendcommon.common.BaseResponse;
import com.fansheng.fansojbackendcommon.common.DeleteRequest;
import com.fansheng.fansojbackendcommon.common.ErrorCode;
import com.fansheng.fansojbackendcommon.common.ResultUtils;
import com.fansheng.fansojbackendcommon.constant.UserConstant;
import com.fansheng.fansojbackendcommon.exception.BusinessException;
import com.fansheng.fansojbackendcommon.exception.ThrowUtils;
import com.fansheng.fansojbackendmodel.model.dto.question.*;
import com.fansheng.fansojbackendmodel.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.fansheng.fansojbackendmodel.model.dto.questionSubmit.QuestionSubmitQueryRequest;
import com.fansheng.fansojbackendmodel.model.entity.Question;
import com.fansheng.fansojbackendmodel.model.entity.QuestionSubmit;
import com.fansheng.fansojbackendmodel.model.entity.User;
import com.fansheng.fansojbackendmodel.model.vo.QuestionSubmitVO;
import com.fansheng.fansojbackendmodel.model.vo.QuestionVO;
import com.fansheng.fansojbackendquestionservice.service.QuestionService;
import com.fansheng.fansojbackendquestionservice.service.QuestionSubmitService;
import com.fansheng.fansojbackendserviceclient.service.JudgeFeignClient;
import com.fansheng.fansojbackendserviceclient.service.UserFeignClient;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 问题接口
 *
 * @author fansheng
 */
@RestController
@RequestMapping("/")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private UserFeignClient userFeignClient;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        List<String> tags = questionAddRequest.getTags();

        if(judgeConfig != null){
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        if(judgeCase != null){
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        questionService.validQuestion(question, true);
        User loginUser = userFeignClient.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userFeignClient.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userFeignClient.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param QuestionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest QuestionUpdateRequest) {
        if (QuestionUpdateRequest == null || QuestionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionUpdateRequest, Question);
        List<String> tags = QuestionUpdateRequest.getTags();
        if (tags != null) {
            Question.setTags(GSON.toJson(tags));
        }
        // 参数校验
        questionService.validQuestion(Question, false);
        long id = QuestionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(Question);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question Question = questionService.getById(id);
        if (Question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(Question, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param QuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest QuestionQueryRequest,
                                                               HttpServletRequest request) {
        long current = QuestionQueryRequest.getCurrent();
        long size = QuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> QuestionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(QuestionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(QuestionPage, request));
    }

    /**
     * 分页获取当前问题创建的资源列表
     *
     * @param QuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest QuestionQueryRequest,
            HttpServletRequest request) {
        if (QuestionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userFeignClient.getLoginUser(request);
        QuestionQueryRequest.setUserId(loginUser.getId());
        long current = QuestionQueryRequest.getCurrent();
        long size = QuestionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> QuestionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(QuestionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(QuestionPage, request));
    }

    // endregion



    /**
     * 编辑（问题）
     *
     * @param QuestionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest QuestionEditRequest, HttpServletRequest request) {
        if (QuestionEditRequest == null || QuestionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question Question = new Question();
        BeanUtils.copyProperties(QuestionEditRequest, Question);
        // 参数校验
        questionService.validQuestion(Question, false);
        User loginUser = userFeignClient.getLoginUser(request);
        long id = QuestionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userFeignClient.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(Question);
        return ResultUtils.success(result);
    }

    /**
     * 管理员接口，不做脱敏处理
     * @param QuestionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionVO>> listQuestionByPage(@RequestBody QuestionQueryRequest QuestionQueryRequest,
                                                               HttpServletRequest request) {
        long current = QuestionQueryRequest.getCurrent();
        long size = QuestionQueryRequest.getPageSize();
        // 限制爬虫
        Page<Question> QuestionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(QuestionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(QuestionPage, request));
    }

    @PostMapping("/question_submit/judge")
    public Long doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userFeignClient.getLoginUser(request);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return questionSubmitId;
    }

    /**
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();

        // 限制爬虫
        Page<QuestionSubmit> QuestionPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        final User loginUser = userFeignClient.getLoginUser(request);
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(QuestionPage, loginUser));
    }

}
