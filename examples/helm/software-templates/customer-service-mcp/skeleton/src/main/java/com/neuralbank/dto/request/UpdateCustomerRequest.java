package com.neuralbank.dto.request;

import com.neuralbank.enums.CustomerType;

public class UpdateCustomerRequest {

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
}
