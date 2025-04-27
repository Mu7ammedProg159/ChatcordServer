package com.mdev.chatcord.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageDTO implements Serializable {

    private String username;
    private String message;
    private String profileImageURL;
    private long timestamp;
    private com.mdev.chatcord.server.dto.EMessageStatus messageStatus;

    @Override
    public String toString() {
        return "MessageDTO{" +
                "username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", profileImageURL='" + profileImageURL + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", MessageStatus='" + messageStatus + '\'' +
                '}';
    }
}
