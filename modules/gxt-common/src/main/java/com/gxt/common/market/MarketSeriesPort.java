package com.gxt.common.market;

import com.gxt.common.dto.MarketBarDto;
import java.time.LocalDate;
import java.util.List;

public interface MarketSeriesPort {
    List<MarketBarDto> getBars(String instrument, LocalDate from, LocalDate to);
}
