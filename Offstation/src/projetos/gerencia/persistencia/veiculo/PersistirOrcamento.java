package projetos.gerencia.persistencia.veiculo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jdbchelper.QueryResult;
import projetos.gerencia.Principal;
import projetos.gerencia.exceptions.ComprarException;
import projetos.gerencia.exceptions.ObjetoInvalidoException;
import projetos.gerencia.negocio.produto.IProduto;
import projetos.gerencia.negocio.veiculo.IOrcamento;
import projetos.gerencia.negocio.veiculo.IVeiculo;
import projetos.gerencia.negocio.veiculo.Orcamento;
import projetos.gerencia.persistencia.Conectar;
import projetos.gerencia.persistencia.produto.PersistirProduto;

public class PersistirOrcamento {

    private final static Map<IVeiculo, PersistirOrcamento> INSTANCIAS = new HashMap();
    private IVeiculo veiculo;

    public PersistirOrcamento(IVeiculo veiculo) throws ObjetoInvalidoException {
        this.setVeiculo(veiculo);
    }

    public static PersistirOrcamento getInstancia(IVeiculo veiculo) {
        PersistirOrcamento persistencia = PersistirOrcamento.INSTANCIAS.get(veiculo);
        if ((persistencia == null)) {
            try {
                persistencia = new PersistirOrcamento(veiculo);
                PersistirOrcamento.INSTANCIAS.put(veiculo, persistencia);
            } catch (ObjetoInvalidoException error) {
                Principal.getInstancia().log("Não foi possível gerar uma persistência para esse veículo. Erro: " + error.getMessage());
            }
        }
        return persistencia;
    }

    public void removerProduto(IProduto produto) {
        if ((this.getVeiculo() != null)) {

        }
    }

    public void adicionarProduto(IOrcamento orcamento) throws ComprarException {
        if ((this.getVeiculo() != null)) {
            if ((this.getVeiculo().getId() > 0)) {
                String sql = "INSERT INTO `orcamento` ( `id`, `veiculoID`, `pecaID`, `quantidade`, `data` ) VALUES ( NULL, ?, ?, ?, NOW() )";
                Object[] params = new Object[]{this.getVeiculo().getId(), orcamento.getProduto().getId(), orcamento.getQuantidade()};
                Principal.getInstancia().gerenciarTransacao(sql, params);
            } else {
                Principal.getInstancia().log("Veiculo ainda não está salvo no banco de dados.");
            }
        } else {
            Principal.getInstancia().log("Nao é possível adicionar um produto em um objeto nulo.");
        }
    }

    public List<IOrcamento> pegarOrcamentos() {
        Map<Long, IProduto> produtos = new HashMap();
        List<IOrcamento> orcamento = new ArrayList();
        QueryResult resultados = Conectar.getInstancia().getJdbc().query("SELECT * FROM `orcamento` WHERE ( `veiculoID` = ? )", new Object[]{this.getVeiculo().getId()});

        while (resultados.next()) {
            IProduto produto = produtos.get(resultados.getLong("pecaID"));
            if ((produto == null)) {
                produto = PersistirProduto.getInstancia().recuperar(resultados.getInt("pecaID"));
                produtos.put(produto.getId(), produto);
            }

            orcamento.add(new Orcamento(this.getVeiculo(), produto, resultados.getInt("id"), resultados.getInt("quantidade"), resultados.getString("data")));
            Principal.getInstancia().log(new StringBuilder().append("Orcamento do produto '").append(produto.getNome()).append("' adicionada na lista.").toString());
        }

        resultados.close();
        return orcamento;
    }

    public IVeiculo getVeiculo() {
        return this.veiculo;
    }

    private void setVeiculo(IVeiculo veiculo) throws ObjetoInvalidoException {
        if ((veiculo == null)) {
            throw (new ObjetoInvalidoException("O veículo não pode ser uma instancia nula."));
        } else {
            this.veiculo = veiculo;
        }
    }

}
