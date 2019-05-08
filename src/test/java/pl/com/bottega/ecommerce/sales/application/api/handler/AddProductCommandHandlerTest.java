package pl.com.bottega.ecommerce.sales.application.api.handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.application.api.command.AddProductCommand;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.client.ClientRepository;
import pl.com.bottega.ecommerce.sales.domain.equivalent.SuggestionService;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductRepository;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import pl.com.bottega.ecommerce.system.application.SystemContext;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AddProductCommandHandlerTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private SuggestionService suggestionService;
    @Mock
    private ClientRepository clientRepository;

    private Client client;
    private AddProductCommandHandler addProductCommandHandler;
    private AddProductCommand addProductCommand;
    private Product product;
    private Product product2;
    private Reservation reservation;
    private SystemContext systemContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        systemContext = new SystemContext();
        addProductCommand = new AddProductCommand(Id.generate(), Id.generate(), 1);
        product = new Product(Id.generate(), new Money(new BigDecimal(10)), "name", ProductType.FOOD);
        client = new Client();
        reservation = new Reservation(Id.generate(), Reservation.ReservationStatus.OPENED, new ClientData(), new Date());

        addProductCommandHandler = new AddProductCommandHandler(reservationRepository, productRepository, suggestionService, clientRepository, systemContext);

        when(reservationRepository.load(any())).thenReturn(reservation);
        when(productRepository.load(any())).thenReturn(product);
        when(clientRepository.load(any())).thenReturn(client);
        when(suggestionService.suggestEquivalent(product, client)).thenReturn(product);
        doNothing().when(reservationRepository).save(any());
    }

    @Test
    public void shouldNotEnterIfWhenProductIsAvailable() {
        addProductCommandHandler.handle(addProductCommand);
        verify(suggestionService, never()).suggestEquivalent(any(), any());
        verify(clientRepository, never()).load(any());
    }
}