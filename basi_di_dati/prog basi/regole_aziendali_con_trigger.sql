use agenzia_funebre;
-- trigger per aggiornare il prezzo all'aggiunta o all'eliminazione dei servizi

DROP TRIGGER IF EXISTS aggiustaprezzoins;
delimiter $$
create trigger aggiustaprezzoins
after insert on servizio
for each row 
begin 
	update funerali set prezzo_tot=0.03*new.prezzo+new.prezzo+prezzo_tot where idfunerali=new.id_funerale;
end $$

DROP TRIGGER IF EXISTS aggiustaprezzoup;
delimiter $$
create trigger aggiustaprezzoup
after update on servizio
for each row 
begin 
	update funerali set prezzo_tot=0.03*new.prezzo+new.prezzo+prezzo_tot-(0.03*new.prezzo+new.prezzo) where idfunerali=new.id_funerale;
end $$

DROP TRIGGER IF EXISTS aggiustaprezzoidel;
delimiter $$
create trigger aggiustaprezzodel
after delete on servizio
for each row 
begin 
	update funerali set prezzo_tot=prezzo_tot-old.prezzo-0.03*old.prezzo where idfunerali=old.id_funerale;
end $$


-- trigger per l'eliminazione dei contatti telefonici di una persona morta 

DROP TRIGGER IF EXISTS eliminatelefono;
delimiter $$
create trigger eliminatelefono
after delete on cliente_vivo
for each row 
begin 
	if(exists(select * from telefono where CF=old.CF)) then
		delete from telefono where CF=old.CF;
	end if;
end $$

-- questo per cancellare il servizio in questione nella tabella servizio
DROP TRIGGER IF EXISTS cancserviziofiori;
delimiter $$
create trigger cancserviziofiori 
after delete on servizio_fiori
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancservizioabiti;
delimiter $$
create trigger cancservizioabiti 
after delete on servizio_abiti
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancserviziobara;
delimiter $$
create trigger cancserviziobara
after delete on servizio_bara
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancservizioburocrazia;
delimiter $$
create trigger cancservizioburocrazia
after delete on servizio_burocrazia
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancserviziocremazione;
delimiter $$
create trigger cancserviziocremazione
after delete on servizio_cremazione
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancserviziolapide;
delimiter $$
create trigger cancserviziolapide
after delete on servizio_lapide
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancserviziomaestranza;
delimiter $$
create trigger cancserviziomaestranza
after delete on servizio_maestranza
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancservizionecrologio;
delimiter $$
create trigger cancservizionecrologio
after delete on servizio_necrologio
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancserviziopulizia;
delimiter $$
create trigger cancserviziopulizia
after delete on servizio_pulizia
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancserviziosepoltura;
delimiter $$
create trigger cancserviziosepoltura
after delete on servizio_sepoltura
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

DROP TRIGGER IF EXISTS cancserviziotrasporto;
delimiter $$
create trigger cancserviziotrasporto
after delete on servizio_trasporto
for each row 
begin 
delete from servizio where id_ordine=old.id_ordine;
end $$

-- trigger per la bara

DROP TRIGGER IF EXISTS barastessotipo;
delimiter $$
create trigger barastessotipo
before insert on servizio_bara
for each row 
begin
declare id_bara_del varchar(20);
declare quant,prez double;
declare id_funerale_bara int;
select distinct(id_funerale) into id_funerale_bara
						from servizio
                        where id_ordine=new.id_ordine;                       
if (exists(select SB.id_ordine ,SB.quantità 
			from servizio_bara SB join servizio S1 on SB.id_ordine=S1.id_ordine
			where  SB.dimensioni=new.dimensioni and SB.materiale=new.materiale and SB.id_ordine!=new.id_ordine and S1.id_funerale=id_funerale_bara)) then
				select SB.id_ordine ,S1.prezzo ,SB.quantità into id_bara_del,prez,quant
				from servizio_bara SB join servizio S1 on SB.id_ordine=S1.id_ordine
				where  SB.dimensioni=new.dimensioni and SB.materiale=new.materiale and SB.id_ordine!=new.id_ordine and S1.id_funerale=id_funerale_fiori; 
				set new.quantità=quant+new.quantità; 
                update servizio set prezzo=prezzo+prez where new.id_ordine=id_ordine;
                delete from servizio where id_ordine=id_bara_del;
                -- set @messagetext =CONCAT('eliminare il vecchio record di bara',id_bara_del,'ed inserire');
                -- SIGNAL SQLSTATE '70005'
					-- SET MESSAGE_TEXT = @messagetext;
				-- delete from servizio_fiori where id_ordine=id_fiori_del;
end if;
end $$

-- trigger per il servizio fiori

DROP TRIGGER IF EXISTS fioristessotipo;
delimiter $$
create trigger fioristessotipo
before insert on servizio_fiori
for each row 
begin
declare id_fiori_del varchar(20);
declare quant,prez double;
declare id_funerale_fiori int;
select id_funerale into id_funerale_fiori
						from servizio
                        where id_ordine=new.id_ordine;                       
