CREATE SCHEMA stock;

CREATE TABLE stock.company_profile (
    market varchar(20) not null,
    ticker varchar(20) not null,
    company_name varchar(100) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp,
    primary key(market, ticker)
);

CREATE TABLE stock.price_feed (
    id uuid not null default gen_random_uuid() primary key,
    market varchar(20) not null default 'NASDAQ',
    ticker varchar(20) not null,
    trade_date date not null default current_date,
    price numeric(10,2) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp,
    unique(market,ticker,trade_date)
);

CREATE TABLE stock.volume_feed (
    id uuid not null default gen_random_uuid() primary key,
    market varchar(20) not null default 'NASDAQ',
    ticker varchar(20) not null,
    trade_date date not null default current_date,
    volume numeric(15,2) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp,
    unique(market,ticker,trade_date)
);

do $$
begin
    INSERT INTO stock.price_feed (
        ticker, price
    ) values
        ('MSFT', 424.57),
        ('AAPL', 169.01),
        ('NVDA', 895.70),
        ('AMZN', 180.54),
        ('META', 493.90),
        ('AVGO',1318.97),
        ('GOOG', 155.09),
        ('TSLA', 166.62),
        ('COST', 709.51),
        ('AMD' , 177.09),
        ('NFLX', 611.49),
        ('PEP' , 171.63),
        ('ADBE', 497.70),
        ('LIN' , 461.19),
        ('CSCO',  49.22);

    INSERT INTO stock.volume_feed (
        ticker, volume
    ) values
        ('MSFT', 22740970),
        ('AAPL', 61281215),
        ('NVDA', 54023118),
        ('AMZN', 43970295),
        ('META', 17730493),
        ('AVGO',  3077463),
        ('GOOG', 23175556),
        ('TSLA',102231390),
        ('COST',  2137676),
        ('AMD' , 78105903),
        ('NFLX',  4503895),
        ('PEP' ,  5905464),
        ('ADBE',  3667357),
        ('LIN' ,  2403731),
        ('CSCO', 19799990);        
end;$$