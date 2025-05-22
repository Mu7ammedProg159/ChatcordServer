package com.mdev.chatcord.server.chat;

import com.mdev.chatcord.server.communication.model.ChatMember;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
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

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id")
    )
    private Set<Message> pinnedMessages = new HashSet<>();

    private LocalDateTime createdAt;

    public Chat(ChatType type, List<ChatMember> members, List<Message> messages, Set<Message> pinnedMessages, LocalDateTime createdAt) {
        this.type = type;
        this.members = members;
        this.messages = messages;
        this.pinnedMessages = pinnedMessages;
        this.createdAt = createdAt;
    }
}
