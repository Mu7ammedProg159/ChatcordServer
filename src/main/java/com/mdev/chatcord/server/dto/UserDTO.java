package com.mdev.chatcord.server.dto;

import com.mdev.chatcord.server.model.EStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserDTO {

    private String username;
    private String tag;
    private String userSocket;
    private String lastMessage;
    private String profileImageURL = "/images/default_pfp.png";
    private EStatus eStatus = EStatus.Offline;

    public UserDTO(String username, String tag){
        super();
        this.username = username;
        this.tag = tag;
    }

    public UserDTO(String tag, String username, EStatus eStatus) {
        this.tag = tag;
        this.username = username;
        this.eStatus = eStatus;
    }
}
