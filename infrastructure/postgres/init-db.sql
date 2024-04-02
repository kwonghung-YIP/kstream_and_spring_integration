CREATE SCHEMA stock;

CREATE TABLE stock.quote (
    id uuid not null default gen_random_uuid() primary key,
    ticker varchar(20) not null,
    price numeric(10,2) not null,
    volume numeric(15,2) not null,
    ver int not null default 1,
    created timestamp not null default current_timestamp,
    lastupd timestamp not null default current_timestamp
);

do $$
begin
    INSERT INTO stock.quote (
        ticker, price, volume
    ) values
        ('MSFT', 424.57, 22740970),
        ('AAPL', 169.01, 61281215),
        ('NVDA', 895.70, 54023118),
        ('AMZN', 180.54, 43970295),
        ('META', 493.90, 17730493),
        ('AVGO',1318.97,  3077463),
        ('GOOG', 155.09, 23175556),
        ('TSLA', 166.62,102231390),
        ('COST', 709.51,  2137676),
        ('AMD' , 177.09, 78105903),
        ('NFLX', 611.49,  4503895),
        ('PEP' , 171.63,  5905464),
        ('ADBE', 497.70,  3667357),
        ('LIN' , 461.19,  2403731),
        ('CSCO',  49.22, 19799990);
end;$$