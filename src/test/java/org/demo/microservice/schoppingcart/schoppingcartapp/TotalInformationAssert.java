package org.demo.microservice.schoppingcart.schoppingcartapp;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Function;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.demo.microservice.schoppingcart.schoppingcartapp.core.output.TotalsInformation;

public class TotalInformationAssert extends AbstractAssert<TotalInformationAssert, TotalsInformation> {

    public TotalInformationAssert(TotalsInformation actual) {
        super(actual, TotalInformationAssert.class);
    }

    public static TotalInformationAssert assertThatTotalInformation(TotalsInformation actual) {
        return new TotalInformationAssert(actual);
    }

    public TotalInformationAssert hasTotalAmountToBePaid(BigDecimal expected) {
        Assertions.assertThat(actual.getTotalAmountToBePaid().amount())
            .isEqualTo(expected);
        return this;
    }

    public TotalInformationAssert hasTotalSellPriceAmount(BigDecimal expected) {
        Assertions.assertThat(actual.getTotalSellPriceAmount().amount())
            .isEqualTo(expected);
        return this;
    }

    public TotalInformationAssert hasTotalListPriceAmount(BigDecimal expected) {
        Assertions.assertThat(actual.getTotalListPriceAmount().amount())
            .isEqualTo(expected);
        return this;
    }



    // a bit more extreme, only needed for v2 :)
    public TotalInformationAssert hasFieldWithValue(String specKey, Object expected) {
        specKeysToCustomAsserts.get(specKey).apply(expected);
        return this;
    }
    private final Map<String, Function<Object, TotalInformationAssert>> specKeysToCustomAsserts = Map.of(
        "total_to_be_paid", (expected) -> this.hasTotalAmountToBePaid((BigDecimal) expected),
        "total_list_price", (expected) -> this.hasTotalAmountToBePaid((BigDecimal) expected),
        "total_sell_amount", (expected) -> this.hasTotalAmountToBePaid((BigDecimal) expected)
    );
}