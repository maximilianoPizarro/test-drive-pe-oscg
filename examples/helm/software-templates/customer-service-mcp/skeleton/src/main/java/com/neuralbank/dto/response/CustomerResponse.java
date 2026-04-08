package com.neuralbank.dto.response;

import com.neuralbank.enums.CustomerType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerResponse {

    public Long id;
    public String identificacion;
    public String tipoIdentificacion;
    public String nombre;
    public String apellido;
    public String nombreCompleto;
    public String email;
    public String telefono;
    public String direccion;
    public String ciudad;
    public String estadoProvincia;
    public String codigoPostal;
    public Long paisId;
    public CustomerType tipoCliente;
    public BigDecimal scoreCrediticio;
    public String nivelRiesgo;
    public LocalDate fechaRegistro;
    public Boolean activo;
    public Long ejecutivoId;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
}
