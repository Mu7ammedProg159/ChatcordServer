package com.mdev.chatcord.server.direct.model;

import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class PrivateChat {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    private User receiver;

    @OneToMany
    private Set<Message> messages;

    public PrivateChat(User receiver){
        this.receiver = receiver;
    }

}
