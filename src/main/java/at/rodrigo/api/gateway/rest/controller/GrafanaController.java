package at.rodrigo.api.gateway.rest.controller;

import at.rodrigo.api.gateway.entity.Api;
import at.rodrigo.api.gateway.entity.Path;
import at.rodrigo.api.gateway.grafana.entity.GrafanaDashboard;
import at.rodrigo.api.gateway.grafana.entity.Panel;
import at.rodrigo.api.gateway.rest.configuration.ThymeleafConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/grafana")
@Slf4j
public class GrafanaController {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @PostMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GrafanaDashboard> getSimpleRestRoutes(@RequestBody Api api, HttpServletRequest request) {

        Context dashboardContext = new Context();
        dashboardContext.setVariable(ThymeleafConfiguration.USER, "automation");
        dashboardContext.setVariable(ThymeleafConfiguration.DESCRIPTION, api.getName());
        dashboardContext.setVariable(ThymeleafConfiguration.MAIN_PANEL_TITLE, "Analytics for " + api.getName());
        dashboardContext.setVariable(ThymeleafConfiguration.DASHBOARD_TITLE, api.getName());

        GrafanaDashboard grafanaDashboard;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String dashboardTemplate = templateEngine.process("dashboard-template.json", dashboardContext);
            grafanaDashboard = objectMapper.readValue(dashboardTemplate, GrafanaDashboard.class);

            int gridPosition = 0;
            int incrementGridValue = 6;
            int incrementIdValue = 1;
            int id = 133;
            for(Path path : api.getPaths()) {
                Context panelContext = new Context();
                panelContext.setVariable(ThymeleafConfiguration.PANEL_GRID_POSITION, gridPosition);
                panelContext.setVariable(ThymeleafConfiguration.PANEL_ID, id);
                panelContext.setVariable(ThymeleafConfiguration.INSTANCE, "capi:8380");
                panelContext.setVariable(ThymeleafConfiguration.TARGET_EXPRESSION, path.getRouteID() + "_total");
                panelContext.setVariable(ThymeleafConfiguration.PANEL_TITLE, path.getRouteID());
                gridPosition = gridPosition + incrementGridValue;
                id = id + incrementIdValue;

                String panelTemplate = templateEngine.process("panel-template.json", panelContext);
                Panel panel = objectMapper.readValue(panelTemplate, Panel.class);
                grafanaDashboard.getDashboard().getPanels().add(panel);
            }

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth("admin", "admin");
            //headers.setBearerAuth("eyJrIjoic1RsVjNOdFNhUDQyUU9kVjJteG5HcGxuT0lMQmVPZDciLCJuIjoiY2FwaSIsImlkIjoxfQ==");
            HttpEntity<GrafanaDashboard> createRequest = new HttpEntity<>(grafanaDashboard, headers);

            ResponseEntity<GrafanaDashboard> response = restTemplate.exchange("http://grafana:3000/api/dashboards/db", HttpMethod.POST, createRequest, GrafanaDashboard.class);
            if(response.getStatusCode().is2xxSuccessful()) {
                return response;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return new ResponseEntity<>(new GrafanaDashboard(), HttpStatus.BAD_REQUEST);
    }
}
