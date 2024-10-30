package br.com.xplod.screenmatch.service;

public interface IConvertDados {
    <T> T obterDados(String json, Class<T> classe);
}
