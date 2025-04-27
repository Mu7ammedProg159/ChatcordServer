package com.mdev.chatcord.server.controller;

import com.mdev.chatcord.server.dto.UserDTO;
import com.mdev.chatcord.server.model.User;
import com.mdev.chatcord.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /* @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id){
        String[] compoundUserId = id.split("#");
        String username = compoundUserId[0];
        String tag = compoundUserId[1];

        User user = userRepository.findByUsernameAndTag(username, id);
        UserDTO userDTO = new UserDTO(user.getUsername(), user.getTag());
        return
    } */

}
