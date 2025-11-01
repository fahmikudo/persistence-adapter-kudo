package id.fahmikudo.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * User - Entity representing users table
 */
@Setter
@Getter
public class User {

    // Getters and Setters
    private Long id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Boolean active;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Constructors
    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", active=" + active +
                ", role='" + role + '\'' +
                '}';
    }
}

