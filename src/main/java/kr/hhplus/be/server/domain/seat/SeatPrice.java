package kr.hhplus.be.server.domain.seat;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum SeatPrice {
    VIP(new BigDecimal(20000)),
    PREMIUM(new BigDecimal(15000)),
    NORMAL(new BigDecimal(10000));

            private final BigDecimal price;

            SeatPrice(BigDecimal price) {
                this.price = price;
            }

    public static BigDecimal getPriceByGrade(SeatGrade grade) {
        return SeatPrice.valueOf(grade.name()).getPrice();
    }
        }