    package com.FinalProject.TodoApp.entity;

    import jakarta.persistence.*;
    import lombok.*;
    import lombok.experimental.FieldDefaults;

    @Entity(name = "user")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public class User {
        private static final long serialVersionUID = 1L;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)

        private Integer id;

        @Column(name = "user_name", unique = true)
        String username;

        @Column(name = "is_active")
        boolean isActive;

        @Column(name = "password")
        String password;

        @Column(name = "email")
        String email;

        @Column(name = "gender")
        String gender;

        @Column(name = "avatar")
        String avatar; // <- thuộc tính avatar mới


    }
