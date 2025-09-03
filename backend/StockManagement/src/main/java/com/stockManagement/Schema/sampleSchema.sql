-- SUPPLIER MASTER
CREATE TABLE IF NOT EXISTS tbl_Supplier (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(30) NOT NULL,
  gender     CHAR(1)     NOT NULL CHECK (gender IN ('M','F')),
  mobile     VARCHAR(10) NOT NULL UNIQUE,
  email      VARCHAR(40) NOT NULL UNIQUE,
  country    VARCHAR(20) NOT NULL,
  state      VARCHAR(20) NOT NULL,
  city       VARCHAR(20) NOT NULL,
  address    VARCHAR(255) NOT NULL,
  createdBy  VARCHAR(30) NOT NULL,
  createdAt  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- SUPPLIER LOG (snapshot of old row on update)
CREATE TABLE IF NOT EXISTS tbl_SupplierLog (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplierId BIGINT      NOT NULL,
  name       VARCHAR(30) NOT NULL,
  gender     CHAR(1)     NOT NULL,
  mobile     VARCHAR(10) NOT NULL,
  email      VARCHAR(40) NOT NULL,
  country    VARCHAR(20) NOT NULL,
  state      VARCHAR(20) NOT NULL,
  city       VARCHAR(20) NOT NULL,
  address    VARCHAR(255) NOT NULL,
  createdBy  VARCHAR(30) NOT NULL,
  createdAt  TIMESTAMP   NOT NULL,
  changedBy  VARCHAR(30) NOT NULL,
  changedAt  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (supplierId) REFERENCES tbl_Supplier(id)
);

-- PRODUCT MASTER
CREATE TABLE IF NOT EXISTS tbl_Product (
  id     VARCHAR(30) PRIMARY KEY,
  name   VARCHAR(30) NOT NULL,
  status CHAR(1)     NOT NULL CHECK (status IN ('A','I'))
);

-- PRODUCT LOG (before update)
CREATE TABLE IF NOT EXISTS tbl_ProductLog (
  id     BIGINT AUTO_INCREMENT PRIMARY KEY,
  prodId VARCHAR(30) NOT NULL,
  name   VARCHAR(30) NOT NULL,
  status CHAR(1)     NOT NULL,
  changedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (prodId) REFERENCES tbl_Product(id)
);

-- PURCHASE HEADER
CREATE TABLE IF NOT EXISTS tbl_PurchaseHeader (
  id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplierId           BIGINT        NOT NULL,
  totalTaxableAmount   DECIMAL(12,2) NOT NULL DEFAULT 0,
  totalGst             DECIMAL(12,2) NOT NULL DEFAULT 0,
  totalAmount          DECIMAL(12,2) NOT NULL DEFAULT 0,
  createdAt            DATE          NOT NULL,
  createdBy            VARCHAR(30)   NOT NULL,
  FOREIGN KEY (supplierId) REFERENCES tbl_Supplier(id)
);

-- PURCHASE DETAILS
CREATE TABLE IF NOT EXISTS tbl_PurchaseDetails (
  headerId         BIGINT       NOT NULL,
  batchId          BIGINT  NOT NULL AUTO_INCREMENT UNIQUE,
  productId        VARCHAR(30)  NOT NULL,
  quantity         INT          NOT NULL,
  taxableAmount    DECIMAL(10,2) NOT NULL,
  gstAmount        DECIMAL(10,2) NOT NULL,
  totalAmount      DECIMAL(10,2) NOT NULL,
  expiry           DATE         NOT NULL,
  reservedQuantity INT          NOT NULL DEFAULT 0,
  availableQuantity INT         NOT NULL DEFAULT 0,
  FOREIGN KEY (headerId) REFERENCES tbl_PurchaseHeader(id),
  FOREIGN KEY (productId) REFERENCES tbl_Product(id)
)AUTO_INCREMENT = 12345678;

-- PROFORMA HEADER
CREATE TABLE IF NOT EXISTS tbl_ProformaHeader (
  id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
  supplierId           BIGINT        NOT NULL,
  totalTaxableAmount   DECIMAL(12,2) NOT NULL DEFAULT 0,
  totalGst             DECIMAL(12,2) NOT NULL DEFAULT 0,
  totalAmount          DECIMAL(12,2) NOT NULL DEFAULT 0,
  status               CHAR(1)       NOT NULL DEFAULT 'C' CHECK (status IN ('C','A','E','D')),
  createdAt            DATE          NOT NULL,
  createdBy            VARCHAR(30)   NOT NULL,
  FOREIGN KEY (supplierId) REFERENCES tbl_Supplier(id)
);

-- PROFORMA DETAILS
-- batchId as VARCHAR (aligned with purchase)
CREATE TABLE IF NOT EXISTS tbl_ProformaDetails (
  headerId       BIGINT       NOT NULL,
  batchId       BIGINT  NOT NULL UNIQUE,
  productId      VARCHAR(30)  NOT NULL,
  quantity       INT          NOT NULL,
  taxableAmount  DECIMAL(10,2) NOT NULL,
  gstAmount      DECIMAL(10,2) NOT NULL,
  totalAmount    DECIMAL(10,2) NOT NULL,
  expiry         DATE         NOT NULL,
  status         CHAR(1)      NOT NULL DEFAULT 'C' CHECK (status IN ('C','E','D')),
  FOREIGN KEY (headerId) REFERENCES tbl_ProformaHeader(id),
  FOREIGN KEY (productId) REFERENCES tbl_Product(id)
);

-- PROFORMA LOG HEADER
CREATE TABLE IF NOT EXISTS tbl_ProformaLogHeader (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  headerId           BIGINT        NOT NULL,
  supplierId         BIGINT        NOT NULL,
  totalTaxableAmount DECIMAL(12,2) NOT NULL,
  totalGst           DECIMAL(12,2) NOT NULL,
  totalAmount        DECIMAL(12,2) NOT NULL,
  editType           CHAR(1)       NOT NULL CHECK (editType IN ('E','D')),
  changedAt          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  changedBy          VARCHAR(30)   NOT NULL,
  FOREIGN KEY (supplierId) REFERENCES tbl_Supplier(id),
  FOREIGN KEY (headerId) REFERENCES tbl_ProformaHeader(id)
);

-- PROFORMA LOG DETAILS
CREATE TABLE IF NOT EXISTS tbl_ProformaLogDetails (
  logId         BIGINT       NOT NULL,
  headerId      BIGINT       NOT NULL,
  batchId      BIGINT  NOT NULL ,
  productId     VARCHAR(30)  NOT NULL,
  quantity      INT          NOT NULL,
  taxableAmount DECIMAL(10,2),
  gstAmount     DECIMAL(10,2),
  totalAmount   DECIMAL(10,2),
  expiry        DATE         NOT NULL,
  status        CHAR(1)      NOT NULL DEFAULT 'E' CHECK (status IN ('E','D')),
  FOREIGN KEY (logId)    REFERENCES tbl_ProformaLogHeader(id),
  FOREIGN KEY (productId) REFERENCES tbl_Product(id)
);

-- STOCK-OUT HEADER (generated when a Proforma is approved)
-- IMPORTANT: headerId references ProformaHeader(id)
CREATE TABLE IF NOT EXISTS tbl_StockOutHeader (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
  headerId            BIGINT      NOT NULL,  -- same as ProformaHeader.id
  supplierId          BIGINT      NOT NULL,
  status   CHAR(1)      NOT NULL CHECK (status IN ('I','R')), -- I= Invoice , R=Return 
  totalTaxableAmount  DECIMAL(12,2) NOT NULL DEFAULT 0,
  totalGst            DECIMAL(12,2) NOT NULL DEFAULT 0,
  totalAmount         DECIMAL(12,2) NOT NULL DEFAULT 0,
  approvedAt          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  approvedBy          VARCHAR(30),
  FOREIGN KEY (supplierId) REFERENCES tbl_Supplier(id),
  FOREIGN KEY (headerId)  REFERENCES tbl_ProformaHeader(id)
)auto_increment=1000;

-- STOCK-OUT DETAILS (line-by-line; each line marked Invoice or Return)
CREATE TABLE IF NOT EXISTS tbl_StockOutDetails (
	id bigint NOT NULL,
  headerId       BIGINT       NOT NULL,
  batchId       BIGINT  NOT NULL UNIQUE,
  productId      VARCHAR(30)  NOT NULL,
  quantity       INT          NOT NULL,
 -- I=Invoice, R=Return  lineType       CHAR(1)      NOT NULL CHECK (lineType IN ('I','R')),
  taxableAmount  DECIMAL(10,2) NOT NULL,
  gstAmount      DECIMAL(10,2) NOT NULL,
  totalAmount    DECIMAL(10,2) NOT NULL,
  expiry         DATE,
  FOREIGN KEY (headerId)  REFERENCES tbl_StockOutHeader(headerId),
  FOREIGN KEY (productId) REFERENCES tbl_Product(id),
  foreign key (id) references tbl_StockOutHeader(id)
);
use sms;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(20) NOT NULL  -- store hashed password later, for now plain text is okay
);