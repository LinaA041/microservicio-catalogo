package co.analisys.biblioteca.controller;

import co.analisys.biblioteca.model.Libro;
import co.analisys.biblioteca.model.LibroId;
import co.analisys.biblioteca.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/libros")
@Tag(name = "Catálogo", description = "Operaciones sobre el catálogo de libros: consulta, búsqueda y disponibilidad")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    @Autowired
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @Operation(
            summary = "Obtener un libro por ID",
            description = "Retorna la información completa de un libro incluyendo título, ISBN, categoría, autores y disponibilidad. Requiere rol ROLE_LIBRARIAN o ROLE_USER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Libro encontrado",
                    content = @Content(schema = @Schema(implementation = Libro.class))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'USER')")
    public Libro obtenerLibro( @Parameter(description = "ID único del libro", required = true, example = "L001")
                                   @PathVariable String id) {
        return catalogoService.obtenerLibro(new LibroId(id));
    }

    @Operation(
            summary = "Verificar disponibilidad de un libro",
            description = "Retorna `true` si el libro está disponible para préstamo, `false` en caso contrario. Usado internamente por el microservicio de Circulación. Requiere rol ROLE_LIBRARIAN o ROLE_USER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad del libro",
                    content = @Content(schema = @Schema(implementation = Boolean.class, example = "true"))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    @GetMapping("/{id}/disponible")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'USER')")
    public boolean isLibroDisponible(@Parameter(description = "ID único del libro", required = true, example = "L001")
                                         @PathVariable String id) {
        Libro libro = catalogoService.obtenerLibro(new LibroId(id));
        return libro != null && libro.isDisponible();
    }

    @Operation(
            summary = "Actualizar disponibilidad de un libro",
            description = "Cambia el estado de disponibilidad de un libro. Llamado automáticamente por el microservicio de Circulación al registrar un préstamo o devolución. Requiere rol ROLE_LIBRARIAN.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado — se requiere ROLE_LIBRARIAN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Libro no encontrado", content = @Content)
    })
    @PutMapping("/{id}/disponibilidad")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public void actualizarDisponibilidad(@Parameter(description = "ID único del libro", required = true, example = "lib-123")
     @PathVariable String id,
     @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Nuevo estado de disponibilidad: `true` = disponible, `false` = no disponible",
        required = true, content = @Content(schema = @Schema(implementation = Boolean.class, example = "false")))
     @RequestBody boolean disponible) {
        catalogoService.actualizarDisponibilidad(new LibroId(id), disponible);
    }


    @Operation(
            summary = "Buscar libros por criterio",
            description = "Busca libros cuyo título coincida con el criterio indicado. Requiere rol ROLE_LIBRARIAN o ROLE_USER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de libros encontrados",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Libro.class)))),
            @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
    })
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'USER')")
    public List<Libro> buscarLibros( @Parameter(description = "Texto a buscar en el título del libro", required = true, example = "Don Quijote")
     @RequestParam String criterio) {
        return catalogoService.buscarLibros(criterio);
    }
}
