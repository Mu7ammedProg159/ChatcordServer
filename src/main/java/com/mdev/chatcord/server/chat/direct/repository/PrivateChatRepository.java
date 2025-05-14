//package com.mdev.chatcord.server.chat.direct.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.Optional;
//
//public interface PrivateChatRepository extends JpaRepository<PrivateChat, Long> {
//
//    Optional<PrivateChat> findByReceiverId(Long id);
//
//    @Query("SELECT p.m FROM PrivateChat WHERE ")
//    String findById(@Param("privateId")Long privateId);
//}
