package com.example.backend.model.vo.groupKaovo;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupKaoqinVO implements Serializable {
    /**
     * 考勤组编号 (钉钉groupId)
     */
    private String groupId;

    /**
     * 考勤组名字
     */
    private String groupName;
    /**
     * 是否删除 (0:正常, 1:已删除)
     */
    private Boolean isDeleted;

}
