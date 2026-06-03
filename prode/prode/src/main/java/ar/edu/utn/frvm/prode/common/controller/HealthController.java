package ar.edu.utn.frvm.prode.common.controller;

import ar.edu.utn.frvm.prode.common.dto.HealthResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller temporal para comprobar que la API levanta y responde.
 * No representa una funcionalidad de negocio del Prode.
 */
@RestController // Le indica a Spring que esta clase responde peticiones HTTP y devuelve datos en formato JSON.
@RequestMapping("/api") // Define el prefijo comun de las rutas de este controller.
public class HealthController {

	@GetMapping("/health") // Atiende peticiones HTTP GET en la ruta /api/health.
	public HealthResponse health() { // Metodo ejecutado cuando llega una peticion GET /api/health.
		return new HealthResponse(
				"OK",
				"Prode API funcionando correctamente"
		); // Spring convierte este DTO automaticamente a JSON.
	}
}
