package com.pasan.material.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pasan.exception.BusinessException;
import com.pasan.material.config.AliOssProperties;
import com.pasan.material.domain.dto.MaterialUploadDTO;
import com.pasan.material.domain.po.Material;
import com.pasan.material.mapper.MaterialMapper;
import com.pasan.material.service.IMaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements IMaterialService {

    private final AliOssProperties aliOssProperties;

    @Override
    public Material upload(MultipartFile file, MaterialUploadDTO dto) {
        if (file.isEmpty()) {
            throw new BusinessException("文件为空");
        }

        String name = dto.getName();
        if (name == null || name.isEmpty()) {
            name = file.getOriginalFilename();
        }
        if (name != null && name.length() > 100) {
            name = name.substring(0, 100);
        }
        // 拼接名字
        String ext = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String folder = "avatar".equals(dto.getType()) ? "avatar/" : "materials/";
        String objectName = folder + UUID.randomUUID() + ext;

        String uri;
        try {
            String accessKeyId = aliOssProperties.getAccessKeyId();
            String accessKeySecret = aliOssProperties.getAccessKeySecret();
            if (accessKeyId == null || accessKeyId.isEmpty()) {
                throw new BusinessException("OSS凭证未配置");
            }
            OSS ossClient = new OSSClientBuilder()
                    .build(aliOssProperties.getEndpoint(), accessKeyId, accessKeySecret);
            ossClient.putObject(aliOssProperties.getBucketName(), objectName, file.getInputStream());
            ossClient.shutdown();
            String endpoint = aliOssProperties.getEndpoint()
                    .replace("https://", "").replace("http://", "");
            uri = "https://" + aliOssProperties.getBucketName() + "." + endpoint + "/" + objectName;
        } catch (Exception e) {
            log.error("OSS上传失败", e);
            throw new BusinessException("文件上传失败");
        }

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Material material = new Material();
        material.setName(name);
        material.setSubject(dto.getSubject());
        material.setUri(uri);
        material.setUserId(userId);
        material.setCreateTime(LocalDateTime.now());
        material.setUpdateTime(LocalDateTime.now());
        save(material);
        return material;
    }

    @Override
    public List<Material> list(String subject, int page, int size) {
        LambdaQueryWrapper<Material> wrapper = new LambdaQueryWrapper<>();
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(Material::getSubject, subject);
        }
        wrapper.orderByDesc(Material::getCreateTime);
        Page<Material> pg = new Page<>(page, size);
        return baseMapper.selectPage(pg, wrapper).getRecords();
    }
}
