package com.neuralbank.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/api")
public class CreditResource {

    @GET
    @Path("/customers")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> listCustomers() {
        return List.of(
            Map.of("id", "C001", "name", "Juan Pérez", "email", "juan@neuralbank.com", "creditScore", 750),
            Map.of("id", "C002", "name", "María García", "email", "maria@neuralbank.com", "creditScore", 820),
            Map.of("id", "C003", "name", "Carlos López", "email", "carlos@neuralbank.com", "creditScore", 680)
        );
    }

    @GET
    @Path("/credits")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> listCredits() {
        return List.of(
            Map.of("id", "CR001", "customerId", "C001", "amount", 25000, "status", "active", "type", "personal"),
            Map.of("id", "CR002", "customerId", "C002", "amount", 150000, "status", "active", "type", "mortgage"),
            Map.of("id", "CR003", "customerId", "C003", "amount", 8000, "status", "pending", "type", "personal")
        );
    }

    @POST
    @Path("/credits/{id}/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> updateCredit(@PathParam("id") String id, Map<String, Object> body) {
        return Map.of(
            "creditId", id,
            "previousStatus", "pending",
            "newStatus", body.getOrDefault("status", "approved"),
            "updatedBy", body.getOrDefault("updatedBy", "system"),
            "updatedAt", "2026-04-07T12:00:00Z"
        );
    }
}
