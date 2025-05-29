package com.mdev.chatcord.server.chat.core.model;

import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.communication.model.ChatMember;
import com.mdev.chatcord.server.message.model.Message;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Builder
@Getter
@Setter
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ChatType type; // Private or Group (Maybe a guild or server ?)

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<ChatMember> members; // Who are the members of this chat ?

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>(); // All messages in chat

    @OneToOne(fetch = FetchType.LAZY)
    private Message lastMessageSent;

    @OneToOne(fetch = FetchType.LAZY)
    private ChatMember lastMessageSender;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id")
    )
    private Set<Message> pinnedMessages = new HashSet<>();

    private LocalDateTime createdAt;

}
