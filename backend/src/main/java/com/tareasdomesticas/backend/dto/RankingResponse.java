package com.tareasdomesticas.backend.dto;

import java.util.List;

public class RankingResponse {
    private String mensaje;
    private List<RankingMemberResponse> ranking;

    public RankingResponse() {}

    public RankingResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public RankingResponse(List<RankingMemberResponse> ranking) {
        this.ranking = ranking;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<RankingMemberResponse> getRanking() {
        return ranking;
    }

    public void setRanking(List<RankingMemberResponse> ranking) {
        this.ranking = ranking;
    }
}
