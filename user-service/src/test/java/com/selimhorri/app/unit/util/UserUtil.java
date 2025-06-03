package com.selimhorri.app.unit.util;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.CredentialDto;

import java.util.Arrays;
import java.util.List;

public class UserUtil {

    public static UserDto getSampleUserDto() {
        CredentialDto credentialDto = CredentialDto.builder()
            .credentialId(1)
            .username("johndoe")
            .password("password123")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        return UserDto.builder()
            .userId(1)
            .firstName("John")
            .lastName("Doe")
            .imageUrl("http://example.com/john.jpg")
            .email("john.doe@example.com")
            .phone("+1234567890")
            .credentialDto(credentialDto)
            .build();
    }

    public static User getSampleUser() {
        Credential credential = Credential.builder()
            .credentialId(1)
            .username("johndoe")
            .password("password123")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        User user = User.builder()
            .userId(1)
            .firstName("John")
            .lastName("Doe")
            .imageUrl("http://example.com/john.jpg")
            .email("john.doe@example.com")
            .phone("+1234567890")
            .credential(credential)
            .build();
        credential.setUser(user);
        return user;
    }

    public static User getSampleUser2() {
        Credential credential = Credential.builder()
            .credentialId(2)
            .username("janesmith")
            .password("password456")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();
        User user = User.builder()
            .userId(2)
            .firstName("Jane")
            .lastName("Smith")
            .imageUrl("http://example.com/jane.jpg")
            .email("jane.smith@example.com")
            .phone("+0987654321")
            .credential(credential)
            .build();
        credential.setUser(user);
        return user;
    }

    public static List<User> getSampleUsers() {
        return Arrays.asList(getSampleUser(), getSampleUser2());
    }
}
