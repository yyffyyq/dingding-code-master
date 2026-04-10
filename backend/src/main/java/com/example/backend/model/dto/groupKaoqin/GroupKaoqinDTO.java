package com.example.backend.model.dto.groupKaoqin;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupKaoqinDTO implements Serializable {
    /**
     * 考勤组编号 (钉钉groupId)
     */
    private String groupId;

    /**
     * 考勤组名字
     */
    private String groupName;

}
