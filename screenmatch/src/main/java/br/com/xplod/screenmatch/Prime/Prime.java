package br.com.xplod.screenmatch.Prime;

import br.com.xplod.screenmatch.model.DadosEpisodio;
import br.com.xplod.screenmatch.model.DadosSerie;
import br.com.xplod.screenmatch.model.DadosTemporada;
import br.com.xplod.screenmatch.model.Episodio;
import br.com.xplod.screenmatch.service.ConsumoAPI;
import br.com.xplod.screenmatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Prime {

    private ConverteDados conversor = new ConverteDados();
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=2dca93d0";

    public void exibeMenu(){
        System.out.print("Digite o nome da serie: ");

        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO
                + nomeSerie.replace(" ", "+")
                + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

        //System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        try {
            for (int i = 1; i <= dados.totalTemporadas(); i++){
                json = consumo.obterDados(ENDERECO
                        + nomeSerie.replace(" ", "+")
                        + "&season="
                        + i
                        + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
        } catch (Exception ex){
            System.out.println("\nSerie não encontrada!");
        }
		//temporadas.forEach(System.out::println);

        //temporadas.forEach(t->t.episodios()
        //        .forEach(e-> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());//permite adicionar novos titulos
        if(dadosEpisodios != null) {
            System.out.println("\nTop Episodios:");
            dadosEpisodios.stream()
                    .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                    //.peek(e-> System.out.println("Filtro (N/A) " + e))
                    .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                    //.peek(e-> System.out.println("Ordenação " + e))
                    .limit(10)
                    //.peek(e-> System.out.println("Top 10 " + e))
                    .map(e -> e.titulo().toUpperCase())
                    //.peek(e-> System.out.println("Maiusculo em map " + e))
                    .forEach(System.out::println);
        }else{
            System.out.println("\nNão há Top 10 aqui!!");
        }
//      adição de episodio e Busca de episodios

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        System.out.println("Digite o trecho do titulo: ");
        var trechoTitulo = leitura.nextLine();

        Optional<Episodio> episodioBusca = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase()
                        .contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if (episodioBusca.isPresent()) {
            System.out.println("Episodio encontrado: " + episodioBusca.get()
                    .getTemporada());
        }else{
            System.out.println("Episodio não existe!");
        }
//        episodios.forEach(System.out::println);
//
//        System.out.println("Episodios apartir de que ano: ");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null &&
//                        e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                        "Episodio: " + e.getTitulo() +
//                        "Data Lancamento: " + e.getDataLancamento()
//                                .format(formatador)
//                ));

        Map<Integer, Double> avaliacoesPorTemporadas = episodios.stream()
                .filter(e-> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                         Collectors.averagingDouble(Episodio::getAvaliacao)));
        //System.out.println("Avaliações por temporada: "+ avaliacoesPorTemporadas);

        DoubleSummaryStatistics estatisticas = episodios.stream()
                .filter(e-> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("Media: " + estatisticas.getAverage());
        System.out.println("Melhor: " + estatisticas.getMax());
        System.out.println("Pior: " + estatisticas.getMin());
        System.out.println("Quantidade: "+ estatisticas.getCount());
    }
}
