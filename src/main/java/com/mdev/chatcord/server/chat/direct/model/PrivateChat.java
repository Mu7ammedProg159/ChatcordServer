package com.mdev.chatcord.server.chat.direct.model;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PrivateChat extends Chat {

    @ManyToOne
    private User sender;

    @ManyToOne
    private User receiver;

}
