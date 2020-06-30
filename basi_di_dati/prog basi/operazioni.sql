use agenzia_funebre;
-- op1
delimiter $$
create procedure inserisciClienteVivo(IN nom VARCHAR(20),IN cognom Varchar(20),IN codF VARCHAR(20),in CAPn int,in Viares varchar(45))
begin
insert into cliente(CF,nome,Cognome) values (codF,nom,cognom);
insert into `cliente_vivo`(CF,CAP_residenza,Via_residenza) values(codF,CAPn,Viares);
end $$

-- op2

delimiter $$
create procedure inserisciClienteMorto(IN nom VARCHAR(20),IN cognom Varchar(20),IN codF VARCHAR(20),in idfuneral int,
in concorpo varchar(20), in datamor date, causmort varchar(45))
begin
insert into cliente(CF,nome,Cognome) values (codF,nom,cognom);
insert into cliente_morto(condizioni_corpo,Cliente_CF,data_morte,causa_morte,funerali_idfunerali) 
values (concorpo,codF,datamor,causmort,idfuneral);
end $$

-- op3
delimiter $$
create procedure inserisciFuneraleCristiano(IN suofuner VARCHAR(1),IN codF VARCHAR(20),
 in datafun date,in dataorg date, in prezz double,in chiesa varchar(45))
begin
declare idfun int;
insert into funerali(data_funerale,prezzo_tot,Cliente_CF,data_organizzazione,suo_funerale) 
values(datafun,prezz,codF,dataorg,suofuner);
set idfun= (select max(idfunerali) from funerali);
insert into funerale_cristiano(idfunerali,Chiesa_celebrazione) values(idfun,Chiesa_celebrazione);
end $$


-- op4
delimiter $$
create procedure updatedataFunerale(in datafun date, in idfun int)
begin
update funerali set data_funerale=datafun where idfunerali=idfun; 
end $$

-- op5
delimiter $$
create procedure insertLuogoFunerale(in nome_luo VARCHAR(45),in orario time, in idfun int)
begin
insert into luogo_funerale(idfunerali,nome_luogo,orario) values(idfun,nome_luo,orario);
end $$


-- op6 
delimiter $$
create procedure insertcit(in nom VARCHAR(45),in regi VARCHAR(45), in CAPf int)
begin
insert into città(nome,regione,CAP) values(nom,regi,CAPf);
end $$

-- op7
delimiter $$
create procedure deleteServ( in id varchar(45))
begin
delete from servizio where id_ordine=id;
end $$


-- op8
delimiter $$
create procedure updQuanServFiori(in quantInPiu double, in id varchar(45))
begin
update servizio_fiori set quantità=quantità+quantInPiu where id_ordine=id;
end $$

-- op9 
create procedure insertFiori(in idfuner int,in prezz double, in idserv varchar(45), in quant double,in tipofio varchar(20))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'fiori',prezz);
insert into servizio_fiori(id_ordine,quantità,tipo) values(idserv,quant,tipofio);
end $$

-- op10 
create procedure insertBara(in idfuner int,in prezz double, in idserv varchar(45), in quant int,in dimensioni varchar(20),in material varchar(45))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'bara',prezz);
insert into servizio_bara(id_ordine,quantità,dimensioni,materiale) values(idserv,quant,dimension,material);
end $$
-- op11
create procedure insertTrasporto(in idfuner int,in prezz double, in idserv varchar(45), in orainizio time,in targa varchar(20))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'trasporto',prezz);
insert into servizio_trasporto(id_ordine,targa,ora_inizio_servizio) values(idserv,targa,orainizio);
end $$

-- op12
create procedure insertAbiti(in idfuner int,in prezz double, in idserv varchar(45), in mar varchar(45),in tag varchar(20))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'Abiti',prezz);
insert into servizio_abiti(id_ordine,taglia,marca) values(idserv,tag,mar);
end $$

-- op13
create procedure insertMaestranza(in idfuner int,in prezz double, in idserv varchar(45), in CF_es varchar(45),in strum varchar(20))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'maestranza',prezz);
insert into servizio_maestranza(id_ordine,CF_esecutore,strumento) values(idserv,CF_es,strum);
end $$

-- op14
create procedure insertCremazione(in idfuner int,in prezz double, in idserv varchar(45), in luogo_crem varchar(45))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'cremazione',prezz);
insert into servizio_cremazione(id_ordine,luogo_cremazione) values(idserv,luogo_crem);
end $$

-- op15
create procedure insertBurocrazia(in idfuner int,in prezz double, in idserv varchar(45), in tip varchar(30),in id_prat varchar(45))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'burocrazia',prezz);
insert into servizio_burocrazia(id_ordine,tipo,id_pratica) values(idserv,tip,id_prat);
end $$

-- op16
create procedure insertSepoltura(in idfuner int,in prezz double, in idserv varchar(45), in ditta varchar(30))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'sepoltura',prezz);
insert into servizio_sepoltura(id_ordine,ditta_sepoltura) values(idserv,ditta);
end $$

