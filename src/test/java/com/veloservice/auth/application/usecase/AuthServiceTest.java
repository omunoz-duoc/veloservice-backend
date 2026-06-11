package com.veloservice.auth.application.usecase;

import com.veloservice.administracion.domain.model.UsuarioSucursal;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioSucursalRepository;
import com.veloservice.auth.application.dto.AuthLoginCommand;
import com.veloservice.auth.application.dto.AuthLoginResult;
import com.veloservice.auth.application.exception.AuthErrorCode;
import com.veloservice.auth.application.exception.AuthException;
import com.veloservice.auth.application.port.SucursalPort;
import com.veloservice.auth.application.security.LoginAttemptService;
import com.veloservice.auth.domain.model.Rol;
import com.veloservice.auth.domain.model.Usuario;
import com.veloservice.auth.infraestructure.email.ResendEmailService;
import com.veloservice.auth.infraestructure.persistence.repository.PasswordResetTokenRepository;
import com.veloservice.auth.infraestructure.persistence.repository.RolRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioPlataformaRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.auth.infraestructure.ratelimit.PasswordResetRateLimiter;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.config.tenant.UsuarioContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioPlataformaRepository usuarioPlataformaRepository;
    @Mock private UsuarioSucursalRepository usuarioSucursalRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordResetRateLimiter passwordResetRateLimiter;
    @Mock private RolRepository rolRepository;
    @Mock private SucursalPort sucursalPort;
    @Mock private JwtTokenProvider jwtProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private ResendEmailService resendEmailService;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void clearContext() {
        UsuarioContext.clear();
    }

    @Test
    void adminTallerWithoutSucursalAssignmentLogsInWithTallerOnlyToken() {
        UUID userId = UUID.randomUUID();
        UUID tallerId = UUID.randomUUID();
        Usuario usuario = usuario(userId, tallerId, rol("admin_taller", "taller"));
        givenSuccessfulCredentialCheck(usuario);
        given(usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(userId)).willReturn(Optional.empty());
        given(jwtProvider.generateToken(userId, usuario.getEmail(), "admin_taller", null, tallerId))
                .willReturn("jwt");

        AuthLoginResult result = authService.login(new AuthLoginCommand(usuario.getEmail(), "Password1!"));

        assertThat(result.getToken()).isEqualTo("jwt");
        assertThat(result.getRol()).isEqualTo("admin_taller");
        assertThat(result.getAmbito()).isEqualTo("taller");
        assertThat(result.getTallerId()).isEqualTo(tallerId);
        assertThat(result.getSucursalId()).isNull();
        verify(usuarioSucursalRepository).findByUsuarioIdAndEsPrincipalTrue(userId);
        verify(jwtProvider).generateToken(userId, usuario.getEmail(), "admin_taller", null, tallerId);
    }

    @Test
    void adminTallerWithPrincipalSucursalIncludesSucursalInLoginAndToken() {
        UUID userId = UUID.randomUUID();
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        Usuario usuario = usuario(userId, tallerId, rol("admin_taller", "taller"));
        givenSuccessfulCredentialCheck(usuario);
        given(usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(userId))
                .willReturn(Optional.of(UsuarioSucursal.builder()
                        .usuarioId(userId)
                        .sucursalId(sucursalId)
                        .esPrincipal(true)
                        .build()));
        given(sucursalPort.findTallerIdBySucursalId(sucursalId)).willReturn(Optional.of(tallerId));
        given(jwtProvider.generateToken(userId, usuario.getEmail(), "admin_taller", sucursalId, tallerId))
                .willReturn("jwt");

        AuthLoginResult result = authService.login(new AuthLoginCommand(usuario.getEmail(), "Password1!"));

        assertThat(result.getToken()).isEqualTo("jwt");
        assertThat(result.getRol()).isEqualTo("admin_taller");
        assertThat(result.getAmbito()).isEqualTo("taller");
        assertThat(result.getTallerId()).isEqualTo(tallerId);
        assertThat(result.getSucursalId()).isEqualTo(sucursalId);
        verify(jwtProvider).generateToken(userId, usuario.getEmail(), "admin_taller", sucursalId, tallerId);
    }

    @Test
    void sucursalScopedUserUsesPrincipalSucursalAndIncludesBothScopes() {
        UUID userId = UUID.randomUUID();
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        Usuario usuario = usuario(userId, tallerId, rol("mecanico", "sucursal"));
        givenSuccessfulCredentialCheck(usuario);
        given(usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(userId))
                .willReturn(Optional.of(UsuarioSucursal.builder()
                        .usuarioId(userId)
                        .sucursalId(sucursalId)
                        .esPrincipal(true)
                        .build()));
        given(sucursalPort.findTallerIdBySucursalId(sucursalId)).willReturn(Optional.of(tallerId));
        given(jwtProvider.generateToken(userId, usuario.getEmail(), "mecanico", sucursalId, tallerId))
                .willReturn("jwt");

        AuthLoginResult result = authService.login(new AuthLoginCommand(usuario.getEmail(), "Password1!"));

        assertThat(result.getRol()).isEqualTo("mecanico");
        assertThat(result.getAmbito()).isEqualTo("sucursal");
        assertThat(result.getTallerId()).isEqualTo(tallerId);
        assertThat(result.getSucursalId()).isEqualTo(sucursalId);
        verify(jwtProvider).generateToken(userId, usuario.getEmail(), "mecanico", sucursalId, tallerId);
    }

    @Test
    void sucursalScopedUserWithoutPrincipalAssignmentReturnsControlledAuthError() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = usuario(userId, UUID.randomUUID(), rol("recepcionista", "sucursal"));
        givenSuccessfulCredentialCheck(usuario);
        given(usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new AuthLoginCommand(usuario.getEmail(), "Password1!")))
                .isInstanceOfSatisfying(AuthException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(AuthErrorCode.USUARIO_SIN_SUCURSAL_PRINCIPAL));
        verify(jwtProvider, never()).generateToken(any(), any(), any(), any(), any());
    }

    @Test
    void roleScopeComparisonHandlesLowercaseSchemaV3Names() {
        UUID userId = UUID.randomUUID();
        UUID tallerId = UUID.randomUUID();
        Usuario usuario = usuario(userId, tallerId, rol("admin_taller", "taller"));
        givenSuccessfulCredentialCheck(usuario);
        given(usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(userId)).willReturn(Optional.empty());
        given(jwtProvider.generateToken(userId, usuario.getEmail(), "admin_taller", null, tallerId))
                .willReturn("jwt");

        AuthLoginResult result = authService.login(new AuthLoginCommand(usuario.getEmail(), "Password1!"));

        assertThat(result.getRol()).isEqualTo("admin_taller");
        assertThat(result.getAmbito()).isEqualTo("taller");
    }

    @Test
    void changeCurrentUserPasswordUpdatesPasswordHashWhenActualPasswordMatches() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = usuario(userId, UUID.randomUUID(), rol("admin_taller", "taller"));
        UsuarioContext.setCurrentUser(userId);
        given(usuarioRepository.findById(userId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("Password1!", "hash")).willReturn(true);
        given(passwordEncoder.encode("Newpass1!")).willReturn("new-hash");

        authService.changeCurrentUserPassword("Password1!", "Newpass1!");

        assertThat(usuario.getPasswordHash()).isEqualTo("new-hash");
        assertThat(usuario.getUpdatedAt()).isNotNull();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void changeCurrentUserPasswordRejectsIncorrectActualPassword() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = usuario(userId, UUID.randomUUID(), rol("admin_taller", "taller"));
        UsuarioContext.setCurrentUser(userId);
        given(usuarioRepository.findById(userId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("wrong", "hash")).willReturn(false);

        assertThatThrownBy(() -> authService.changeCurrentUserPassword("wrong", "Newpass1!"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Contrasena actual incorrecta");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void changeCurrentUserPasswordRejectsInvalidNewPassword() {
        UUID userId = UUID.randomUUID();
        Usuario usuario = usuario(userId, UUID.randomUUID(), rol("admin_taller", "taller"));
        UsuarioContext.setCurrentUser(userId);
        given(usuarioRepository.findById(userId)).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("Password1!", "hash")).willReturn(true);

        assertThatThrownBy(() -> authService.changeCurrentUserPassword("Password1!", "sinreglas"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("INVALID_PASSWORD");
        verify(usuarioRepository, never()).save(any());
    }

    private void givenSuccessfulCredentialCheck(Usuario usuario) {
        given(loginAttemptService.isBlocked(usuario.getEmail())).willReturn(false);
        given(usuarioRepository.findByEmail(usuario.getEmail())).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("Password1!", usuario.getPasswordHash())).willReturn(true);
        given(usuarioRepository.save(usuario)).willReturn(usuario);
    }

    private Usuario usuario(UUID id, UUID tallerId, Rol rol) {
        return Usuario.builder()
                .id(id)
                .tallerId(tallerId)
                .rolId(rol.getId())
                .rol(rol)
                .nombre("Ana")
                .apellido("Perez")
                .email("ana@veloservice.cl")
                .passwordHash("hash")
                .activo(true)
                .build();
    }

    private Rol rol(String nombre, String ambito) {
        return Rol.builder()
                .id(UUID.randomUUID())
                .nombre(nombre)
                .ambito(ambito)
                .activo(true)
                .build();
    }
}
