package com.mdev.chatcord.server.friend.repository;

import com.mdev.chatcord.server.friend.model.Friendship;
import com.mdev.chatcord.server.friend.service.EFriendStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findByOwnerId(Long id);
    Optional<Friendship> findByOwnerIdAndFriendId(Long owner_id, Long friend_id);
    @Query("SELECT f FROM Friendship f WHERE f.owner.id = :ownerId AND f.friend.username =:friend_username AND f.friend.tag = :friend_tag")
    Optional<Friendship> findByOwnerIdFriendUsernameAndTag(@Param("ownerId") Long ownerId,
                                                           @Param("friend_username") String friend_username,
                                                           @Param("friend_tag") String friend_tag);

    @Query("SELECT f FROM Friendship f WHERE f.friend.username =:friend_username AND f.friend.tag = :friend_tag")
    Optional<Friendship> findByFriendUsernameAndTag(@Param("friend_username") String friend_username,
                                                    @Param("friend_tag") String friend_tag);

    @Query("SELECT f FROM Friendship f JOIN FETCH f.friend where f.owner.id = :profileId OR f.friend.id = :profileId")
    Page<Friendship> findAllByProfileId(@Param("profileId") Long profileId, Pageable pageable);

    @Query("SELECT f FROM Friendship f WHERE f.friendStatus = :friendStatus AND f.friend.id = :friendId")
    Page<Friendship> findAllByFriendStatusAndFriendId(@Param("friendStatus") EFriendStatus friendStatus,
                                                      @Param("friendId") Long friendId, Pageable pageable);

//    Set<Long> findAllUuidByOwnerId();

    boolean existsByOwnerIdAndFriendId(Long owner_id, Long friend_id);

    @Modifying
    @Transactional
    //@Query("DELETE FROM Friendship f WHERE f.owner.id = :ownerId AND f.friend.id = :friendId")
    @Query("DELETE FROM Friendship f WHERE f.friendStatus = 'DECLINED' AND f.declinedAt < :cutoff")
    int deleteFriendship(@Param("cutoff") LocalDateTime cutoff);

}
