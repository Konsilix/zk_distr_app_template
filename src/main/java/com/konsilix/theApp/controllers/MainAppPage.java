package com.konsilix.theApp.controllers;

import com.konsilix.theApp.models.ClusterInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
//import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

import java.util.Collections;
import java.util.Map;

import static com.konsilix.zk.ZkDemoUtil.getMyHostname;

@Slf4j // this gives access to a logger called "log"
@Controller
public class MainAppPage {

//    @Autowired
//    private SpringResourceTemplateResolver templateResolver;

    @GetMapping("/")
    public String index(Model model) {
        // ... in a method where you can access the templateResolver ...
//        String prefix = templateResolver.getPrefix();
//        String suffix = templateResolver.getSuffix();
//        log.info("Thymeleaf template prefix: {} ", prefix);
//        log.info("Thymeleaf template suffix: {} ", suffix);

        model.addAttribute("hostname", getMyHostname());
        model.addAttribute("message", "Hello from T");
        return "index.html"; // This will return src/main/resources/templates/index.html
    }

//    @GetMapping("/")
//    public String home() {
//        return "index.html"; // This should match index.html in src/main/resources/templates
//    }

    @GetMapping("/welcome")
    @ResponseBody
    public String greet(){
        return "Hello Thymeleaf and world of OAuth2 and Spring Security!";
    }

    //    @RequestMapping("/user")
    @GetMapping("/user")
    @ResponseBody
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            log.info("Authenticated user: " + principal.getAttribute("name"));
            return Collections.singletonMap("name", principal.getAttribute("name"));
        } else {
            log.error("No authenticated user found.");
            return Collections.singletonMap("error", "No authenticated user");
        }
    }

    @GetMapping("/clusterInfo")
    public ResponseEntity<ClusterInfo> getClusterinfo() {
        return ResponseEntity.ok(ClusterInfo.getClusterInfo());
    }
}
