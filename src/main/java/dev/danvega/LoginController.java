package dev.danvega;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final OAuth2ClientConditionService oauthService;

    public LoginController(OAuth2ClientConditionService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model, String error, String logout) {

        if (error != null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Invalid username or password");
        }

        model.addAttribute("googleEnabled", oauthService.isGoogleEnabled());
        model.addAttribute("githubEnabled", oauthService.isGithubEnabled());

        return "pages/login";
    }

    @GetMapping("/")
    public String home() {
        return "pages/home";
    }
}
