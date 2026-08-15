package com.gxt.backtest.market;

import com.gxt.common.dto.MarketBarDto;
import com.gxt.common.exception.ResourceNotFoundException;
import com.gxt.common.market.MarketSeriesPort;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UnavailableMarketSeriesService implements MarketSeriesPort {

    @Override
    public List<MarketBarDto> getBars(String instrument, LocalDate from, LocalDate to) {
        throw new ResourceNotFoundException(
                "Market data module is not loaded. Restore gxt-market-data to run backtests for "
                        + instrument);
    }
}
