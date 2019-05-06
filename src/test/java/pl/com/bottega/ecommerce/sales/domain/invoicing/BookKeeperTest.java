package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

    private BookKeeper bookKeeper;
    private InvoiceRequest invoiceRequest;
    private TaxPolicy taxPolicy;

    @Before
    public void setUp() throws Exception {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(mock(ClientData.class));
        taxPolicy = mock(TaxPolicy.class);
    }

    @Test
    public void shouldReturnOneInvoiceIfOneProductIsGiven() {
        RequestItem requestItem = new RequestItem(mock(ProductData.class), 1, new Money(1));
        invoiceRequest.add(requestItem);

        when(taxPolicy.calculateTax(any(), any())).thenReturn(new Tax(new Money(new BigDecimal(1)), "tax"));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems().size(), is(1));
    }

    @Test
    public void shouldReturnTwoInvoicesIfTwoProductsAreGiven() {
        RequestItem requestItem1 = new RequestItem(mock(ProductData.class), 1, new Money(1));
        RequestItem requestItem2 = new RequestItem(mock(ProductData.class), 1, new Money(1));
        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem2);

        when(taxPolicy.calculateTax(any(), any())).thenReturn(new Tax(new Money(new BigDecimal(1)), "tax"));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems().size(), is(2));
    }

    @Test
    public void shouldReturnZeroIfNoProductsAreGiven() {
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        assertThat(invoice.getItems().size(), is(0));
    }

    @Test
    public void shouldNotUseCalculateTaxMethodIfThereAreNoProducts() {
        bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(0)).calculateTax(any(), any());
    }

    @Test
    public void shouldUseCalculateTaxMethodTwiceIfTwoProductsAreGiven() {
        RequestItem requestItem1 = new RequestItem(mock(ProductData.class), 1, new Money(1));
        RequestItem requestItem2 = new RequestItem(mock(ProductData.class), 1, new Money(1));
        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem2);

        when(taxPolicy.calculateTax(any(), any())).thenReturn(new Tax(new Money(new BigDecimal(1)), "tax"));
        bookKeeper.issuance(invoiceRequest, taxPolicy);

        verify(taxPolicy, times(2)).calculateTax(any(), any());
    }

    @Test
    public void shouldReturnSumOfProductsPrice() {
        RequestItem requestItem1 = new RequestItem(mock(ProductData.class), 1, new Money(10));
        RequestItem requestItem2 = new RequestItem(mock(ProductData.class), 1, new Money(10));
        invoiceRequest.add(requestItem1);
        invoiceRequest.add(requestItem2);

        when(taxPolicy.calculateTax(any(), any())).thenReturn(new Tax(new Money(new BigDecimal(1)), "tax"));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getGros().toString(), is("22.00 €"));
    }
}