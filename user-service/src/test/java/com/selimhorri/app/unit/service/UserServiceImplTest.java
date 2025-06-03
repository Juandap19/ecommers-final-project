package com.selimhorri.app.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user1;
    private User user2;
    private List<User> userList;    @BeforeEach
    void setUp() {
        // Setup test data
        Credential credential1 = Credential.builder()
            .credentialId(1)
            .username("johndoe")
            .password("password123")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();

        user1 = User.builder()
            .userId(1)
            .firstName("John")
            .lastName("Doe")
            .imageUrl("http://example.com/john.jpg")
            .email("john.doe@example.com")
            .phone("+1234567890")
            .credential(credential1)
            .build();

        credential1.setUser(user1);

        Credential credential2 = Credential.builder()
            .credentialId(2)
            .username("janesmith")
            .password("password456")
            .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
            .isEnabled(true)
            .isAccountNonExpired(true)
            .isAccountNonLocked(true)
            .isCredentialsNonExpired(true)
            .build();

        user2 = User.builder()
            .userId(2)
            .firstName("Jane")
            .lastName("Smith")
            .imageUrl("http://example.com/jane.jpg")
            .email("jane.smith@example.com")
            .phone("+0987654321")
            .credential(credential2)
            .build();

        credential2.setUser(user2);

        userList = Arrays.asList(user1, user2);
    }

    @Test
    void findAll_ShouldReturnListOfUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(userList);

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(user1.getFirstName(), result.get(0).getFirstName());
        assertEquals(user2.getFirstName(), result.get(1).getFirstName());
    }

    @Test
    void findById_WithValidId_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));

        // When
        UserDto result = userService.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(user1.getUserId(), result.getUserId());
        assertEquals(user1.getFirstName(), result.getFirstName());
        assertEquals(user1.getLastName(), result.getLastName());
    }

    @Test
    void findById_WithInvalidId_ShouldThrowException() {
        // Given
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findById(999);
        });
    }

    @Test
    void findByUsername_WithValidUsername_ShouldReturnUser() {
        // Given
        String username = "johndoe";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.of(user1));

        // When
        UserDto result = userService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(user1.getUserId(), result.getUserId());
        assertEquals(user1.getFirstName(), result.getFirstName());
    }

    @Test
    void findByUsername_WithInvalidUsername_ShouldThrowException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByCredentialUsername(username)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UserObjectNotFoundException.class, () -> {
            userService.findByUsername(username);
        });
    }
}
