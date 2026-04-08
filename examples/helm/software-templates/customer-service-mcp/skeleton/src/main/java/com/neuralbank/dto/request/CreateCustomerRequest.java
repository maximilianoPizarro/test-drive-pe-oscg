package com.neuralbank.dto.request;

import com.neuralbank.enums.CustomerType;

import java.math.BigDecimal;

public class CreateCustomerRequest {

    public String identificacion;
    public String tipoIdentificacion;
    public String nombre;
    public String apellido;
    public String email;
    public String telefono;
    public String direccion;
    public String ciudad;
    public String estadoProvincia;
    public String codigoPostal;
    public Long paisId;
    public CustomerType tipoCliente;
    public BigDecimal scoreCrediticio;
}
