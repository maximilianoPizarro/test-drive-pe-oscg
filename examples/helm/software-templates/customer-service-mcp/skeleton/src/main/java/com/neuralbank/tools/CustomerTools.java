package com.neuralbank.tools;

import com.neuralbank.client.CustomerClient;
import com.neuralbank.dto.request.CreateCustomerRequest;
import com.neuralbank.dto.request.UpdateCustomerRequest;
import com.neuralbank.dto.response.CustomerResponse;
import com.neuralbank.dto.response.CreditScoreResponse;
import com.neuralbank.dto.response.PageResponse;
import com.neuralbank.enums.CustomerType;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CustomerTools {

    @RestClient
    CustomerClient customerClient;

    @Tool(description = "Create a new customer in NeuralBank system. Returns the complete customer information including generated ID.")
    public String createCustomer(
            @ToolArg(description = "Customer identification number (RUT, DNI, etc). Example: 12345678-9") String identificacion,
            @ToolArg(description = "Type of identification: RUT, DNI, PASSPORT, SSN, RFC, CURP, CPF, CUIT") String tipoIdentificacion,
            @ToolArg(description = "Customer first name") String nombre,
            @ToolArg(description = "Customer last name") String apellido,
            @ToolArg(description = "Customer email address") String email,
            @ToolArg(description = "Customer phone number with country code. Example: +56912345678") String telefono,
            @ToolArg(description = "City where customer resides") String ciudad,
            @ToolArg(description = "Country ID (1 for Chile, 2 for Argentina, etc)") String paisId,
            @ToolArg(description = "Customer type: PERSONAL, EMPRESARIAL, or CORPORATIVO", required = false) String tipoCliente,
            @ToolArg(description = "Initial credit score (0-1000)", required = false) String scoreCrediticio
    ) {
        try {
            CreateCustomerRequest request = new CreateCustomerRequest();
            request.identificacion = identificacion;
            request.tipoIdentificacion = tipoIdentificacion;
            request.nombre = nombre;
            request.apellido = apellido;
            request.email = email;
            request.telefono = telefono;
            request.ciudad = ciudad;
            request.paisId = parseLong(paisId, "paisId");

            if (tipoCliente != null) {
                request.tipoCliente = CustomerType.valueOf(tipoCliente.toUpperCase());
            }
            if (scoreCrediticio != null) {
                request.scoreCrediticio = BigDecimal.valueOf(parseDouble(scoreCrediticio, "scoreCrediticio"));
            }

            CustomerResponse response = customerClient.createCustomer(request);
            return formatCustomerResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get complete customer information by their unique ID.")
    public String getCustomer(
            @ToolArg(description = "Customer unique ID in the system") String customerId
    ) {
        try {
            CustomerResponse response = customerClient.getCustomerById(parseLong(customerId, "customerId"));
            return formatCustomerResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Find a customer by their identification number (RUT, DNI, passport, etc).")
    public String getCustomerByIdentification(
            @ToolArg(description = "Customer identification number. Example: 12345678-9") String identificacion
    ) {
        try {
            CustomerResponse response = customerClient.getCustomerByIdentificacion(identificacion);
            return formatCustomerResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Search for customers using various filters. Returns a paginated list.")
    public String searchCustomers(
            @ToolArg(description = "Page number (0-based)", required = false) String page,
            @ToolArg(description = "Number of results per page (default: 20)", required = false) String size,
            @ToolArg(description = "Search term for name, email, or identification", required = false) String search,
            @ToolArg(description = "Filter by type: PERSONAL, EMPRESARIAL, CORPORATIVO", required = false) String tipoCliente,
            @ToolArg(description = "Filter by city", required = false) String ciudad
    ) {
        try {
            CustomerType customerType = tipoCliente != null ? CustomerType.valueOf(tipoCliente.toUpperCase()) : null;
            PageResponse<CustomerResponse> response = customerClient.searchCustomers(
                    page != null ? parseInt(page, "page") : 0,
                    size != null ? parseInt(size, "size") : 20,
                    search,
                    customerType,
                    ciudad
            );
            return formatPageResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Update customer information. Only provided fields will be updated.")
    public String updateCustomer(
            @ToolArg(description = "Customer ID to update") String customerId,
            @ToolArg(description = "New first name", required = false) String nombre,
            @ToolArg(description = "New last name", required = false) String apellido,
            @ToolArg(description = "New email address", required = false) String email,
            @ToolArg(description = "New phone number", required = false) String telefono,
            @ToolArg(description = "New city", required = false) String ciudad
    ) {
        try {
            UpdateCustomerRequest request = new UpdateCustomerRequest();
            request.nombre = nombre;
            request.apellido = apellido;
            request.email = email;
            request.telefono = telefono;
            request.ciudad = ciudad;

            CustomerResponse response = customerClient.updateCustomer(parseLong(customerId, "customerId"), request);
            return formatCustomerResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Get the current credit score and risk evaluation for a customer.")
    public String getCreditScore(
            @ToolArg(description = "Customer ID to get credit score for") String customerId
    ) {
        try {
            CreditScoreResponse response = customerClient.getCreditScore(parseLong(customerId, "customerId"));
            return formatCreditScoreResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Recalculate the credit score for a customer based on current financial data.")
    public String calculateCreditScore(
            @ToolArg(description = "Customer ID to recalculate credit score for") String customerId
    ) {
        try {
            CreditScoreResponse response = customerClient.calculateCreditScore(parseLong(customerId, "customerId"));
            return formatCreditScoreResponse(response);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Update the risk level for a customer with justification.")
    public String updateRiskLevel(
            @ToolArg(description = "Customer ID") String customerId,
            @ToolArg(description = "New risk level: Bajo, Medio, Alto, Muy Alto") String nivelRiesgo,
            @ToolArg(description = "Justification for the risk level change") String justificacion
    ) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("nivelRiesgo", nivelRiesgo);
            body.put("justificacion", justificacion);
            customerClient.updateRiskLevel(parseLong(customerId, "customerId"), body);
            return "Risk level updated successfully to: " + nivelRiesgo;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Activate a customer account, allowing them to perform transactions.")
    public String activateCustomer(
            @ToolArg(description = "Customer ID to activate") String customerId
    ) {
        try {
            customerClient.activateCustomer(parseLong(customerId, "customerId"));
            return "Customer " + customerId + " has been activated successfully.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Tool(description = "Deactivate a customer account, preventing new transactions.")
    public String deactivateCustomer(
            @ToolArg(description = "Customer ID to deactivate") String customerId,
            @ToolArg(description = "Reason for deactivation") String motivo
    ) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("motivo", motivo);
            customerClient.deactivateCustomer(parseLong(customerId, "customerId"), body);
            return "Customer " + customerId + " has been deactivated. Reason: " + motivo;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private Long parseLong(String value, String paramName) {
        if (value == null) return null;
        try { return Long.parseLong(value); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + paramName + ": must be a valid number. Value: " + value);
        }
    }

    private Integer parseInt(String value, String paramName) {
        if (value == null) return null;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + paramName + ": must be a valid number. Value: " + value);
        }
    }

    private Double parseDouble(String value, String paramName) {
        if (value == null) return null;
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + paramName + ": must be a valid number. Value: " + value);
        }
    }

    private String formatCustomerResponse(CustomerResponse c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer Information:\n");
        sb.append("===================\n");
        sb.append("ID: ").append(c.id).append("\n");
        sb.append("Name: ").append(c.nombre).append(" ").append(c.apellido).append("\n");
        sb.append("Identification: ").append(c.identificacion).append(" (").append(c.tipoIdentificacion).append(")\n");
        sb.append("Email: ").append(c.email).append("\n");
        sb.append("Phone: ").append(c.telefono).append("\n");
        if (c.ciudad != null) sb.append("City: ").append(c.ciudad).append("\n");
        sb.append("Customer Type: ").append(c.tipoCliente).append("\n");
        sb.append("Active: ").append(c.activo != null && c.activo ? "Yes" : "No").append("\n");
        if (c.scoreCrediticio != null) sb.append("Credit Score: ").append(c.scoreCrediticio).append("\n");
        if (c.nivelRiesgo != null) sb.append("Risk Level: ").append(c.nivelRiesgo).append("\n");
        return sb.toString();
    }

    private String formatCreditScoreResponse(CreditScoreResponse s) {
        StringBuilder sb = new StringBuilder();
        sb.append("Credit Score Information:\n");
        sb.append("=======================\n");
        sb.append("Customer ID: ").append(s.customerId).append("\n");
        sb.append("Score: ").append(s.score).append("\n");
        sb.append("Risk Level: ").append(s.nivelRiesgo).append("\n");
        sb.append("Evaluation: ").append(s.evaluacion).append("\n");
        return sb.toString();
    }

    private String formatPageResponse(PageResponse<CustomerResponse> page) {
        StringBuilder sb = new StringBuilder();
        sb.append("Search Results:\n");
        sb.append("==============\n");
        sb.append("Total: ").append(page.totalElements).append("\n");
        sb.append("Page: ").append(page.currentPage + 1).append(" of ").append(page.totalPages).append("\n\n");
        for (int i = 0; i < page.content.size(); i++) {
            CustomerResponse c = page.content.get(i);
            sb.append((i + 1)).append(". ");
            sb.append(c.nombre).append(" ").append(c.apellido);
            sb.append(" (ID: ").append(c.id).append(") - ").append(c.email);
            if (c.scoreCrediticio != null) sb.append(" - Score: ").append(c.scoreCrediticio);
            sb.append("\n");
        }
        return sb.toString();
    }
}
