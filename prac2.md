# Documentación Técnica - Práctica 2

**Nombre:** Daniel Jaramillo  
**Fecha:** 9 de junio de 2025


Este documento recopila los cambios realizados durante la implementación de la funcionalidad de bloqueo/desbloqueo de usuarios en la aplicación. Está orientado a los desarrolladores del equipo y detalla las nuevas clases, métodos, plantillas, tests y fragmentos de código relevantes en ToDoApp.

## 1. Nuevas Clases y Métodos

### 1.1 `LoginController (modificada)`

* **Ubicación:** `madstodolist.controller.LoginController`
* **Propósito de la modificación:** Manejar las solicitudes de bloqueo y habilitación de usuarios.
* **Método principal:**

  ```java
  @PostMapping("/usuarios/{id}/bloqueo")
  public String toggleBloqueo(@PathVariable Long id, HttpSession session) {
      UsuarioData admin = (UsuarioData) session.getAttribute("usuario");
      if (admin == null || !Boolean.TRUE.equals(admin.getEsAdministrador())) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autorizado.");
      }
      usuarioService.Bloqueo(id);
      return "redirect:/registrados";
  }
  ```

  * Valida que el usuario en sesión sea administrador.
  * Llama a `usuarioService.Bloqueo(id)` para alternar el estado de bloqueo.

  **Método secundario:**
    Valida que el usuario que va a ingresar no esté bloqueado

  ```java
     @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session) {
      //Lógica anterior
      else if (loginStatus == UsuarioService.LoginStatus.USER_BLOCKED) {
            model.addAttribute("error", "Tu cuenta está bloqueada. Contacta al administrador.");
            return "login";
        }
  }
  ```


### 1.2 `UsuarioService#Bloqueo`

* **Ubicación:** `madstodolist.service.UsuarioService`
* **Funcionalidad:** Invierte el estado de `bloqueado` del usuario con el ID proporcionado.
* **Implementación clave:**

  ```java
  @Transactional
    public void Bloqueo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));
        usuario.setBloqueado(!Boolean.TRUE.equals(usuario.getBloqueado()));
        usuarioRepository.save(usuario);
    }
  ```

  * Se asegura de manejar valores `null` en `bloqueado`.
  * Persiste el nuevo estado en la base de datos.

## 2. Plantillas Thymeleaf Añadidas/Modificadas

### 2.1 `listaUsuarios.html`

* **Ubicación:** `src/main/resources/templates/listaUsuarios.html`
* **Cambios destacados:**

  * Nueva columna **Estado** que muestra `Activo` o `Bloqueado`.
  * Botón inline de bloqueo/habilitación:

    ```html
    <td>
      <span th:text="${usuario.bloqueado} ? 'Bloqueado' : 'Activo'"
            th:classappend="${usuario.bloqueado} ? ' text-danger' : ' text-success'"></span>
    </td>
    <td>
      <form th:action="@{'/usuarios/' + ${usuario.id} + '/bloqueo'}" method="post" class="d-inline">
        <button type="submit"
                class="btn btn-sm"
                th:classappend="${usuario.bloqueado} ? ' btn-outline-success' : ' btn-outline-danger'"
                th:text="${usuario.bloqueado} ? 'Habilitar' : 'Bloquear'"></button>
      </form>
    </td>
    ```
  * Uso de utilidades de Bootstrap (`d-inline`, `btn-outline-*`, `text-danger`/`text-success`).


## 4. Explicación de Código Fuente Relevante

### 4.1 Manejo de Estado en DTO y modelo

* En `UsuarioData` y `Usuario` se añadió el atributo `bloqueado` de tipo `Boolean` con sus getters/setters.
* Se inicializa a `false` por defecto en la base de datos para usuarios nuevos.

### 4.2 Filtrado de Permisos en el Controlador

* El controlador comprueba en cada acción que el usuario en sesión exista y tenga `esAdministrador == true`. De lo contrario lanza `ResponseStatusException(HttpStatus.UNAUTHORIZED)`.

### 4.3 Integración con la Vista

* Thymeleaf renderiza el estado y adapta estilos y texto del botón en función del valor de `usuario.bloqueado`.
* El formulario es **inline** para mantener la consistencia visual.
