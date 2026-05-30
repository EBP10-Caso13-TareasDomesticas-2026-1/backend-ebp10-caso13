package com.tareasdomesticas.backend.service;

import com.tareasdomesticas.backend.dto.RegistroUsuarioRequest;
import com.tareasdomesticas.backend.entity.Usuario;
import com.tareasdomesticas.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ConcurrentMap<String, FailedAttempt> intentosFallidos = new ConcurrentHashMap<>();
    private static final int MAX_INTENTOS = 3;
    private static final int BLOQUEO_MINUTOS = 15;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{5}$");

    public UsuarioService(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public boolean existePorCorreo(String correo) {
        return usuarioRepository.existsByCorreo(correo);
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario registrarUsuario(RegistroUsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        usuario.setPinSeguridadHash(passwordEncoder.encode(request.getPinSeguridad()));
        usuario.setCreadoEn(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    public Usuario validarCredenciales(String correo, String contrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Credenciales inválidas");
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(contrasena, usuario.getContrasenaHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return usuario;
    }

    public void restablecerContrasena(String correo, String pinSeguridad, String nuevaContrasena, String confirmarContrasena) {
        if (correo == null || correo.isBlank() || !EMAIL_PATTERN.matcher(correo).matches()) {
            throw new IllegalArgumentException("Formato de correo inválido");
        }

        if (pinSeguridad == null || !PIN_PATTERN.matcher(pinSeguridad).matches()) {
            throw new IllegalArgumentException("PIN de seguridad inválido");
        }

        if (nuevaContrasena == null || confirmarContrasena == null || nuevaContrasena.isBlank() || confirmarContrasena.isBlank()) {
            throw new IllegalArgumentException("Campos requeridos faltantes");
        }

        if (!nuevaContrasena.equals(confirmarContrasena)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        if (nuevaContrasena.length() < 8) {
            throw new IllegalArgumentException("La nueva contraseña no cumple la política mínima");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isEmpty()) {
            // No indicar que el correo no existe: respuesta genérica al cliente
            throw new RuntimeException("Operación no permitida");
        }

        Usuario usuario = usuarioOpt.get();

        // Comprueba bloqueo por intentos
        FailedAttempt fa = intentosFallidos.get(correo);
        if (fa != null && fa.lockUntil != null) {
            LocalDateTime ahora = LocalDateTime.now(ZoneOffset.UTC);
            if (ahora.isBefore(fa.lockUntil)) {
                throw new RuntimeException("Operación no permitida");
            }
        }

        // Validar PIN comparando hash
        boolean pinMatches = passwordEncoder.matches(pinSeguridad, usuario.getPinSeguridadHash());

        if (!pinMatches) {
            // incrementar contador
            intentosFallidos.compute(correo, (k, v) -> {
                if (v == null) v = new FailedAttempt(0, null);
                v.count++;
                if (v.count >= MAX_INTENTOS) {
                    v.lockUntil = LocalDateTime.now(ZoneOffset.UTC).plus(BLOQUEO_MINUTOS, ChronoUnit.MINUTES);
                }
                return v;
            });
            throw new RuntimeException("Operación no permitida");
        }

        // PIN correcto -> actualizar contraseña y limpiar intentos
        usuario.setContrasenaHash(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
        intentosFallidos.remove(correo);
    }

    private static class FailedAttempt {
        int count;
        LocalDateTime lockUntil;

        FailedAttempt(int count, LocalDateTime lockUntil) {
            this.count = count;
            this.lockUntil = lockUntil;
        }
    }
}