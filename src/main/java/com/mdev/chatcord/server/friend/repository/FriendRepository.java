package com.mdev.chatcord.server.friend.repository;

import com.mdev.chatcord.server.friend.model.Friend;
import jakarta.websocket.server.PathParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    Optional<Friend> findByOwnerId(Long owner_id);
    Optional<Friend> findByOwnerIdAndFriendId(Long owner_id, Long friend_id);

    @Query("SELECT f FROM Friend f where f.owner.id = :ownerId")
    Page<Friend> findAllByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    boolean existsByOwnerIdAndFriendId(Long owner_id, Long friend_id);
}
