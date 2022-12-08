package org.demo.microservice.schoppingcart.schoppingcartapp.core;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.demo.microservice.schoppingcart.schoppingcartapp.TotalInformationAssert.assertThatTotalInformation;
import static org.demo.microservice.schoppingcart.schoppingcartapp.core.input.ProductCategory.TOYS;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.demo.microservice.schoppingcart.schoppingcartapp.Specification;
import org.demo.microservice.schoppingcart.schoppingcartapp.TotalInformationAssert;
import org.demo.microservice.schoppingcart.schoppingcartapp.core.input.Product;
import org.demo.microservice.schoppingcart.schoppingcartapp.core.input.ProductCategory;
import org.demo.microservice.schoppingcart.schoppingcartapp.core.input.ProductTestBuilder;
import org.demo.microservice.schoppingcart.schoppingcartapp.core.input.ShoppingCartData;
import org.demo.microservice.schoppingcart.schoppingcartapp.core.output.ShoppingCartOverview;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ShoppingCartOverviewServiceTest.TestConfig.class)
class ShoppingCartOverviewServiceTest {

    @Autowired
    private ShoppingCartOverviewService shoppingCartOverviewService;

    @Test
    void should_return_a_single_product_that_was_in_the_input() {

        ShoppingCartData input = new ShoppingCartData();
        input.addProduct(new ProductTestBuilder()
                        .withName("does not matter")
                        .withId("prod-id-1")
                        .withListPrice(valueOf(10.99))
                        .withSellPrice(valueOf(9.99))
                .build());

        ShoppingCartOverview output = shoppingCartOverviewService.generateShoppingCartOverview(input);

        assertThat(output).isNotNull();
        assertThat(output.getProducts()).hasSize(1);
        assertThat(output.getProducts().get(0).getName()).isEqualTo("does not matter");
        assertThat(output.getProducts().get(0).getId()).isEqualTo("prod-id-1");
        assertThat(output.getProducts().get(0).getListPrice()).isEqualTo(valueOf(10.99));
        assertThat(output.getProducts().get(0).getSellPrice()).isEqualTo(valueOf(9.99));
        assertThat(output.getProducts().get(0).getProductCategory()).isEqualTo(TOYS);
        assertThat(output.getProducts().get(0).getDiscountPercentage()).isEqualByComparingTo(valueOf(90.90));

        assertThat(output.getTotals().getTotalAmountToBePaid().amount()).isEqualTo(valueOf(9.99));
        assertThat(output.getTotals().getTotalListPriceAmount().amount()).isEqualTo(valueOf(10.99));
        assertThat(output.getTotals().getTotalSellPriceAmount().amount()).isEqualTo(valueOf(9.99));
        assertThat(output.getTotals().getOverallDiscountAmountPercentage()).isEqualByComparingTo(valueOf(90.90));
    }

    @Test
    void should_return_a_three_products_that_was_in_the_input() {

        ShoppingCartData input = new ShoppingCartData();
        input.addProduct(new ProductTestBuilder()
                .withName("My first book")
                .withListPrice(valueOf(5.99))
                .build());
        input.addProduct(new ProductTestBuilder()
                .withName("My first phone")
                .build());
        input.addProduct(new ProductTestBuilder()
                .withName("My first toy")
                .build());

        ShoppingCartOverview output = shoppingCartOverviewService.generateShoppingCartOverview(input);

        assertThat(output).isNotNull();
        assertThat(output.getProducts()).hasSize(3);
        assertThat(output.getProducts().get(0).getName()).isEqualTo("My first book");
        assertThat(output.getProducts().get(1).getName()).isEqualTo("My first phone");
        assertThat(output.getProducts().get(2).getName()).isEqualTo("My first toy");

        assertThat(output.getTotals().getTotalAmountToBePaid().amount()).isEqualTo(valueOf(1.5));
        assertThat(output.getTotals().getTotalListPriceAmount().amount()).isEqualTo(valueOf(7.99));
        assertThat(output.getTotals().getTotalSellPriceAmount().amount()).isEqualTo(valueOf(1.5));
        assertThat(output.getTotals().getOverallDiscountAmountPercentage()).isEqualByComparingTo(valueOf(18.80));
    }

