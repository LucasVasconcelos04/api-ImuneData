package br.com.luquinhas.apiimunedata.controller;

import br.com.luquinhas.apiimunedata.entity.RegistroVacinacao;
import br.com.luquinhas.apiimunedata.service.RegistroVacinacaoService;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vacinacao")
public class RegistroVacinacaoController {

    private final RegistroVacinacaoService service;

    @Autowired
    public RegistroVacinacaoController(RegistroVacinacaoService service) {
        this.service = service;
    }

    // ==========================================
    // GET /api/vacinacao
    // Lista todos os registros
    // ==========================================
    @GetMapping
    public ResponseEntity<List<RegistroVacinacao>> listarTodos() {
        List<RegistroVacinacao> registros = service.listarTodos();
        return ResponseEntity.ok(registros);
    }

    // ==========================================
    // GET /api/vacinacao/{id}
    // Busca um registro especifico por ID
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<RegistroVacinacao> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(registro -> ResponseEntity.ok(registro))
                .orElse(ResponseEntity.notFound().build());
    }


    // ==========================================
    // POST /api/vacinacao
    // Cria um novo registro
    // ==========================================
    @PostMapping
    public ResponseEntity<RegistroVacinacao> criar(@RequestBody RegistroVacinacao registro) {
        RegistroVacinacao registroCriado = service.criar(registro);

        // Monta a URI do novo recurso (boa pratica REST para status 201)
        URI uri = URI.create("/api/vacinacao/" + registroCriado.getId());

        return ResponseEntity.created(uri).body(registroCriado);
    }

    // ==========================================
    // PUT /api/vacinacao/{id}
    // Atualiza um registro existente
    // ==========================================
    @PutMapping("/{id}")
    public ResponseEntity<RegistroVacinacao> atualizar(
            @PathVariable Long id,
            @RequestBody RegistroVacinacao dadosNovos) {

        return service.atualizar(id, dadosNovos)
                .map(registroAtualizado -> ResponseEntity.ok(registroAtualizado))
                .orElse(ResponseEntity.notFound().build());
    }

    // ==========================================
    // DELETE /api/vacinacao/{id}
    // Deleta um registro
    // ==========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        boolean deletado = service.deletar(id);

        if (deletado) {
            return ResponseEntity.noContent().build();  // 204 No Content
        }
        return ResponseEntity.notFound().build();        // 404 Not Found
    }

    // ==========================================
    // GET /api/vacinacao/vacina/{vacina}
    // Lista registros filtrados por tipo de vacina
    // ==========================================
    @GetMapping("/vacina/{vacina}")
    public ResponseEntity<List<RegistroVacinacao>> buscarPorVacina(@PathVariable String vacina) {
        List<RegistroVacinacao> registros = service.buscarPorVacina(vacina);
        return ResponseEntity.ok(registros);
    }

    // ==========================================
    // GET /api/vacinacao/estado/{estado}
    // Lista registros filtrados por estado
    // ==========================================
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<RegistroVacinacao>> buscarPorEstado(@PathVariable String estado) {
        List<RegistroVacinacao> registros = service.buscarPorEstado(estado);
        return ResponseEntity.ok(registros);
    }

    // ==========================================
    // GET /api/vacinacao/regiao/{regiao}
    // Lista registros filtrados por regiao geografica do Brasil
    // Valores aceitos: NORTE, NORDESTE, CENTRO_OESTE, SUDESTE, SUL
    // ==========================================
    @GetMapping("/regiao/{regiao}")
    public ResponseEntity<?> buscarPorRegiao(@PathVariable String regiao) {
        try {
            List<RegistroVacinacao> registros = service.buscarPorRegiao(regiao);
            return ResponseEntity.ok(registros);
        } catch (IllegalArgumentException e) {
            // Regiao invalida -> 400 Bad Request com mensagem clara
            Map<String, String> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            return ResponseEntity.badRequest().body(erro);
        }
    }

    // ==========================================
    // POST /api/vacinacao/upload-csv
    // Importa registros a partir de um arquivo CSV
    // ==========================================
    @PostMapping("/upload-csv")
    public ResponseEntity<Map<String, Object>> importarCsv(@RequestParam("arquivo") MultipartFile arquivo) {

        Map<String, Object> resposta = new HashMap<>();

        // Validacao: arquivo nao pode ser vazio
        if (arquivo.isEmpty()) {
            resposta.put("erro", "O arquivo enviado esta vazio");
            return ResponseEntity.badRequest().body(resposta);
        }

        // Validacao: deve ser um arquivo CSV
        String nomeArquivo = arquivo.getOriginalFilename();
        if (nomeArquivo == null || !nomeArquivo.toLowerCase().endsWith(".csv")) {
            resposta.put("erro", "O arquivo deve ter extensao .csv");
            return ResponseEntity.badRequest().body(resposta);
        }

        try {
            int totalImportado = service.importarCsv(arquivo);

            resposta.put("mensagem", "Importacao realizada com sucesso");
            resposta.put("totalRegistrosImportados", totalImportado);
            resposta.put("nomeArquivo", nomeArquivo);

            return ResponseEntity.status(HttpStatus.CREATED).body(resposta);

        } catch (IOException | CsvException e) {
            resposta.put("erro", "Erro ao processar o arquivo CSV: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
        }
    }
}
