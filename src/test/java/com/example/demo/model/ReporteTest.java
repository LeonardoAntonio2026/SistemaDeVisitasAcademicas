package com.example.demo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReporteTest {

    @Test
    void getIdReporte() {
        Reporte reporte = new Reporte();
        reporte.setIdReporte(10);

        assertEquals(10, reporte.getIdReporte());
    }

    @Test
    void setIdReporte() {
        Reporte reporte = new Reporte();

        reporte.setIdReporte(25);

        assertEquals(25, reporte.getIdReporte());
    }

    @Test
    void getIdSolicitud() {
        Reporte reporte = new Reporte();
        reporte.setIdSolicitud(100);

        assertEquals(100, reporte.getIdSolicitud());
    }

    @Test
    void setIdSolicitud() {
        Reporte reporte = new Reporte();

        reporte.setIdSolicitud(200);

        assertEquals(200, reporte.getIdSolicitud());
    }

    @Test
    void getFecha() {
        Reporte reporte = new Reporte();
        reporte.setFecha("2026-08-14");

        assertEquals("2026-08-14", reporte.getFecha());
    }

    @Test
    void setFecha() {
        Reporte reporte = new Reporte();

        reporte.setFecha("2026-08-15");

        assertEquals("2026-08-15", reporte.getFecha());
    }

    @Test
    void getResultados() {
        Reporte reporte = new Reporte();
        reporte.setResultados("La visita fue satisfactoria");

        assertEquals("La visita fue satisfactoria", reporte.getResultados());
    }

    @Test
    void setResultados() {
        Reporte reporte = new Reporte();

        reporte.setResultados("Resultados de la visita");

        assertEquals("Resultados de la visita", reporte.getResultados());
    }

    @Test
    void getObservaciones() {
        Reporte reporte = new Reporte();
        reporte.setObservaciones("Todo salió correctamente");

        assertEquals("Todo salió correctamente", reporte.getObservaciones());
    }

    @Test
    void setObservaciones() {
        Reporte reporte = new Reporte();

        reporte.setObservaciones("Sin observaciones");

        assertEquals("Sin observaciones", reporte.getObservaciones());
    }

    @Test
    void getFechaCreacion() {
        Reporte reporte = new Reporte();
        reporte.setFechaCreacion("2026-08-14 10:30:00");

        assertEquals("2026-08-14 10:30:00", reporte.getFechaCreacion());
    }

    @Test
    void setFechaCreacion() {
        Reporte reporte = new Reporte();

        reporte.setFechaCreacion("2026-08-14 11:00:00");

        assertEquals("2026-08-14 11:00:00", reporte.getFechaCreacion());
    }

    @Test
    void getIdEstado() {
        Reporte reporte = new Reporte();
        reporte.setIdEstado(1);

        assertEquals(1, reporte.getIdEstado());
    }

    @Test
    void setIdEstado() {
        Reporte reporte = new Reporte();

        reporte.setIdEstado(3);

        assertEquals(3, reporte.getIdEstado());
    }

    @Test
    void getMotivo() {
        Reporte reporte = new Reporte();
        reporte.setMotivo("Visita académica");

        assertEquals("Visita académica", reporte.getMotivo());
    }

    @Test
    void setMotivo() {
        Reporte reporte = new Reporte();

        reporte.setMotivo("Actividad empresarial");

        assertEquals("Actividad empresarial", reporte.getMotivo());
    }

    @Test
    void getNombreEstado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Pendiente");

        assertEquals("Pendiente", reporte.getNombreEstado());
    }

    // Todos los estados visibles del reporte empiezan con "Reporte": es lo que
    // distingue en el badge un reporte "En revisión" de una solicitud "En
    // revisión", y es el mismo texto que devuelve Solicitud.getEstadoLegible().

    @Test
    void getEstadoLegiblePendiente() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Pendiente");

        assertEquals("Reporte por completar", reporte.getEstadoLegible());
    }

    @Test
    void getEstadoLegiblePendienteMinusculas() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("pendiente");

        assertEquals("Reporte por completar", reporte.getEstadoLegible());
    }

    @Test
    void getEstadoLegibleCompletado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Completado");

        assertEquals("Reporte en revisión", reporte.getEstadoLegible());
    }

    @Test
    void getEstadoLegibleCompletadoMinusculas() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("completado");

        assertEquals("Reporte en revisión", reporte.getEstadoLegible());
    }

    @Test
    void getEstadoLegibleAprobado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Aprobado");

        assertEquals("Reporte aprobado", reporte.getEstadoLegible());
    }

    @Test
    void getEstadoLegibleRechazado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Rechazado");

        assertEquals("Reporte rechazado", reporte.getEstadoLegible());
    }

    @Test
    void getEstadoLegibleOtroEstado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Otro");

        assertEquals("Otro", reporte.getEstadoLegible());
    }

    @Test
    void getClaseEstadoPendiente() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Pendiente");

        assertEquals("reporte-pendiente", reporte.getClaseEstado());
    }

    @Test
    void getClaseEstadoPendienteMinusculas() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("pendiente");

        assertEquals("reporte-pendiente", reporte.getClaseEstado());
    }

    @Test
    void getClaseEstadoCompletado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Completado");

        assertEquals("en-revision", reporte.getClaseEstado());
    }

    @Test
    void getClaseEstadoCompletadoMinusculas() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("completado");

        assertEquals("en-revision", reporte.getClaseEstado());
    }

    @Test
    void getClaseEstadoAprobado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Aprobado");

        assertEquals("aprobado", reporte.getClaseEstado());
    }

    @Test
    void getClaseEstadoAprobadoMinusculas() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("aprobado");

        assertEquals("aprobado", reporte.getClaseEstado());
    }

    @Test
    void getClaseEstadoRechazado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Rechazado");

        assertEquals("rechazado", reporte.getClaseEstado());
    }

    @Test
    void getClaseEstadoOtroEstado() {
        Reporte reporte = new Reporte();
        reporte.setNombreEstado("Otro");

        assertEquals("rechazado", reporte.getClaseEstado());
    }

    @Test
    void setNombreEstado() {
        Reporte reporte = new Reporte();

        reporte.setNombreEstado("Aprobado");

        assertEquals("Aprobado", reporte.getNombreEstado());
    }

    @Test
    void getNombreEmpresaActividad() {
        Reporte reporte = new Reporte();
        reporte.setNombreEmpresaActividad("Empresa ABC");

        assertEquals("Empresa ABC", reporte.getNombreEmpresaActividad());
    }

    @Test
    void setNombreEmpresaActividad() {
        Reporte reporte = new Reporte();

        reporte.setNombreEmpresaActividad("Empresa XYZ");

        assertEquals("Empresa XYZ", reporte.getNombreEmpresaActividad());
    }

    @Test
    void getLugarDireccion() {
        Reporte reporte = new Reporte();
        reporte.setLugarDireccion("Av. Reforma 123");

        assertEquals("Av. Reforma 123", reporte.getLugarDireccion());
    }

    @Test
    void setLugarDireccion() {
        Reporte reporte = new Reporte();

        reporte.setLugarDireccion("Calle Principal 456");

        assertEquals("Calle Principal 456", reporte.getLugarDireccion());
    }

    @Test
    void getIdUsuarioSolicitante() {
        Reporte reporte = new Reporte();
        reporte.setIdUsuarioSolicitante(50);

        assertEquals(50, reporte.getIdUsuarioSolicitante());
    }

    @Test
    void setIdUsuarioSolicitante() {
        Reporte reporte = new Reporte();

        reporte.setIdUsuarioSolicitante(75);

        assertEquals(75, reporte.getIdUsuarioSolicitante());
    }

    @Test
    void getNombreSolicitante() {
        Reporte reporte = new Reporte();
        reporte.setNombreSolicitante("Juan Pérez");

        assertEquals("Juan Pérez", reporte.getNombreSolicitante());
    }

    @Test
    void setNombreSolicitante() {
        Reporte reporte = new Reporte();

        reporte.setNombreSolicitante("María López");

        assertEquals("María López", reporte.getNombreSolicitante());
    }

    @Test
    void getTotalEstudiantes() {
        Reporte reporte = new Reporte();
        reporte.setTotalEstudiantes(30);

        assertEquals(30, reporte.getTotalEstudiantes());
    }

    @Test
    void setTotalEstudiantes() {
        Reporte reporte = new Reporte();

        reporte.setTotalEstudiantes(50);

        assertEquals(50, reporte.getTotalEstudiantes());
    }

    @Test
    void getCorreoSolicitante() {
        Reporte reporte = new Reporte();
        reporte.setCorreoSolicitante("juan.perez@example.com");

        assertEquals("juan.perez@example.com", reporte.getCorreoSolicitante());
    }

    @Test
    void setCorreoSolicitante() {
        Reporte reporte = new Reporte();

        reporte.setCorreoSolicitante("maria.lopez@example.com");

        assertEquals("maria.lopez@example.com", reporte.getCorreoSolicitante());
    }

    @Test
    void getFechaSolicitud() {
        Reporte reporte = new Reporte();
        reporte.setFechaSolicitud("2026-08-10");

        assertEquals("2026-08-10", reporte.getFechaSolicitud());
    }

    @Test
    void setFechaSolicitud() {
        Reporte reporte = new Reporte();

        reporte.setFechaSolicitud("2026-08-12");

        assertEquals("2026-08-12", reporte.getFechaSolicitud());
    }
}