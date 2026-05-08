package com.pasan.material.service;

import com.pasan.material.domain.dto.MaterialUploadDTO;
import com.pasan.material.domain.po.Material;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IMaterialService {
    Material upload(MultipartFile file, MaterialUploadDTO dto);
    List<Material> list(String subject, int page, int size);
}
