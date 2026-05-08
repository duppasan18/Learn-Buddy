package com.pasan.invitation.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long inviteId;

    private Long senderId;

    private String senderName;

    /** 0文本 1图片 2文件 3系统通知 */
    private Integer type;

    private String content;

    private String fileUrl;

    private String fileName;

    private LocalDateTime createTime;
}
