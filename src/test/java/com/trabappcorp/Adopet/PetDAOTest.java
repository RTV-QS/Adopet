package DAO;

import JPA.EntityManagerSingleton;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import modelo.Adotante;
import modelo.FiltroAdotante;
import modelo.Pet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Casos de teste unitários da classe PetDAO (CT-PD): 84 execuções.
 *
 * Complexidade ciclomática da classe sob teste: 17 (findPetsFor = 14),
 * medida com lizard e registrada no Plano de Teste, seção 1.1.
 * A suíte cobre a montagem da consulta, não as operações genéricas de CRUD.
 *
 * Técnica de isolamento: mockStatic de EntityManagerSingleton para evitar
 * a abertura de um EntityManager real no construtor de JpaDAO. EntityManager
 * e TypedQuery são mocks; cada caso instancia um PetDAO pelo construtor privado
 * via reflexão, sem compartilhar o estado do singleton entre os testes.
 *
 * Técnicas de projeto: partição de equivalência para as 64 combinações dos
 * seis filtros opcionais e análise de valores próximos de zero e entradas
 * textuais para 18 casos adicionais. Dois casos verificam lista vazia e falha
 * da dependência. Entradas e resultados estão na planilha e no CSV CT-PD.
 *
 * Oráculo: código, com asserções sobre JPQL, parâmetros, identidade da lista
 * e interações. A filtragem em banco real pertence à integração da Entrega 2.
 * Nenhum caso está desabilitado: os 84 passaram sem defeito novo confirmado.
 */
class PetDAOTest {
    private static final String BASE = "SELECT P FROM modelo.Pet P JOIN P.variacao V ";
    private static final String[] NAMES = {"alturaMenor", "gastoMensalMenor", "pesoMenor", "especie", "porte", "raca"};
    private static final String[] CLAUSES = {" AND P.altura<:alturaMenor", " AND P.gastoMensal<:gastoMensalMenor", " AND P.peso<:pesoMenor", " AND V.especie=:especie", " AND V.porte=:porte", " AND V.raca=:raca"};
    private static final Object[] VALUES = {50.0, 200.0, 15.0, "Cachorro", "Pequeno", "SRD"};

    static Stream<Integer> combinations() { return IntStream.range(0, 64).boxed(); }

    @ParameterizedTest(name = "CT-PD-C{0}: combinação dos seis filtros")
    @MethodSource("combinations")
    void queryAndParametersForEveryCombination(int mask) throws Exception {
        Object[] values = new Object[6];
        String expected = BASE;
        if (mask != 0) expected += "WHERE 1=1 ";
        for (int i = 0; i < 6; i++) {
            if ((mask & (1 << i)) != 0) { values[i] = VALUES[i]; expected += CLAUSES[i]; }
        }
        exercise(values, expected, Arrays.asList(new Pet(), new Pet()));
    }

    static Stream<Arguments> boundaries() {
        return IntStream.range(0, 6).boxed().flatMap(i -> {
            Object[] samples = i < 3 ? new Object[]{-0.01, 0.0, 0.01} : new Object[]{"", " ", "d'agua"};
            return Arrays.stream(samples).map(value -> Arguments.of(i, value));
        });
    }

    @ParameterizedTest(name = "CT-PD-L: campo {0}, valor [{1}]")
    @MethodSource("boundaries")
    void nonNullBoundaryValuesRemainBoundParameters(int index, Object value) throws Exception {
        Object[] values = new Object[6]; values[index] = value;
        exercise(values, BASE + "WHERE 1=1 " + CLAUSES[index], Collections.emptyList());
    }

    @Test
    void ctPdR01ReturnsEmptyListFromJpa() throws Exception {
        exercise(new Object[6], BASE, Collections.emptyList());
    }

    @Test
    void ctPdR02PropagatesPersistenceFailure() throws Exception {
        EntityManager em = mock(EntityManager.class);
        @SuppressWarnings("unchecked") TypedQuery<Pet> query = mock(TypedQuery.class);
        when(em.createQuery(BASE, Pet.class)).thenReturn(query);
        RuntimeException failure = new RuntimeException("falha simulada");
        when(query.getResultList()).thenThrow(failure);
        try (MockedStatic<EntityManagerSingleton> singleton = mockStatic(EntityManagerSingleton.class)) {
            singleton.when(EntityManagerSingleton::getInstance).thenReturn(em);
            Adotante adotante = new Adotante(); adotante.setFiltro(new FiltroAdotante());
            PetDAO dao = freshDao();
            assertSame(failure, assertThrows(RuntimeException.class, () -> dao.findPetsFor(adotante)));
            verify(query).getResultList();
        }
    }

    private PetDAO freshDao() throws Exception {
        Constructor<PetDAO> constructor = PetDAO.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private void exercise(Object[] values, String expected, List<Pet> result) throws Exception {
        EntityManager em = mock(EntityManager.class);
        @SuppressWarnings("unchecked") TypedQuery<Pet> query = mock(TypedQuery.class);
        when(em.createQuery(expected, Pet.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(result);
        FiltroAdotante filtro = new FiltroAdotante((String) values[3], (String) values[5], (String) values[4], (Double) values[0], (Double) values[2], (Double) values[1]);
        Adotante adotante = new Adotante(); adotante.setFiltro(filtro);
        try (MockedStatic<EntityManagerSingleton> singleton = mockStatic(EntityManagerSingleton.class)) {
            singleton.when(EntityManagerSingleton::getInstance).thenReturn(em);
            assertSame(result, freshDao().findPetsFor(adotante));
            singleton.verify(EntityManagerSingleton::getInstance);
        }
        verify(em).createQuery(expected, Pet.class);
        for (int i = 0; i < 6; i++) if (values[i] != null) verify(query).setParameter(NAMES[i], values[i]);
        verify(query).getResultList();
        verifyNoMoreInteractions(em, query);
    }
}
