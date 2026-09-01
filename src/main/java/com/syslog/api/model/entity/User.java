package com.syslog.api.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;

    private String email;

    private String username;

    private String password;

    private Timestamp updatedAt;
}
