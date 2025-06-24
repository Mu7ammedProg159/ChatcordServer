package com.mdev.chatcord.server.websocket.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class MessagesDTO {
    private String from;
    private String to;
    private String content;

    // Constructors, getters, setters
    public MessagesDTO() {}
    public MessagesDTO(String from, String to, String content) {
        this.from = from;
        this.to = to;
        this.content = content;
    }

}