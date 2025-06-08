package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.LoginData;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;

@Controller
public class LoginController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "formLogin";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session) {

        // Llamada al servicio para comprobar si el login es correcto
        UsuarioService.LoginStatus loginStatus = usuarioService.login(loginData.geteMail(), loginData.getPassword());

        if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());

            managerUserSession.logearUsuario(usuario.getId());
            session.setAttribute("usuario", usuario);
            
            if (Boolean.TRUE.equals(usuario.getEsAdministrador()))
                return "redirect:/registrados";
            else
                return "redirect:/usuarios/" + usuario.getId() + "/tareas";

        } else if (loginStatus == UsuarioService.LoginStatus.USER_NOT_FOUND) {
            model.addAttribute("error", "No existe usuario");
            return "formLogin";
        } else if (loginStatus == UsuarioService.LoginStatus.ERROR_PASSWORD) {
            model.addAttribute("error", "Contraseña incorrecta");
            return "formLogin";
        }
        return "formLogin";
    }
    
    @GetMapping("/registro")
    public String registroForm(Model model) {

        boolean existeAdmin = usuarioRepository.existsByEsAdministradorTrue();
        model.addAttribute("existeAdmin", existeAdmin);
        model.addAttribute("registroData", new RegistroData());
        return "formRegistro";
    }

   @PostMapping("/registro")
   public String registroSubmit(@Valid RegistroData registroData, BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "formRegistro";
        }

        if (usuarioService.findByEmail(registroData.getEmail()) != null) {
            model.addAttribute("registroData", registroData);
            model.addAttribute("error", "El usuario " + registroData.getEmail() + " ya existe");
            return "formRegistro";
        }

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail(registroData.getEmail());
        usuario.setPassword(registroData.getPassword());
        usuario.setFechaNacimiento(registroData.getFechaNacimiento());
        usuario.setNombre(registroData.getNombre());
        usuario.setEsAdministrador(registroData.isEsAdministrador());

        usuarioService.registrar(usuario);
        return "redirect:/login";
   }

   @GetMapping("/logout")
   public String logout(HttpSession session) {
        managerUserSession.logout();
        managerUserSession.logearUsuario(null);
        session.invalidate();
        return "redirect:/login";
   }
   
   @GetMapping("/about")
   public String about(Model model) {
       model.addAttribute("titulo", "About");
       return "about";
   }
   
   @GetMapping("/registrados")
   public String usuarioList(Model model, HttpSession session) {

       UsuarioData usuario = (UsuarioData) session.getAttribute("usuario");

       if (usuario == null) {
           model.addAttribute("error", "Debes iniciar sesión.");
           return "redirect:/login";
       }

       List<UsuarioData> usuarios = usuarioService.findAll();
       model.addAttribute("usuarios", usuarios);
       return "listaUsuarios";
   }
   
   @GetMapping("/registrados/{id}")
   public String verUsuario(@PathVariable Long id, Model model) {
       UsuarioData usuario = usuarioService.findById(id);
       if (usuario == null) {
    	    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    	}                    
       model.addAttribute("usuario", usuario);
       return "descripcionUsuario";
   }
   
   @GetMapping("/admin")
   public boolean existeAdmin(){
       try {
		return usuarioRepository.existsByEsAdministradorTrue();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return false;
   }

}
