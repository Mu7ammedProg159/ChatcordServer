package com.mdev.chatcord.server.friend.repository;

import com.mdev.chatcord.server.friend.model.Friend;
import com.mdev.chatcord.server.friend.service.EFriendStatus;
import jakarta.websocket.server.PathParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByOwnerId(Long id);
    Optional<Friend> findByOwnerIdAndFriendId(Long owner_id, Long friend_id);
    @Query("SELECT f FROM Friend f WHERE f.owner.id = :ownerId AND f.friend.username =:friend_username AND f.friend.tag = :friend_tag")
    Optional<Friend> findByFriendUsernameAndTag(@Param("ownerId") Long ownerId,
                                                @Param("friend_username") String friend_username,
                                                @Param("friend_tag") String friend_tag);

    @Query("SELECT f FROM Friend f where f.owner.id = :ownerId")
    Page<Friend> findAllByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("SELECT f FROM Friend f WHERE f.friendStatus = :friendStatus AND f.friend.id = :friendId")
    Page<Friend> findAllByFriendStatusAndFriendId(@Param("friendStatus") EFriendStatus friendStatus,
                                                  @Param("friendId") Long friendId, Pageable pageable);

    boolean existsByOwnerIdAndFriendId(Long owner_id, Long friend_id);

}