if (exists(select SF.id_ordine ,S1.prezzo ,SF.quantità 
			from servizio_fiori SF join servizio S1 on SF.id_ordine=S1.id_ordine
			where  SF.tipo=new.tipo and SF.id_ordine!=new.id_ordine and S1.id_funerale=id_funerale_fiori)) then
				select SF.id_ordine ,S1.prezzo ,SF.quantità into id_fiori_del,prez,quant
				from servizio_fiori SF join servizio S1 on SF.id_ordine=S1.id_ordine
				where  SF.tipo=new.tipo and SF.id_ordine!=new.id_ordine and S1.id_funerale=id_funerale_fiori; 
				set new.quantità=quant+new.quantità; 
                update servizio set prezzo=prezzo+prez where new.id_ordine=id_ordine;
                delete from servizio where id_ordine=id_fiori_del;
                -- set @messagetext =CONCAT('eliminare il vecchio record di fiori',id_fiori_del);
                -- SIGNAL SQLSTATE '70005'
					-- SET MESSAGE_TEXT = @messagetext;
				-- delete from servizio_fiori where id_ordine=id_fiori_del;
end if;
end $$

-- trigger per funerali già avvenuti
DROP TRIGGER IF EXISTS funavvenuti;
delimiter $$
create trigger funavvenuti 
before insert on servizio
for each row 
begin 
if (exists(select * from funerali where new.id_funerale=idfunerali and DATE(data_funerale)<=DATE(curdate()))) then
	 SIGNAL SQLSTATE '70005'
      SET MESSAGE_TEXT = 'Non si possono aggiungere servizi per funerali già avvenuti';
end if;
end $$

-- trigger per i funerali particolari senza descrizione
DROP TRIGGER IF EXISTS funpartic;
delimiter $$
create trigger funpartic 
before insert on funerale_particolare
for each row 
begin 
if (new.descrizione is null) then
	 SIGNAL SQLSTATE '70005'
      SET MESSAGE_TEXT = 'un funerale particolare deve avere una descrizione';
end if;
end $$

-- trigger per togliere una persona morta dai clienti vivi

DROP TRIGGER IF EXISTS utentemorto;
delimiter $$
create trigger utentemorto 
after insert on cliente_morto
for each row 
begin 
if (exists(select * from cliente_vivo where CF=new.Cliente_CF) ) then
	 delete from cliente_vivo where CF=new.Cliente_CF;
end if;
end $$

DROP TRIGGER IF EXISTS notdead;
delimiter $$
create trigger notdead 
before insert on funerali
for each row 
begin 
if (exists(select * from cliente_morto CM where CM.Cliente_CF=New.Cliente_CF)) then
	 SIGNAL SQLSTATE '70005'
      SET MESSAGE_TEXT = 'Un cliente morto non può organizzare il suo funerale';
end if;
end $$

-- in realtà nel momento in cui un cliente muore viene assegnato ad un funerale e la chiave di cliente morto e CF quindi non c'è la possibilità di mettere due funerali-- 

DROP TRIGGER IF EXISTS onefuneral;
delimiter $$
create trigger onefuneral 
before insert on cliente_morto
for each row 
begin 
if (exists(select * from cliente_morto CM where CM.Cliente_CF=New.Cliente_CF and CM.funerali_idfunerali is not null)) then
	 SIGNAL SQLSTATE '70005'
      SET MESSAGE_TEXT = 'Un cliente morto può avere solo un funerale';
end if;
end $$

-- una persona organizza il suo funerale ma stabilisce pure una data --

DROP TRIGGER IF EXISTS nodatafuneral;
delimiter $$
create trigger nodatafunerale 
before insert on funerali
for each row 
begin 
if (new.suo_funerale is not null and new.data_funerale is not null) then
	 SIGNAL SQLSTATE '70005'
      SET MESSAGE_TEXT = 'un funerale di una persona viva non richiede un data';
end if;
end $$


-- una persona che aveva organizzato il suo funerale muore ed il suo funerale viene organizzato 10 giorni dopo -- 
-- prima di eliminare il record della persona dai clienti vivi si deve inserire nei clienti morti--

DROP TRIGGER IF EXISTS miofuneraleorgtrans;
delimiter $$
create trigger miofuneraleorgtrans
after delete on cliente_vivo
for each row 
begin 
	if(not exists(select * from Cliente_morto where Cliente_CF=old.CF)) then
		insert into cliente_morto(condizioni_corpo,Cliente_CF,data_morte,causa_morte,funerali_idfunerali)
		values (null,old.CF,null,null,null);
	end if;
end $$

DROP TRIGGER IF EXISTS miofuneraleorg;
delimiter $$
create trigger miofuneraleorg 
after insert on cliente_morto
for each row 
begin 
if (exists(select * from funerali where Cliente_CF=new.Cliente_CF and suo_funerale is not null) and new.data_morte is not null) then
	 update funerali set data_funerale=DATE_ADD(new.data_morte, INTERVAL 10 DAY) where Cliente_CF=new.Cliente_CF;
end if;
end $$

DROP TRIGGER IF EXISTS miofuneraleorgup;
delimiter $$
create trigger miofuneraleorgup 
after update on cliente_morto
for each row 
begin 
if (exists(select * from funerali where Cliente_CF=new.Cliente_CF and suo_funerale is not null) and new.data_morte is not null) then
	 update funerali set data_funerale=DATE_ADD(new.data_morte, INTERVAL 10 DAY) where Cliente_CF=new.Cliente_CF;
end if;
end $$


