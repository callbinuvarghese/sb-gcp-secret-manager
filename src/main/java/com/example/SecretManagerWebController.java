package com.example;

import com.google.cloud.spring.secretmanager.SecretManagerTemplate;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SecretManagerWebController {

    @Autowired
    private Environment environment;

    @Autowired
    private SecretManagerTemplate secretManagerTemplate;

    // Application secrets can be accessed using @Value and using the "sm://" syntax.
    @Value("${sm://application-secret}")
    private String appSecret;
    @Value("${sm://application-secret/1}")
    private String appSecretOld;
    @Value("${sm://source-api-password-2}")
    private String sourceApiPassword2;

    // Multiple ways of loading the application-secret are demonstrated in bootstrap.properties.
    // Try it with my-app-secret-1 or my-app-secret-2
    @Value("${my-app-secret-1}")
    private String myAppSecret;

    @Value("${my-app-secret-2}")
    private String myAppSecretOld;

    @GetMapping("/")
    public ModelAndView renderIndex(ModelMap map) {
        map.put("applicationSecret", this.appSecret);
        map.put("applicationSecretOld", this.appSecretOld);
        map.put("sourceApiPassword2", this.sourceApiPassword2);
        map.put("myApplicationSecret", this.myAppSecret);
        map.put("myApplicationSecretOld", this.myAppSecretOld);
        return new ModelAndView("index.html", map);
    }

    @GetMapping("/getSecret")
    @ResponseBody
    public String getSecret(
            @RequestParam String secretId,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String projectId,
            ModelMap map) {

        if (StringUtils.isEmpty(version)) {
            version = SecretManagerTemplate.LATEST_VERSION;
        }

        String secretPayload;
        if (StringUtils.isEmpty(projectId)) {
            secretPayload = this.secretManagerTemplate.getSecretString(
                    "sm://" + secretId + "/" + version);
        }
        else {
            secretPayload = this.secretManagerTemplate.getSecretString(
                    "sm://" + projectId + "/" + secretId + "/" + version);
        }

        return "Secret ID: " + secretId + " | Value: " + secretPayload
                + "<br/><br/><a href='/'>Go back</a>";
    }

    @PostMapping("/createSecret")
    public ModelAndView createSecret(
            @RequestParam String secretId,
            @RequestParam String secretPayload,
            @RequestParam(required = false) String projectId,
            ModelMap map) {

        if (StringUtils.isEmpty(projectId)) {
            this.secretManagerTemplate.createSecret(secretId, secretPayload);
        }
        else {
            this.secretManagerTemplate.createSecret(secretId, secretPayload.getBytes(), projectId);
        }

        map.put("applicationSecret", this.appSecret);
        map.put("myApplicationSecret", this.myAppSecret);
        map.put("message", "Secret created!");
        return new ModelAndView("index.html", map);
    }

    @PostMapping("/deleteSecret")
    public ModelAndView deleteSecret(
            @RequestParam String secretId,
            @RequestParam(required = false) String projectId,
            ModelMap map) {
        if (StringUtils.isEmpty(projectId)) {
            this.secretManagerTemplate.deleteSecret(secretId);
        }
        else {
            this.secretManagerTemplate.deleteSecret(secretId, projectId);
        }
        map.put("applicationSecret", this.appSecret);
        map.put("message", "Secret deleted!");
        return new ModelAndView("index.html", map);
    }
}