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
        ('AMD', 183.34, 73152283);
end;$$