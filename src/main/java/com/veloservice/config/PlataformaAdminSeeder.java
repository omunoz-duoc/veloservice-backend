package com.veloservice.config;

import java.time.OffsetDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.veloservice.auth.domain.model.UsuarioPlataforma;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioPlataformaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlataformaAdminSeeder implements ApplicationRunner {

    private final UsuarioPlataformaRepository usuarioPlataformaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioPlataformaRepository.count() > 0) {
            return;
        }

        String email = System.getenv("ADMIN_PLATAFORMA_EMAIL");
        String password = System.getenv("ADMIN_PLATAFORMA_PASSWORD");
        String nombre = System.getenv("ADMIN_PLATAFORMA_NOMBRE");
        String apellido = System.getenv("ADMIN_PLATAFORMA_APELLIDO");

        if (email == null || password == null) {
            log.warn("No hay usuarios_plataforma. Define ADMIN_PLATAFORMA_EMAIL y ADMIN_PLATAFORMA_PASSWORD para crear el primero.");
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        UsuarioPlataforma admin = UsuarioPlataforma.builder()
                .nombre(nombre != null ? nombre : "Admin")
                .apellido(apellido != null ? apellido : "Plataforma")
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        usuarioPlataformaRepository.save(admin);
        log.info("Usuario plataforma creado: {}", email);
    }
}
