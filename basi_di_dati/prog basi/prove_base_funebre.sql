use agenzia_funebre;

insert into città(nome,regione,CAP) values ('serradifalco','sicilia',93010);

insert into cliente(CF,nome,Cognome) values ('LCCGRG98P11B429T','giorgio','locicero');
insert into cliente(CF,nome,Cognome) values ('LMADNL99A211B429N','daniele','alma');
insert into cliente(CF,nome,Cognome) values ('LCCCRL96U06B429T','carlo','locicero');
insert into cliente(CF,nome,Cognome) values ('LCCVNC92T22B429T','vincenzo','locicero');

insert into `cliente_vivo`(CF,CAP_residenza) values('LCCCRL96U06B429T',93010);
insert into `cliente_vivo`(CF,CAP_residenza) values('LCCVNC92T22B429T',93010);
insert into `cliente_vivo`(CF,CAP_residenza) values('LMADNL99A211B429N',93010);

update cliente_morto set funerali_idfunerali=1 where Cliente_CF='LCCGRG98P11B429T';

-- prove primo trigger --
insert into cliente_morto(condizioni_corpo,Cliente_CF,data_morte,causa_morte,funerali_idfunerali) 
values ('brutte','LCCGRG98P11B429T','2012-12-21','vita',NULL);

insert into funerali(idfunerali,data_funerale,prezzo_tot,Cliente_CF,data_organizzazione) 
values(1,'2021-12-21',1000,'LCCGRG98P11B429T','2020-12-21');

-- prove secondo trigger -- 

insert into funerali(idfunerali,data_funerale,prezzo_tot,Cliente_CF,data_organizzazione,suo_funerale) 
values(2,null,1000,'LMADNL99A211B429N','2020-12-21','s');

insert into cliente_morto(condizioni_corpo,Cliente_CF,data_morte,causa_morte,funerali_idfunerali) 
values ('brutte','LMADNL99A211B429N','2100-3-12','vita',NULL);

insert into funerale_particolare(idfunerali,descrizione) values (2,'voglio che mia madre venga e dia uno schiaffo sulla mia guancia destra e poi mi strappi il naso molto allegramente');
insert into funerale_particolare(idfunerali,descrizione) values (2,null);

insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('13',1,'fiori',23.4);
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('14',1,'fiori',21.0);

insert into servizio_fiori(id_ordine,quantità,tipo) values('13',21.0,'crisantemi');
insert into servizio_fiori(id_ordine,quantità,tipo) values('14',19.0,'crisantemi');

select * from funerali;

select * from servizio S join servizio_fiori SF on SF.id_ordine=S.id_ordine;

select * from servizio_fiori;

select * from cliente_morto;

select * from `cliente_vivo`;