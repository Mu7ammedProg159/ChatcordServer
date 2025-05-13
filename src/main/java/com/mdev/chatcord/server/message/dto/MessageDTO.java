package com.mdev.chatcord.server.message.dto;

import com.mdev.chatcord.server.message.service.EMessageStatus;
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
    private EMessageStatus messageStatus;

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
