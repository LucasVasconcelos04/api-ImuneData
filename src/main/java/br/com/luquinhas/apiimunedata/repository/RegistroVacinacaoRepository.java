package br.com.luquinhas.apiimunedata.repository;

import br.com.luquinhas.apiimunedata.entity.RegistroVacinacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroVacinacaoRepository extends JpaRepository<RegistroVacinacao, Long> {

    // Busca todos os registros de uma vacina especifica (ex: BCG, Gripe)
    List<RegistroVacinacao> findByVacina(String vacina);

    // Busca todos os registros de um estado especifico (ex: SP, RJ)
    List<RegistroVacinacao> findByEstado(String estado);

    // Busca registros cujo estado esteja na lista fornecida
    // Sera usado para buscar por regiao (ex: SUDESTE = ES, MG, RJ, SP)
    List<RegistroVacinacao> findByEstadoIn(List<String> estados);
}
