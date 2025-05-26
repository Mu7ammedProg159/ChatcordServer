package com.mdev.chatcord.server.communication.model;

import com.mdev.chatcord.server.chat.Chat;
import com.mdev.chatcord.server.message.model.Message;
import com.mdev.chatcord.server.user.model.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMember {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Profile profile; // Who the member of a chat ?

    @ManyToOne
    private Chat chat; // Where the chat is ?

    @ManyToMany
    @JoinTable(
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "message_id")
    )
    private Set<Message> starredMessages;

    private int pings = 0; // How many messages unseen ?
    private boolean muted; // Keep pings shushed
    private LocalDateTime lastSeenAt; // When last time you entered the chat ?

    // for guild chats
    private String nickname; // How you want others see your name ?

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ChatRole role; // Role will be Null in 1-on-1 Private chats.

    public ChatMember(Profile user, Chat chat, Set<Message> starredMessages, int pings, boolean muted, LocalDateTime lastSeenAt, String nickname, ChatRole role) {
        this.profile = user;
        this.chat = chat;
        this.starredMessages = starredMessages;
        this.pings = pings;
        this.muted = muted;
        this.lastSeenAt = lastSeenAt;
        this.nickname = nickname;
        this.role = role;
    }
}
