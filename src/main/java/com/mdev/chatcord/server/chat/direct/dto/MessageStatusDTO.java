package com.mdev.chatcord.server.chat.direct.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@Getter
@Setter
public class MessageStatusDTO {

    private String messageUuid;
    private String messageStatusName;
    private String senderUuid;
    private String receiverUuid;

}
