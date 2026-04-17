package com.example.backend.service;

import com.example.backend.model.dto.groupKaoqin.GroupKaoqinDTO;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinQuertRequest;
import com.example.backend.model.vo.groupKaovo.GroupKaoqinVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.example.backend.model.entity.GroupKaoqin;

import java.util.List;

/**
 *  服务层。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
public interface GroupKaoqinService extends IService<GroupKaoqin> {

    /**
     * 插入考勤组信息
     * @param resultList 考勤组列表
     * @return 封装后的考勤组列表
     */
    Integer insertGroupList(List<GroupKaoqinDTO> resultList);

    /**
     * 分页查询函数
     * @param groupKaoqinQuertRequest
     * @return
     */
    QueryWrapper getQueryWrapper(GroupKaoqinQuertRequest groupKaoqinQuertRequest);

    /**
     * 封装考勤组列表
     * @param records 考勤组列表
     * @return 封装后的考勤组列表
     */
    List<GroupKaoqinVO> getGrouKaoqinList(List<GroupKaoqin> records);

    /**
     * 封装考勤组对象
     * @param groupKaoqin 未封装考勤组
     * @return 封装后考勤组
     */
    GroupKaoqinVO getGrouKaoqinVO(GroupKaoqin groupKaoqin);
}
