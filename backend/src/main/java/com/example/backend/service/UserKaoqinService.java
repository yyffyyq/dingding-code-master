package com.example.backend.service;

import com.example.backend.model.dto.UserKaoqinDTO;
import com.mybatisflex.core.service.IService;
import com.example.backend.model.entity.UserKaoqin;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface UserKaoqinService extends IService<UserKaoqin> {


    Integer insertGroupList(List<UserKaoqinDTO> resultList);
}
