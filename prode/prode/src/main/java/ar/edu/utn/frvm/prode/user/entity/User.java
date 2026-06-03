package ar.edu.utn.frvm.prode.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entidad JPA que representa un usuario registrado en la plataforma.
 *
 * Una entidad es una clase Java que Hibernate puede guardar en una tabla de la base de datos.
 */
@Entity // Indica que esta clase es una entidad administrada por JPA/Hibernate.
@Table(name = "users") // Define el nombre de la tabla. Se usa "users" para evitar conflictos con palabras reservadas.
@Getter // Lombok genera automaticamente los metodos get de los campos.
@Setter // Lombok genera automaticamente los metodos set de los campos.
@NoArgsConstructor // Lombok genera un constructor vacio, obligatorio para que JPA pueda crear objetos.
public class User {

	@Id // Marca este campo como clave primaria de la tabla.
	@GeneratedValue(strategy = GenerationType.IDENTITY) // La base de datos genera el id automaticamente.
	private Long id;

	@Column(nullable = false, unique = true, length = 50) // Campo obligatorio, unico y con longitud maxima.
	private String username;

	@Column(nullable = false, unique = true, length = 120) // Campo obligatorio, unico y con longitud maxima.
	private String email;

	@Column(nullable = false) // Campo obligatorio. Guarda el hash BCrypt, nunca la contrasena real.
	private String password;

	@Enumerated(EnumType.STRING) // Guarda el enum como texto: USER o ADMIN.
	@Column(nullable = false) // Todo usuario debe tener un rol.
	private Role role;

	@Column(nullable = false, updatable = false) // Se carga al crear el usuario y no se actualiza luego.
	private Instant createdAt;

	/**
	 * Constructor usado por los services para crear usuarios nuevos.
	 *
	 * @param username nombre de usuario elegido.
	 * @param email correo electronico del usuario.
	 * @param password hash BCrypt de la contrasena.
	 * @param role rol asignado al usuario.
	 */
	public User(String username, String email, String password, Role role) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.role = role;
	}

	/**
	 * Metodo ejecutado automaticamente antes de insertar el usuario en la base de datos.
	 *
	 * No recibe parametros.
	 * Completa la fecha de creacion.
	 * No devuelve nada porque solo modifica el estado interno de la entidad.
	 */
	@PrePersist // JPA ejecuta este metodo antes de guardar por primera vez la entidad.
	public void prePersist() {
		this.createdAt = Instant.now();
	}
}
