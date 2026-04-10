package com.example.backend.model.dto.groupKaoqin;

import com.example.backend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class GroupKaoqinQuertRequest extends PageRequest implements Serializable {
    /**
     * 考勤组编号 (钉钉groupId)
     */
    private String groupId;

    /**
     * 考勤组名字
     */
    private String groupName;

    private static final long serialVersionUID = 1L;
}
