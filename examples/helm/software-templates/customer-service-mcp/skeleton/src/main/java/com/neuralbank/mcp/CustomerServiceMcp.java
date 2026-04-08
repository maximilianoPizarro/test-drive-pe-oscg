package com.neuralbank.mcp;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/api/mcp")
public class CustomerServiceMcp {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> info() {
        return Map.of(
            "name", "${{values.name}}",
            "version", "1.0.0",
            "description", "Neuralbank Customer Service MCP Server",
            "status", "running"
        );
    }

    @GET
    @Path("/tools")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> tools() {
        return Map.of("tools", List.of(
            Map.of("name", "getCreditScore",
                   "description", "Obtiene el puntaje crediticio de un cliente por su ID"),
            Map.of("name", "getCustomerInfo",
                   "description", "Obtiene la información completa de un cliente"),
            Map.of("name", "updateCreditStatus",
                   "description", "Actualiza el estado del crédito de un cliente")
        ));
    }

    @GET
    @Path("/tools/getCreditScore/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getCreditScore(@PathParam("customerId") String customerId) {
        return Map.of(
            "customerId", customerId,
            "creditScore", 750,
            "rating", "Excelente",
            "lastUpdated", "2026-04-07"
        );
    }

    @GET
    @Path("/tools/getCustomerInfo/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getCustomerInfo(@PathParam("customerId") String customerId) {
        return Map.of(
            "customerId", customerId,
            "name", "Juan Pérez",
            "email", "juan.perez@neuralbank.com",
            "accountType", "Premium",
            "creditLimit", 50000
        );
    }

    @POST
    @Path("/tools/updateCreditStatus/{customerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> updateCreditStatus(@PathParam("customerId") String customerId, Map<String, Object> body) {
        return Map.of(
            "customerId", customerId,
            "status", "updated",
            "newStatus", body.getOrDefault("status", "active"),
            "updatedAt", "2026-04-07T12:00:00Z"
        );
    }
}
