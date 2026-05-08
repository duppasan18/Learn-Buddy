package com.pasan.material.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pasan.material.domain.po.Material;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaterialMapper extends BaseMapper<Material> {
}
