package com.example.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.example.backend.exception.BusinessException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.ThrowUtils;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinDTO;
import com.example.backend.model.dto.groupKaoqin.GroupKaoqinQuertRequest;
import com.example.backend.model.vo.groupKaovo.GroupKaoqinVO;
import com.example.backend.model.vo.sysUservo.SysUserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.backend.model.entity.GroupKaoqin;
import com.example.backend.mapper.GroupKaoqinMapper;
import com.example.backend.service.GroupKaoqinService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 *
 * @author <a href="https://github.com/yyffyyq">代码制造者yfy</a>
 */
@Service
public class GroupKaoqinServiceImpl extends ServiceImpl<GroupKaoqinMapper, GroupKaoqin>  implements GroupKaoqinService{

    @Resource
    private GroupKaoqinMapper groupKaoqinMapper;

    /**
     * 插入考勤组信息
     * @param resultList 考勤组列表
     * @return 返回成功插入数量
     */
    @Override
    public Integer insertGroupList(List<GroupKaoqinDTO> resultList) {

        ThrowUtils.throwIf(resultList == null, ErrorCode.PARAMS_ERROR);

        Set<String> existingIds = getExistingGroupIdSet();
        List<GroupKaoqin> insertList = new ArrayList<>();

        // 获取数据表中考勤组的 id 列表，用于之后的查询，这里我选择采用hashmap的方法，
        // 之后想要插入考勤组的时候，先拿 group_id 去查询一下 map 看看是true 还是false
        for (GroupKaoqinDTO groupKaoqinDTO : resultList) {
            String currentGroupId = groupKaoqinDTO.getGroupId();

            // 通过hashmap判断，并加入到插入列表中
            if (!existingIds.contains(currentGroupId)) {
                GroupKaoqin groupKaoqin = new GroupKaoqin();
                groupKaoqin.setGroupId(currentGroupId);
                groupKaoqin.setGroupName(groupKaoqinDTO.getGroupName());
                groupKaoqin.setCreateTime(LocalDateTime.now());
                groupKaoqin.setUpdateTime(LocalDateTime.now());
                groupKaoqin.setIsDeleted(false);
                insertList.add(groupKaoqin);
            }else{
                // 如果存在就跳过，这里可以加入操作日志
            }
        }
        Integer result = 0;
        // 插入列表中所有数据
        if (insertList.size() > 0) {
            result = groupKaoqinMapper.insertBatch(insertList);
        }
        return result;
    }

    /**
     * 查询获取数据表中 group_id 用于存入数据表前查询避免重复插入
     * @return 返回 hashSet<> 类型
     */
    public Set<String> getExistingGroupIdSet(){

        // 查询一次数据库，存入数组中，用于返回
        QueryWrapper queryWrapper = QueryWrapper.create()
                .select(GroupKaoqin::getGroupId);
        List<String> idList = groupKaoqinMapper.selectObjectListByQueryAs(queryWrapper, String.class);

        // 返回存入 HashSet
        return new HashSet<>(idList);
    }


    @Override
    public QueryWrapper getQueryWrapper(GroupKaoqinQuertRequest groupKaoqinQuertRequest) {

        // 1. 判断分页查询请求是否为空
        ThrowUtils.throwIf(groupKaoqinQuertRequest == null, ErrorCode.PARAMS_ERROR);

        // 2. 获取请求体内的数据，并构造sql请求
        String groupId = groupKaoqinQuertRequest.getGroupId();
        String groupName = groupKaoqinQuertRequest.getGroupName();
        String sortField = groupKaoqinQuertRequest.getSortField();
        String sortOrder = groupKaoqinQuertRequest.getSortOrder();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("group_id", groupId, StrUtil.isNotBlank(groupId))
                .like("group_name", groupName, StrUtil.isNotBlank(groupName));
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }

        // 3. 返回sql语句
        return queryWrapper;
    }

    /**
     * 封装考勤组列表
     * @param records 考勤组列表
     * @return List<GroupKaoqinVO>
     */
    @Override
    public List<GroupKaoqinVO> getGrouKaoqinList(List<GroupKaoqin> records) {
        if(CollUtil.isEmpty(records)){
            return new ArrayList<>();
        }
        //landa表达式
        return records.stream()
                .map(this::getGrouKaoqinVO)
                .collect(Collectors.toList());
    }

    /**
     * 封装考勤组对象
     * @param groupKaoqin 未封装考勤组
     * @return GroupKaoqinVO
     */
    @Override
    public GroupKaoqinVO getGrouKaoqinVO(GroupKaoqin groupKaoqin) {
        // 判断封装对象是否为空
        if(groupKaoqin==null){
            throw new BusinessException(ErrorCode.SYS_USER_VO_LOGIN_PARAMS_ERROR);
        }

        // 创建封装对象，并赋值
        GroupKaoqinVO groupKaoqinVO = new GroupKaoqinVO();
        BeanUtils.copyProperties(groupKaoqin,groupKaoqinVO);

        // 返回封装数据
        return groupKaoqinVO;
    }

}
