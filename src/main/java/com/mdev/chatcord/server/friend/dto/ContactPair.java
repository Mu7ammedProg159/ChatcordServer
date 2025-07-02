package com.mdev.chatcord.server.friend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ContactPair {
    private ContactPreview requester;
    private ContactPreview receiver;
}
