package com.tareasdomesticas.backend.dto;

import java.util.List;

public class FiltrarTareasResponse {

    private String mensaje;
    private FiltrosAplicados filtrosAplicados;
    private List<DetalleTareaResponse> tareas;

    public FiltrarTareasResponse() {
    }

    public FiltrarTareasResponse(String mensaje, FiltrosAplicados filtrosAplicados,
                                 List<DetalleTareaResponse> tareas) {
        this.mensaje = mensaje;
        this.filtrosAplicados = filtrosAplicados;
        this.tareas = tareas;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public FiltrosAplicados getFiltrosAplicados() {
        return filtrosAplicados;
    }

    public void setFiltrosAplicados(FiltrosAplicados filtrosAplicados) {
        this.filtrosAplicados = filtrosAplicados;
    }

    public List<DetalleTareaResponse> getTareas() {
        return tareas;
    }

    public void setTareas(List<DetalleTareaResponse> tareas) {
        this.tareas = tareas;
    }

    public static class FiltrosAplicados {

        private List<String> estados;
        private List<String> prioridades;
        private List<Long> idsMiembros;

        public FiltrosAplicados() {
        }

        public FiltrosAplicados(List<String> estados, List<String> prioridades, List<Long> idsMiembros) {
            this.estados = estados;
            this.prioridades = prioridades;
            this.idsMiembros = idsMiembros;
        }

        public List<String> getEstados() {
            return estados;
        }

        public void setEstados(List<String> estados) {
            this.estados = estados;
        }

        public List<String> getPrioridades() {
            return prioridades;
        }

        public void setPrioridades(List<String> prioridades) {
            this.prioridades = prioridades;
        }

        public List<Long> getIdsMiembros() {
            return idsMiembros;
        }

        public void setIdsMiembros(List<Long> idsMiembros) {
            this.idsMiembros = idsMiembros;
        }
    }
}
