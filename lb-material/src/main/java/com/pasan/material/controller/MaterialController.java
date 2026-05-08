package com.pasan.material.controller;

import com.pasan.material.domain.dto.MaterialUploadDTO;
import com.pasan.material.domain.po.Material;
import com.pasan.material.service.IMaterialService;
import com.pasan.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/material")
@RequiredArgsConstructor
public class MaterialController {

    private final IMaterialService materialService;

    /**
     * 上传学习资料文件到OSS
     */
    @PostMapping("/upload")
    public Result<Material> upload(@RequestParam("file") MultipartFile file,
                                   MaterialUploadDTO dto) {
        return Result.success(materialService.upload(file, dto));
    }

    /**
     * 分页查询学习资料列表，可按学科筛选
     */
    @GetMapping("/list")
    public Result<List<Material>> list(@RequestParam(required = false) String subject,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return Result.success(materialService.list(subject, page, size));
    }

    /**
     * 获取学科列表
     */
    //todo 优化列表，从数据库读取返回
    @GetMapping("/subjects")
    public Result<List<String>> getSubjects() {
        return Result.success(List.of("计算机", "数学", "外语", "政治", "物理", "化学", "其他"));
    }
}
