
CREATE SCHEMA IF NOT EXISTS `agenzia_funebre` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `agenzia_funebre` ;


CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`Cliente` (
  `CF` VARCHAR(45) NOT NULL,
  `nome` VARCHAR(45) NULL,
  `Cognome` VARCHAR(45) NULL,
  PRIMARY KEY (`CF`))
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`città` (
  `nome` VARCHAR(45) NULL,
  `regione` VARCHAR(45) NULL,
  `CAP` INT NOT NULL,
  PRIMARY KEY (`CAP`))
ENGINE = InnoDB;



CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`funerali` (
  `idfunerali` INT NOT NULL AUTO_INCREMENT,
  `data_funerale` DATE NULL,
  `prezzo_tot` DOUBLE DEFAULT 1000 check(prezzo_tot>=1000),
  `Cliente_CF` VARCHAR(45) NOT NULL,
  `data_organizzazione` DATE NULL,
  `suo_funerale` VARCHAR(1) default null,
  PRIMARY KEY (`idfunerali`),
  INDEX `fk_funerali_Cliente_idx` (`Cliente_CF` ASC) VISIBLE,
  CONSTRAINT `fk_funerali_Cliente`
    FOREIGN KEY (`Cliente_CF`)
    REFERENCES `agenzia_funebre`.`Cliente` (`CF`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`cliente_vivo` (
  `CF` VARCHAR(45) NOT NULL,
  `CAP_residenza` INT NOT NULL,
  `Via_residenza` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`CF`),
  INDEX `fk_cliente_vivo_Cliente1_idx` (`CF` ASC) VISIBLE,
  INDEX `fk_cliente_vivo_città1_idx` (`CAP_residenza` ASC) VISIBLE,
  CONSTRAINT `fk_cliente_vivo_Cliente1`
    FOREIGN KEY (`CF`)
    REFERENCES `agenzia_funebre`.`Cliente` (`CF`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_cliente_vivo_città1`
    FOREIGN KEY (`CAP_residenza`)
    REFERENCES `agenzia_funebre`.`città` (`CAP`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`Telefono` (
  `CF` VARCHAR(45) NOT NULL,
  `num_telefono` bigint NOT NULL,
  PRIMARY KEY (`num_telefono`),
  FOREIGN KEY (`CF`)
  REFERENCES `agenzia_funebre`.`cliente_vivo`(`CF`)
  ON DELETE cascade
  on update no action
  )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`cliente_morto` (
  `condizioni_corpo` VARCHAR(20) NULL,
  `Cliente_CF` VARCHAR(45) NOT NULL,
  `data_morte` DATE NULL,
  `causa_morte` VARCHAR(45) NULL,
  `funerali_idfunerali` INT ,
  PRIMARY KEY (`Cliente_CF`),
  INDEX `fk_cliente_morto_Cliente1_idx` (`Cliente_CF` ASC) VISIBLE,
  INDEX `fk_cliente_morto_funerali1_idx` (`funerali_idfunerali` ASC) VISIBLE,
  CONSTRAINT `fk_cliente_morto_Cliente1`
    FOREIGN KEY (`Cliente_CF`)
    REFERENCES `agenzia_funebre`.`Cliente` (`CF`)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_cliente_morto_funerali1`
    FOREIGN KEY (`funerali_idfunerali`)
    REFERENCES `agenzia_funebre`.`funerali` (`idfunerali`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`funerale_cristiano` (
  `idfunerali` INT NOT NULL,
  `chiesa_celebrazione` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`idfunerali`),
    FOREIGN KEY (`idfunerali`)
    REFERENCES `agenzia_funebre`.`funerali` (`idfunerali`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`funerale_americano` (
  `idfunerali` INT NOT NULL,
  `luogo_luncheon` VARCHAR(45),
  `orario_inizio` time,
  `orario_fine` time,
  PRIMARY KEY (`idfunerali`),
    FOREIGN KEY (`idfunerali`)
    REFERENCES `agenzia_funebre`.`funerali` (`idfunerali`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`funerale_particolare` (
  `idfunerali` INT NOT NULL,
  `descrizione` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`idfunerali`),
    FOREIGN KEY (`idfunerali`)
    REFERENCES `agenzia_funebre`.`funerali` (`idfunerali`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`luogo_funerale` (
  `idfunerali` INT NOT NULL,
  `nome_luogo` VARCHAR(60) NOT NULL,
  `orario` time,
  PRIMARY KEY (`idfunerali`,`nome_luogo`),
    FOREIGN KEY (`idfunerali`)
    REFERENCES `agenzia_funebre`.`funerali` (`idfunerali`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `id_funerale` INT NOT NULL,
  `tipo_servizio` VARCHAR(45),
  `prezzo` double default 0,
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_funerale`)
    REFERENCES `agenzia_funebre`.`funerali` (`idfunerali`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_fiori` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `quantità` double DEFAULT 0,
  `tipo` VARCHAR(20),
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_maestranza` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `CF_esecutore` VARCHAR(45),
  `strumento` VARCHAR(20),
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_bara` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `quantità` int DEFAULT 0,
  `dimensioni` VARCHAR(20) ,
  `materiale` VARCHAR(45),
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_abiti` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `taglia` VARCHAR(20) ,
  `marca` VARCHAR(45),
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_cremazione` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `luogo_cremazione` VARCHAR(45),
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_trasporto` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `targa` VARCHAR(20) ,
  `ora_inizio_servizio` time,
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_burocrazia` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `tipo` VARCHAR(30) ,
  `id_pratica` VARCHAR(45) ,
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_necrologio` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `affisione` VARCHAR(30) ,
  `descrizione` VARCHAR(120) ,
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_sepoltura` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `ditta_sepoltura` VARCHAR(30) ,
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_pulizia` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `giorno_pulizia` date ,
  `CF_esecutore` VARCHAR(45) ,
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `agenzia_funebre`.`servizio_lapide` (
  `id_ordine` VARCHAR(45) NOT NULL,
  `materiale` VARCHAR(45) ,
  `epitaffio` VARCHAR(160) ,
  `caratteristiche_aggiuntive` VARCHAR(160) ,
  PRIMARY KEY (`id_ordine`),
	FOREIGN KEY (`id_ordine`)
    REFERENCES `agenzia_funebre`.`servizio` (`id_ordine`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
    )
ENGINE = InnoDB;