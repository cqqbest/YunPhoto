package com.cq.YunPhoto.Controller;


import cn.hutool.http.HttpRequest;
import com.cq.YunPhoto.Common.BaseResponse;
import com.cq.YunPhoto.Common.ResultUtils;
import com.cq.YunPhoto.Exception.ErrorCode;
import com.cq.YunPhoto.Exception.ThrowUtils;
import com.cq.YunPhoto.Model.dto.Space.Analyze.*;
import com.cq.YunPhoto.Model.entity.User;
import com.cq.YunPhoto.Model.vo.Space.Analyze.*;
import com.cq.YunPhoto.Model.vo.Space.SpaceVo;
import com.cq.YunPhoto.service.PictureService;
import com.cq.YunPhoto.service.SpaceAnalyzeService;
import com.cq.YunPhoto.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/spaceAnalyze")
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;

    /**
     * 空间资源使用分析
     */
    @PostMapping("/spaceUsageAnalyze")
    public BaseResponse<SpaceUsageAnalyzeResponse> spaceUsageAnalyze(@RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, HttpServletRequest request) {
        //判空
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = spaceAnalyzeService.spaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        //返回结果
        return ResultUtils.success(spaceUsageAnalyzeResponse);
    }
    /**
     * 空间图片分类分析
     */
    @PostMapping("/spaceCateGoryAnalyze")
    public BaseResponse<List<SpaceCateGoryAnalyzeResponse>> spaceCateGoryAnalyze(@RequestBody SpaceCateGoryAnalyzeRequest spaceCateGoryAnalyzeRequest, HttpServletRequest request) {
        //判空
        ThrowUtils.throwIf(spaceCateGoryAnalyzeRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        List<SpaceCateGoryAnalyzeResponse> spaceCateGoryAnalyzeResponse = spaceAnalyzeService.spaceCateGoryAnalyze(spaceCateGoryAnalyzeRequest, loginUser);
        //返回结果
        return ResultUtils.success(spaceCateGoryAnalyzeResponse);

    }

    /**
     * 空间图片标签分析
     */
    @PostMapping("/spaceTagAnalyze")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> spaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        //判空
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        List<SpaceTagAnalyzeResponse> spaceTagAnalyzeResponse = spaceAnalyzeService.spaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        //返回结果
        return ResultUtils.success(spaceTagAnalyzeResponse);
    }

    /**
     * 空间图片大小分析
     */
    @PostMapping("/spaceSizeAnalyze")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> spaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        //判空
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyzeResponse = spaceAnalyzeService.spaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        //返回结果
        return ResultUtils.success(spaceSizeAnalyzeResponse);

    }

    /**
     * 用户上传行为分析
     */
    @PostMapping("/userUploadAnalyze")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> userUploadAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {

        //判空
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        List<SpaceUserAnalyzeResponse> spaceUserAnalyzeResponse = spaceAnalyzeService.spaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        //返回结果
        return ResultUtils.success(spaceUserAnalyzeResponse);
    }

    /**
     * 空间使用排行分析
     */
    @PostMapping("/spaceRankAnalyze")
    public BaseResponse<List<SpaceVo>> spaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
        //判空
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.NOT_FOUND_ERROR, "请求参数为空");
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        //调用接口
        List<SpaceVo> spaceRankAnalyzeResponse = spaceAnalyzeService.spaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        //返回结果
        return ResultUtils.success(spaceRankAnalyzeResponse);
    }


}
