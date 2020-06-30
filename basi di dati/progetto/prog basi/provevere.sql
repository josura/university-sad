insert into cliente(CF,nome,Cognome) values ('LCCGRG98P11B429T','giorgio','locicero');
insert into cliente(CF,nome,Cognome) values ('LMADNL99A211B429N','daniele','alma');
insert into cliente(CF,nome,Cognome) values ('LCCCRL96U06B429T','carlo','locicero');
insert into cliente(CF,nome,Cognome) values ('LCRGCM95U06B429R','locurto','giacomo');
insert into città(nome,regione,CAP) values ('serradifalco','sicilia',93010);
insert into `cliente_vivo`(CF,CAP_residenza,Via_residenza) values('LMADNL99A211B429N',93010,'via giovanni 43esimo n4');
insert into `cliente_vivo`(CF,CAP_residenza,Via_residenza) values('LCCCRL96U06B429T',93010,'via giorgio 66esimo n7');
insert into cliente_morto(condizioni_corpo,Cliente_CF,data_morte,causa_morte,funerali_idfunerali) 
values ('belle','LMADNL99A211B429N','2100-12-21','vita',NULL);
insert into funerali(data_funerale,prezzo_tot,Cliente_CF,data_organizzazione,suo_funerale) 
values('2021-12-21',1000,'LCCCRL96U06B429T','2020-12-21',null);

