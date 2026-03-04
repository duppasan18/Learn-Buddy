package com.pasan.learnbuddy.service.impl;

import com.pasan.learnbuddy.domain.po.Location;
import com.pasan.learnbuddy.mapper.LocationMapper;
import com.pasan.learnbuddy.service.ILocationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 自习地点表 服务实现类
 * </p>
 *
 * @author pasan
 * @since 2026-03-04
 */
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements ILocationService {

}
