package alg.coyote001.service;

import alg.coyote001.entity.Role;
import alg.coyote001.entity.User;
import alg.coyote001.event.SubscriptionCancelledEvent;
import alg.coyote001.event.SubscriptionExpiredEvent;
import alg.coyote001.event.SubscriptionUpgradedEvent;
import alg.coyote001.repository.RoleRepository;
import alg.coyote001.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionRoleIntegrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private SubscriptionRoleIntegrationService integrationService;

    private User testUser;
    private Role userRole;
    private Role freeRole;
    private Role premiumRole;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(new HashSet<>())
                .build();

        userRole = Role.builder()
                .id(1L)
                .name("USER")
                .description("Base user role")
                .build();

        freeRole = Role.builder()
                .id(2L)
                .name("SUBSCRIPTION_FREE")
                .description("Free subscription plan")
                .build();

        premiumRole = Role.builder()
                .id(3L)
                .name("SUBSCRIPTION_PREMIUM")
                .description("Premium subscription plan")
                .build();
    }

    @Test
    void testHandleSubscriptionUpgraded() {
        // Given
        SubscriptionUpgradedEvent event = new SubscriptionUpgradedEvent(this, 1L, "PREMIUM", "FREE");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("SUBSCRIPTION_PREMIUM")).thenReturn(Optional.of(premiumRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        integrationService.handleSubscriptionUpgraded(event);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getRoles().contains(userRole));
        assertTrue(savedUser.getRoles().contains(premiumRole));
    }

    @Test
    void testHandleSubscriptionUpgraded_UserNotFound() {
        // Given
        SubscriptionUpgradedEvent event = new SubscriptionUpgradedEvent(this, 1L, "PREMIUM", "FREE");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                integrationService.handleSubscriptionUpgraded(event));
    }

    @Test
    void testHandleSubscriptionCancelled() {
        // Given
        SubscriptionCancelledEvent event = new SubscriptionCancelledEvent(this, 1L, "PREMIUM");
        testUser.getRoles().add(premiumRole);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("SUBSCRIPTION_FREE")).thenReturn(Optional.of(freeRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        integrationService.handleSubscriptionCancelled(event);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getRoles().contains(userRole));
        assertTrue(savedUser.getRoles().contains(freeRole));
        assertFalse(savedUser.getRoles().contains(premiumRole));
    }

    @Test
    void testHandleSubscriptionExpired() {
        // Given
        SubscriptionExpiredEvent event = new SubscriptionExpiredEvent(this, 1L, "PREMIUM");
        testUser.getRoles().add(premiumRole);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("SUBSCRIPTION_FREE")).thenReturn(Optional.of(freeRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        integrationService.handleSubscriptionExpired(event);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getRoles().contains(userRole));
        assertTrue(savedUser.getRoles().contains(freeRole));
        assertFalse(savedUser.getRoles().contains(premiumRole));
    }

    @Test
    void testInitializeSubscriptionRoles_CreatesMissingRoles() {
        // Given
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.findByName("SUBSCRIPTION_FREE")).thenReturn(Optional.empty());
        when(roleRepository.findByName("SUBSCRIPTION_PREMIUM")).thenReturn(Optional.empty());
        
        when(roleRepository.save(any(Role.class))).thenReturn(new Role());

        // When
        integrationService.initializeSubscriptionRoles();

        // Then
        verify(roleRepository, times(3)).save(any(Role.class));
        verify(roleRepository).save(argThat(role -> role.getName().equals("USER")));
        verify(roleRepository).save(argThat(role -> role.getName().equals("SUBSCRIPTION_FREE")));
        verify(roleRepository).save(argThat(role -> role.getName().equals("SUBSCRIPTION_PREMIUM")));
    }

    @Test
    void testInitializeSubscriptionRoles_RolesAlreadyExist() {
        // Given
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("SUBSCRIPTION_FREE")).thenReturn(Optional.of(freeRole));
        when(roleRepository.findByName("SUBSCRIPTION_PREMIUM")).thenReturn(Optional.of(premiumRole));

        // When
        integrationService.initializeSubscriptionRoles();

        // Then
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void testHasSubscriptionAccess_PremiumUser() {
        // Given
        testUser.getRoles().add(premiumRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = integrationService.hasSubscriptionAccess(1L, "twitter_tracking");

        // Then
        assertTrue(result);
    }

    @Test
    void testHasSubscriptionAccess_FreeUserBasicResource() {
        // Given
        testUser.getRoles().add(freeRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = integrationService.hasSubscriptionAccess(1L, "basic_tracking");

        // Then
        assertTrue(result);
    }

    @Test
    void testHasSubscriptionAccess_FreeUserPremiumResource() {
        // Given
        testUser.getRoles().add(freeRole);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = integrationService.hasSubscriptionAccess(1L, "twitter_tracking");

        // Then
        assertFalse(result);
    }

    @Test
    void testHasSubscriptionAccess_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> 
                integrationService.hasSubscriptionAccess(1L, "twitter_tracking"));
    }

    @Test
    void testRoleCreation_Premium() {
        // Given
        when(roleRepository.findByName("SUBSCRIPTION_PREMIUM")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(premiumRole);

        // When
        integrationService.initializeSubscriptionRoles();

        // Then
        verify(roleRepository).save(argThat(role -> 
            role.getName().equals("SUBSCRIPTION_PREMIUM") && 
            role.getDescription().contains("Premium subscription") &&
            role.getPriority() == 5
        ));
    }

    @Test
    void testRoleCreation_Free() {
        // Given
        when(roleRepository.findByName("SUBSCRIPTION_FREE")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(freeRole);

        // When
        integrationService.initializeSubscriptionRoles();

        // Then
        verify(roleRepository).save(argThat(role -> 
            role.getName().equals("SUBSCRIPTION_FREE") && 
            role.getDescription().contains("Free subscription") &&
            role.getPriority() == 1
        ));
    }

    @Test
    void testUpdateUserRoles_RemovesOldSubscriptionRoles() {
        // Given
        SubscriptionUpgradedEvent event = new SubscriptionUpgradedEvent(this, 1L, "PREMIUM", "FREE");
        
        Set<Role> existingRoles = new HashSet<>();
        existingRoles.add(freeRole);
        existingRoles.add(userRole);
        testUser.setRoles(existingRoles);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("SUBSCRIPTION_PREMIUM")).thenReturn(Optional.of(premiumRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        integrationService.handleSubscriptionUpgraded(event);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getRoles().contains(userRole));
        assertTrue(savedUser.getRoles().contains(premiumRole));
        assertFalse(savedUser.getRoles().contains(freeRole)); // Old subscription role removed
    }

    @Test
    void testUpdateUserRoles_PreservesNonSubscriptionRoles() {
        // Given
        SubscriptionUpgradedEvent event = new SubscriptionUpgradedEvent(this, 1L, "PREMIUM", "FREE");
        
        Role adminRole = Role.builder()
                .id(4L)
                .name("ADMIN")
                .description("Admin role")
                .build();
        
        Set<Role> existingRoles = new HashSet<>();
        existingRoles.add(freeRole);
        existingRoles.add(userRole);
        existingRoles.add(adminRole);
        testUser.setRoles(existingRoles);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("SUBSCRIPTION_PREMIUM")).thenReturn(Optional.of(premiumRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        integrationService.handleSubscriptionUpgraded(event);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.getRoles().contains(userRole));
        assertTrue(savedUser.getRoles().contains(premiumRole));
        assertTrue(savedUser.getRoles().contains(adminRole)); // Non-subscription role preserved
        assertFalse(savedUser.getRoles().contains(freeRole)); // Old subscription role removed
    }
} 