insert into funerali(data_funerale,prezzo_tot,Cliente_CF,data_organizzazione,suo_funerale) 
values(null,1000,'LCCCRL96U06B429T','2050-09-24','s');
insert into cliente_morto(condizioni_corpo,Cliente_CF,data_morte,causa_morte,funerali_idfunerali) 
values ('buone','LCCCRL96U06B429T','2070-09-23','tristezza',2);
insert into cliente_morto(condizioni_corpo,Cliente_CF,data_morte,causa_morte,funerali_idfunerali) 
values ('medie','LCRGCM95U06B429R','2060-09-23','abuh',1);
insert into luogo_funerale(idfunerali,nome_luogo,orario) values(1,'montagna_alta','082000');
insert into servizio(id_ordine,id_funerale,tipo_servizio) values('1',1,'fiori');
insert into servizio_fiori(id_ordine,quantità,tipo) values('1',23.4,'crisantemi');
update servizio_fiori set quantità=quantità+0.1 where id_ordine='1';
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('2',1,'fiori',20.0);
insert into servizio_fiori(id_ordine,quantità,tipo) values('2',25.2,'rose');
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('3',1,'bara',210.0);
insert into servizio_bara(id_ordine,quantità,dimensioni,materiale) values('3',2,'100x200','legno di sambuco');
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('4',1,'trasporto',100.0);
insert into servizio_trasporto(id_ordine,targa,ora_inizio_servizio) values('4','50CCL2353','093000');
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('5',1,'abiti',200.0);
insert into servizio_abiti(id_ordine,taglia,marca) values('5','42','giorgione');
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('6',1,'maestranza',30.0);
insert into servizio_maestranza(id_ordine,CF_esecutore,strumento) values('6','DGFGSD96T21345S','cornamusa');
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('7',1,'cremazione',20.0);
insert into servizio_cremazione(id_ordine,luogo_cremazione) values('7','ufficio legale via monesrrato n5');
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('8',1,'burocrazia',0);
insert into servizio_burocrazia(id_ordine,tipo,id_pratica) values('8','comune',324555);
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('9',1,'sepoltura',0);
insert into servizio_sepoltura(id_ordine,ditta_sepoltura) values('9','peppini becchini');
insert into servizio(id_ordine,id_funerale,tipo_servizio,prezzo) values('10',1,'lapide',50.0);
insert into servizio_lapide(id_ordine,materiale,epitaffio,caratteristiche_aggiuntive) values('10','marmo','Era una cosa speciale, e niente, prima o poi muoiono tutti','incisione di una faccia che piange e di un demone laterale che saluta il visitatore');
insert into servizio(id_ordine,id_funerale,tipo_servizio) values('11',1,'necrologio');
insert into servizio_necrologio(id_ordine,affisione,descrizione) values('11','serradifalco','ricordiamolo ma non poi così tanto');
insert into funerale_americano(idfunerali,luogo_luncheon,orario_inizio,orario_fine) values(1,'via giuseppe toto n27','190000','203000');
insert into telefono(CF,num_telefono) values ('LCCCRL96U06B429T',3452134453);
insert into telefono(CF,num_telefono) values ('LCCCRL96U06B429T',3748264223);
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Quinn","Mejia","178859856-00001"),("Fuller","Bauer","290422179-00009"),("Benedict","Silva","673690871-00003"),("Paul","Fleming","317556330-00007"),("Sean","Gillespie","739886893-00000"),("Jakeem","Gates","049073034-00008"),("Patrick","Collier","548711878-00007"),("Lionel","Conway","891360299-00004"),("Walter","Carr","360096655-00004"),("Rogan","Harvey","715114179-00008");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Addison","Stevenson","027645050-00009"),("Jason","Melendez","464927011-00005"),("Allen","Gallegos","253618797-00000"),("Davis","Alston","732491469-00001"),("Graham","Oneal","989051727-00000"),("Lucius","Palmer","813268091-00002"),("Elmo","Barr","040644627-00008"),("Tarik","Hodges","762429058-00006"),("Scott","Macias","275842896-00001"),("Hakeem","Joyce","004134508-00003");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Damon","Short","341382851-00002"),("Jack","Matthews","537599474-00005"),("Nathaniel","Garza","359228244-00000"),("Chandler","Larson","917624561-00002"),("Daquan","Prince","136328184-00009"),("Malcolm","Berger","625193891-00003"),("Lamar","Doyle","136843638-00000"),("Jacob","Obrien","686395351-00006"),("Rogan","Hess","640582722-00008"),("Talon","Wolf","314609306-00001");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Beau","Bean","679216861-00006"),("Lucian","Stuart","224758490-00003"),("Dante","Morse","187260914-00002"),("Chester","Snyder","802965855-00009"),("Asher","Clements","132399254-00003"),("Griffin","Elliott","038366753-00002"),("Leo","Horn","864606025-00004"),("Ethan","Patton","191410695-00008"),("Ulysses","Casey","418989869-00007"),("Mannix","Myers","156318453-00008");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Orson","Ramsey","624626438-00003"),("Sean","Rollins","356822817-00009"),("Ulysses","Brennan","326074903-00005"),("Callum","Crane","146163050-00009"),("Cole","Eaton","007289457-00009"),("Noble","Savage","824074470-00009"),("Geoffrey","Kennedy","449755388-00005"),("Zachary","Carroll","192575405-00001"),("Addison","Hogan","017724154-00004"),("Cain","Castro","501914493-00009");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Shad","Giles","061668232-00004"),("Seth","Hodges","236006086-00003"),("Duncan","Witt","994448694-00004"),("Peter","Patrick","340273598-00003"),("Channing","Malone","421800350-00009"),("Elton","Cox","141558148-00005"),("Denton","Armstrong","933527756-00003"),("Denton","Benton","773737739-00005"),("Zephania","Jarvis","914058920-00002"),("Driscoll","Clements","513081711-00006");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Harrison","Bates","862339967-00005"),("Asher","Tran","864097720-00006"),("Malachi","Ayers","130190234-00000"),("Tanek","Branch","219573276-00009"),("Leroy","Mullen","135676831-00005"),("Declan","Jackson","901902387-00009"),("Jordan","Wise","445040801-00000"),("Ulric","Ray","645397878-00002"),("Galvin","Johnston","275026011-00005"),("Ishmael","Scott","637014200-00009");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Lamar","Owens","707413563-00008"),("Vaughan","Williams","255404626-00004"),("Jonah","Ferrell","533083036-00004"),("Aidan","Rodgers","775500663-00007"),("Ferdinand","Whitehead","609334974-00001"),("Kenneth","Stanton","550513923-00007"),("Colt","Ryan","996532396-00008"),("Arthur","Hansen","130186992-00009"),("Colby","Garrison","681001251-00005"),("Amos","Brennan","612447144-00009");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Carlos","Morton","383044583-00003"),("Melvin","Farmer","030419279-00002"),("Brian","Gay","635763576-00009"),("Quinn","Cantu","957767296-00008"),("Luke","Glenn","008230146-00006"),("Leonard","Nicholson","604254136-00004"),("Garrison","Callahan","515423069-00001"),("Shad","Wells","793388034-00001"),("Brent","Silva","267898278-00002"),("Raphael","Sloan","941126047-00005");
INSERT INTO `cliente` (`nome`,`Cognome`,`CF`) VALUES ("Tobias","Guthrie","628514200-00003"),("Nehru","Horn","060568839-00009"),("Reece","Dudley","836569608-00000"),("Ray","Barry","379186273-00007"),("Declan","Cooke","205128762-00007"),("Hop","Cox","049641558-00009"),("Chaim","Figueroa","915192066-00008"),("Ray","Green","499502995-00008"),("Prescott","Larsen","705583698-00000"),("Brian","Robles","246345490-00006");