-- op17
create procedure insertLapide(in idfuner int,in prezz double, in idserv varchar(45), in materia varchar(45),in epitaff varchar(160),in caratt varchar(160))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'lapide',prezz);
insert into servizio_lapide(id_ordine,materiale,epitaffio,caratteristiche_aggiuntive) values(idserv,materia,epitaff,caratt);
end $$
-- op 18
create procedure insertNecrologio(in idfuner int,in prezz double, in idserv varchar(45), in affis varchar(30),in descr varchar(120))
begin
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values(idserv,idfuner,'necrologio',prezz);
insert into servizio_necrologio(id_ordine,affisione,descrizione) values(idserv,affis,descr);
end $$
 -- op19
 create procedure deletLuogoFune(in idfuner int,in luog Varchar(45))
begin
delete from luogo_funerale where nome_luogo=luog and idfunerali=idfuner;
end $$

-- op20
delimiter $$
create procedure inserisciFuneraleAmericano(IN suofuner VARCHAR(1),IN codF VARCHAR(20),
 in datafun date,in dataorg date, in prezz double,in luogo varchar(45),in orain time,in orafine time)
begin
declare idfun int;
insert into funerali(data_funerale,prezzo_tot,Cliente_CF,data_organizzazione,suo_funerale) 
values(datafun,prezz,codF,dataorg,suofuner);
set idfun= (select max(idfunerali) from funerali);
insert into funerale_americano(idfunerali,luogo_luncheon,orario_inizio,orario_fine) values(idfun,luogo,orain,orafine);
end $$

-- op21
select CF,nome,cognome
from funerali F join cliente C on F.Cliente_CF=C.CF,servizio_maestranza SM
where F.Cliente_CF=SM.CF_esecutore;
-- op22
select idfunerali
from funerali F join cliente C on F.Cliente_CF=C.CF
where Cliente_CF='LCCCRL96U06B429T';
-- op23
select CM.Cliente_CF
from funerali  join cliente_morto CM on CM.funerali_idfunerali=idfunerali
where idfunerali=1; 
-- op24
select nome_luogo,orario
from luogo_funerale LF join funerali F on LF.idfunerali=F.idfunerali
where F.idfunerali=1;
-- op25
select *
from servizio
where id_funerale=1;
-- op26
select prezzo_tot
from funerali
where idfunerali=1;
-- op27

WITH RECURSIVE organizzatori_ancestrali AS (
    select   CM.Cliente_CF as salma,F.Cliente_CF organizzatore
    from cliente_morto CM join funerali  F on CM.funerali_idfunerali=F.idfunerali
    UNION ALL
    select OA.organizzatore,F.Cliente_CF
    from organizzatori_ancestrali OA join cliente_morto CM on OA.organizzatore=CM.CLiente_CF ,funerali F
    where CM.funerali_idfunerali=F.idfunerali and (not exists(select * from funerali where Cliente_CF=OA.organizzatore and suo_funerale is not null))
)


SELECT * FROM organizzatori_ancestrali;

-- op28
create view epitaffi_gente(CF_morto,epitaffio) as
select CM.CLiente_CF,epitaffio
from servizio S join funerali on idfunerali=S.id_funerale,servizio_lapide SL,cliente_morto CM
where S.id_ordine=SL.id_ordine and CM.funerali_idfunerali=idfunerali;

select *
from epitaffi_gente
where CF_morto='LCCGRG98P11B429T';

-- op29
create view gente_sfortunata5(CF,nome,cognome,CAP_resid) as
select C.CF,C.nome,C.cognome,CV.CAP_residenza
from Cliente C join cliente_vivo CV on C.CF=CV.CF
where 5<=(select count(*)
		 from funerali
         where Cliente_CF=C.CF);

select GS.CF,GS.nome,GS.cognome,GS.CAP_resid,C.nome as nomecittà,C.regione
from gente_sfortunata5 GS join città C on CAP_resid=CAP;

-- op30
create view gentesfortunata2(CF,num_fun_org) as
select Cliente_CF,count(*)
from funerali
group by Cliente_CF
having 2<=count(*);

select G2.CF,G2.num_fun_org
from gentesfortunata2 G2 join telefono T on G2.CF=T.CF
group by G2.CF,G2.num_fun_org 
having 2<=count(num_telefono);

-- op 31
create procedure insertTelefono(in CFn varchar(20),in num bigint)
begin
insert into telefono(CF,num_telefono) values (CFn,num);
end $$

-- op32
create procedure deletFunerale(in idfun int)
begin
delete from funerali where idfunerali=idfun;
end $$

-- op33
create procedure deletServiziPagamento(in idfun int)
begin
delete from servizio where id_funerale=idfun and prezzo>0;
end $$
-- op34
delimiter $$
create procedure inserisciFuneraleParticolare(IN suofuner VARCHAR(1),IN codF VARCHAR(20),
 in datafun date,in dataorg date, in prezz double,in descr varchar(200))
begin
declare idfun int;
insert into funerali(data_funerale,prezzo_tot,Cliente_CF,data_organizzazione,suo_funerale) 
values(datafun,prezz,codF,dataorg,suofuner);
set idfun= (select max(idfunerali) from funerali);
insert into funerale_particolare(idfunerali,descrizione) values(idfun,descr);
end $$

