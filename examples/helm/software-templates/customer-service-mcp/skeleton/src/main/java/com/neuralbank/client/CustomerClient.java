package com.neuralbank.client;

import com.neuralbank.dto.request.CreateCustomerRequest;
import com.neuralbank.dto.request.UpdateCustomerRequest;
import com.neuralbank.dto.response.CustomerResponse;
import com.neuralbank.dto.response.CreditScoreResponse;
import com.neuralbank.dto.response.PageResponse;
import com.neuralbank.enums.CustomerType;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.Map;

@Path("/api/v1/customers")
@RegisterRestClient(configKey = "customerclient")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CustomerClient {

    @POST
    CustomerResponse createCustomer(CreateCustomerRequest request);

    @GET
    @Path("/{customerId}")
    CustomerResponse getCustomerById(@PathParam("customerId") Long customerId);

    @GET
    @Path("/identification/{identificacion}")
    CustomerResponse getCustomerByIdentificacion(@PathParam("identificacion") String identificacion);

    @PUT
    @Path("/{customerId}")
    CustomerResponse updateCustomer(@PathParam("customerId") Long customerId, UpdateCustomerRequest request);

    @GET
    PageResponse<CustomerResponse> searchCustomers(
            @RestQuery("page") @DefaultValue("0") int page,
            @RestQuery("size") @DefaultValue("20") int size,
            @RestQuery("search") String search,
            @RestQuery("tipoCliente") CustomerType tipoCliente,
            @RestQuery("ciudad") String ciudad
    );

    @GET
    @Path("/{customerId}/credit-score")
    CreditScoreResponse getCreditScore(@PathParam("customerId") Long customerId);

    @POST
    @Path("/{customerId}/credit-score/calculate")
    CreditScoreResponse calculateCreditScore(@PathParam("customerId") Long customerId);

    @POST
    @Path("/{customerId}/activate")
    void activateCustomer(@PathParam("customerId") Long customerId);

    @POST
    @Path("/{customerId}/deactivate")
    void deactivateCustomer(@PathParam("customerId") Long customerId, Map<String, String> body);

    @PUT
    @Path("/{customerId}/risk-level")
    void updateRiskLevel(@PathParam("customerId") Long customerId, Map<String, String> body);
}
