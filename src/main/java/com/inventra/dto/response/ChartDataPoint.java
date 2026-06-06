package com.inventra.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataPoint {

    @Schema(description = "Year", example = "2026")
    private int year;

    @Schema(description = "Month (1-12)", example = "3")
    private int month;

    @Schema(description = "Month label", example = "Mar")
    private String monthLabel;

    @Schema(description = "Number of STOCK_IN transactions", example = "12")
    private long inCount;

    @Schema(description = "Number of STOCK_OUT transactions", example = "8")
    private long outCount;

    @Schema(description = "Total stock-in quantity", example = "350")
    private long inQuantity;

    @Schema(description = "Total stock-out quantity", example = "120")
    private long outQuantity;
}
