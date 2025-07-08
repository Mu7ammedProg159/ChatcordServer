package com.mdev.chatcord.server.chat.core.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mdev.chatcord.server.chat.core.enums.ChatType;
import com.mdev.chatcord.server.communication.model.ChatMember;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.websocket.configuration.UUIDPrinciple;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@RequiredArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public abstract class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    private ChatType type; // Private or Group (Maybe a guild or server ?)

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<ChatMember> members; // Who are the members of this chat ?

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>(); // All messages in chat

    @OneToOne(fetch = FetchType.LAZY)
    private Message lastMessageSent;

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id")
    )
    private Set<Message> pinnedMessages = new HashSet<>();

    private LocalDateTime createdAt;

    @PrePersist
    private void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

}