    @Test
    void test_that_when_a_customer_has_category_reduction_for_toys_these_are_calculated() {
        ShoppingCartData input = new ShoppingCartData();
        input.setCustomerId("customer-1");
        input.addProduct(new ProductTestBuilder()
                .withName("My first book")
                .withListPrice(valueOf(5.99))
                .withCategory(ProductCategory.BOOKS)
                .build());
        input.addProduct(new ProductTestBuilder()
                .withName("My first phone")
                .withCategory(TOYS)
                .withSellPrice(valueOf(100))
                .withListPrice(valueOf(100))
                .build());
        input.addProduct(new ProductTestBuilder()
                .withName("My first toy")
                .withCategory(TOYS)
                .withSellPrice(valueOf(100))
                .withListPrice(valueOf(100))
                .build());

        ShoppingCartOverview output = shoppingCartOverviewService.generateShoppingCartOverview(input);

        assertThat(output).isNotNull();

        assertThat(output.getTotals().getTotalAmountToBePaid().amount()).isEqualTo(valueOf(100.5));
        assertThat(output.getTotals().getTotalListPriceAmount().amount()).isEqualTo(valueOf(205.99));
        assertThat(output.getTotals().getTotalSellPriceAmount().amount()).isEqualTo(valueOf(100.5));
    }

    @Test
    void customSpecs_forGivenPart_v1() {
        //given
        ShoppingCartData input = new ShoppingCartData();
        input.setCustomerId("customer-1");
        //and
        Specification.from(List.of(
                " last_price | sell_price | category | name",
                " ==================================================",
                " 5.99       | n/a        | BOOKS    | My first book",
                " 100        | 100        | TOYS     | My first phone",
                " 100        | 100        | TOYS     | My first toy"
            )).stream()
            .map(this::specToProduct)
            .forEach(input::addProduct);

        //when
        ShoppingCartOverview output = shoppingCartOverviewService.generateShoppingCartOverview(input);

        //then
        assertThat(output).isNotNull();
        assertThatTotalInformation(output.getTotals())
            .hasTotalAmountToBePaid(valueOf(100.5d))
            .hasTotalListPriceAmount(valueOf(205.99d))
            .hasTotalSellPriceAmount(valueOf(100.5d));
    }


    private Product specToProduct(Specification.Row row) {
        return new ProductTestBuilder()
            .withName(row.get("name"))
            .withListPrice(new BigDecimal(row.get("last_price")))
            .withSellPrice(row.getIfApplicable("sell_price").map(BigDecimal::new))
            .withCategory(ProductCategory.valueOf(row.get("category")))
            .build();
    }


    @Test
    void usingSpecsForGiven_andSpecsPlusCustomAssertForThenPart_v2() {
        //given
        ShoppingCartData input = new ShoppingCartData();
        input.setCustomerId("customer-1");
        //and
        Specification.from(List.of(
                " last_price | sell_price | category | name",
                " ==================================================",
                " 5.99       | n/a        | BOOKS    | My first book",
                " 100        | 100        | TOYS     | My first phone",
                " 100        | 100        | TOYS     | My first toy"
            )).stream()
            .map(this::specToProduct)
            .forEach(input::addProduct);

        //when
        ShoppingCartOverview output = shoppingCartOverviewService.generateShoppingCartOverview(input);

        //then
        Specification.from(List.of(
                " spec_key          | expected_value    ",
                "=======================================",
                " total_to_be_paid  | 100.5             ",
                " total_list_price  | 205.99            ",
                " total_sell_amount | 100.5             "
            )).stream()
            .forEach(row -> {
                var tested = assertThatTotalInformation(output.getTotals());
                var expectedValue = row.getBigDecimal("expected_value");
                runDynamicTest(tested, row.get("spec_key"), expectedValue);
            });
    }

    private void runDynamicTest(TotalInformationAssert tested, String specKey, BigDecimal expectedValue) {
        specKeysToCustomAsserts.get(specKey).accept(tested, expectedValue);
    }

    private final Map<String, BiConsumer<TotalInformationAssert, Object>> specKeysToCustomAsserts = Map.of(
        "total_to_be_paid", (tested, expectedOut) -> tested.hasTotalAmountToBePaid((BigDecimal) expectedOut),
        "total_list_price", (tested, expectedOut) -> tested.hasTotalListPriceAmount((BigDecimal) expectedOut),
        "total_sell_amount", (tested, expectedOut) -> tested.hasTotalSellPriceAmount((BigDecimal) expectedOut)
    );


    @ComponentScan(value = {
            "org.demo.microservice.schoppingcart.schoppingcartapp.core" })
    public static class TestConfig {

    }
}