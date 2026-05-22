package br.com.luquinhas.apiimunedata.service;

import br.com.luquinhas.apiimunedata.entity.Regiao;
import br.com.luquinhas.apiimunedata.entity.Dose;
import br.com.luquinhas.apiimunedata.entity.RegistroVacinacao;
import br.com.luquinhas.apiimunedata.repository.RegistroVacinacaoRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegistroVacinacaoService {

    private final RegistroVacinacaoRepository repository;

    @Autowired
    public RegistroVacinacaoService(RegistroVacinacaoRepository repository) {
        this.repository = repository;
    }

    // ==========================================
    // OPERACOES CRUD
    // ==========================================

    // Lista todos os registros do banco
    public List<RegistroVacinacao> listarTodos() {
        return repository.findAll();
    }

    // Busca um registro por ID
    // Retorna Optional para forcar o Controller a tratar o caso "nao encontrado"
    public Optional<RegistroVacinacao> buscarPorId(Long id) {
        return repository.findById(id);
    }

    // Cria um novo registro
    public RegistroVacinacao criar(RegistroVacinacao registro) {
        return repository.save(registro);
    }

    // Atualiza um registro existente
    // Retorna Optional vazio se o ID nao existir
    public Optional<RegistroVacinacao> atualizar(Long id, RegistroVacinacao dadosNovos) {
        return repository.findById(id)
                .map(registroExistente -> {
                    registroExistente.setMunicipio(dadosNovos.getMunicipio());
                    registroExistente.setEstado(dadosNovos.getEstado());
                    registroExistente.setVacina(dadosNovos.getVacina());
                    registroExistente.setDose(dadosNovos.getDose());
                    registroExistente.setQuantidadeAplicada(dadosNovos.getQuantidadeAplicada());
                    registroExistente.setDataRegistro(dadosNovos.getDataRegistro());
                    return repository.save(registroExistente);
                });
    }

    // Deleta um registro pelo ID
    // Retorna true se deletou, false se o ID nao existia
    public boolean deletar(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    // ==========================================
    // CONSULTAS ESPECIALIZADAS
    // ==========================================

    // Busca todos os registros de uma vacina especifica
    public List<RegistroVacinacao> buscarPorVacina(String vacina) {
        return repository.findByVacina(vacina);
    }

    // Busca todos os registros de um estado especifico
    public List<RegistroVacinacao> buscarPorEstado(String estado) {
        return repository.findByEstado(estado);
    }

    // Busca todos os registros de uma regiao do Brasil
    // Ex: SUDESTE retorna registros de ES, MG, RJ e SP
    public List<RegistroVacinacao> buscarPorRegiao(String regiao) {
        Regiao regiaoEnum = Regiao.fromString(regiao);
        return repository.findByEstadoIn(regiaoEnum.getEstados());
    }

    // ==========================================
    // IMPORTACAO DE CSV (desafio tecnico)
    // ==========================================

    // Le um arquivo CSV com separador ';' e popula o banco
    // Formato esperado:
    // municipio;estado;vacina;dose;quantidadeAplicada;dataRegistro
    // Santo Andre;SP;BCG;PRIMEIRA;150;2026-01-15
    public int importarCsv(MultipartFile arquivo) throws IOException, CsvException {

        // Configura o parser do OpenCSV para usar ';' como separador
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(';')
                .build();

        List<RegistroVacinacao> registrosImportados = new ArrayList<>();

        // Try-with-resources garante que o Reader sera fechado automaticamente
        try (Reader reader = new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withCSVParser(parser)
                     .withSkipLines(1)  // Pula a linha do cabecalho
                     .build()) {

            List<String[]> linhas = csvReader.readAll();

            for (String[] linha : linhas) {
                RegistroVacinacao registro = new RegistroVacinacao();
                registro.setMunicipio(linha[0]);
                registro.setEstado(linha[1]);
                registro.setVacina(linha[2]);
                registro.setDose(Dose.valueOf(linha[3].toUpperCase()));
                registro.setQuantidadeAplicada(Integer.parseInt(linha[4]));
                registro.setDataRegistro(LocalDate.parse(linha[5]));

                registrosImportados.add(registro);
            }
        }

        // saveAll eh mais eficiente que salvar um por um
        repository.saveAll(registrosImportados);

        return registrosImportados.size();
    }
}
